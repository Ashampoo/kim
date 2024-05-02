/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashampoo.kim.format.jpeg.iptc

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.decodeLatin1BytesToString
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.toInt
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.common.toUInt8
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes.Companion.getIptcType
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.skipToQuad
import kotlin.jvm.JvmStatic

public object IptcParser {

    internal val EMPTY_BYTE_ARRAY = byteArrayOf()

    /**
     * Block types (or Image Resource IDs) that are not recommended to be
     * interpreted when libraries process Photoshop IPTC metadata.
     *
     * @see https://www.adobe.com/devnet-apps/photoshop/fileformatashtml/
     */
    @Suppress("MagicNumber")
    private val PHOTOSHOP_IGNORED_BLOCK_TYPE = listOf(1084, 1085, 1086, 1087)

    public const val CODED_CHARACTER_SET_IPTC_CODE = 90

    /* "ESC % G" as bytes */
    public val UTF8_CHARACTER_ESCAPE_SEQUENCE =
        byteArrayOf('\u001B'.code.toByte(), '%'.code.toByte(), 'G'.code.toByte())

    public val APP13_BYTE_ORDER = ByteOrder.BIG_ENDIAN

    /**
     * Checks if the ByteArray starts with the Photoshop identifaction header.
     * This is mandatory for IPTC embedded into APP13.
     */
    @JvmStatic
    public fun isPhotoshopApp13Segment(segmentData: ByteArray): Boolean {

        if (!segmentData.startsWith(JpegConstants.APP13_IDENTIFIER))
            return false

        val index = JpegConstants.APP13_IDENTIFIER.size

        return index + 4 <= segmentData.size &&
            segmentData.toInt(index, APP13_BYTE_ORDER) == JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT
    }

    /**
     * Parses IPTC from the given string.
     *
     * @param bytes                 The IPTC bytes
     * @param startsWithApp13Header If IPTC is read from JPEG the header is required.
     */
    @JvmStatic
    public fun parseIptc(
        bytes: ByteArray,
        startsWithApp13Header: Boolean = true
    ): IptcMetadata {

        val records = mutableListOf<IptcRecord>()

        val blocks = parseAllIptcBlocks(bytes, startsWithApp13Header)

        for (block in blocks) {

            /* Ignore everything but IPTC data. */
            if (!block.isIPTCBlock())
                continue

            records.addAll(parseIPTCBlock(block.blockData))
        }

        return IptcMetadata(records, blocks)
    }

    private fun parseIPTCBlock(bytes: ByteArray): List<IptcRecord> {

        var isUtf8 = false

        val records = mutableListOf<IptcRecord>()

        var index = 0

        @Suppress("LoopWithTooManyJumpStatements")
        while (index + 1 < bytes.size) {

            val tagMarker = bytes[index++].toUInt8()

            /* We look after the IPTC record tag marker to read. */
            if (tagMarker != IptcConstants.IPTC_RECORD_TAG_MARKER)
                continue

            val recordNumber = bytes[index++].toUInt8()
            val recordType = bytes[index++].toUInt8()

            val recordSize = bytes.toUInt16(index, APP13_BYTE_ORDER)
            index += 2

            val extendedDataset = recordSize > IptcConstants.IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE

            /* Ignore extended dataset and everything after. */
            if (extendedDataset)
                return records

            val recordData = bytes.slice(index, recordSize)

            index += recordSize

            if (recordNumber == IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER &&
                recordType == CODED_CHARACTER_SET_IPTC_CODE
            ) {
                isUtf8 = isUtf8(recordData)
                continue
            }

            if (recordNumber != IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)
                continue

            if (recordType == 0)
                continue

            records.add(
                IptcRecord(
                    iptcType = getIptcType(recordType),
                    value = if (isUtf8)
                        recordData.decodeToString()
                    else
                        recordData.decodeLatin1BytesToString()
                )
            )
        }

        return records
    }

    private fun parseAllIptcBlocks(
        bytes: ByteArray,
        startsWithApp13Header: Boolean
    ): List<IptcBlock> {

        val blocks = mutableListOf<IptcBlock>()

        val byteReader = ByteArrayByteReader(bytes)

        if (startsWithApp13Header) {

            val idString = byteReader.readBytes(
                "App13 Segment identifier",
                JpegConstants.APP13_IDENTIFIER.size
            )

            if (!JpegConstants.APP13_IDENTIFIER.contentEquals(idString))
                throw ImageReadException(
                    "Not a Photoshop App13 segment: ${idString.contentToString()} " +
                        " != " + JpegConstants.APP13_IDENTIFIER.contentToString()
                )
        }

        @Suppress("LoopWithTooManyJumpStatements")
        while (true) {

            val resourceBlockSignature: Int = try {
                byteReader.read4BytesAsInt("Image Resource Block Signature", APP13_BYTE_ORDER)
            } catch (ignore: ImageReadException) {
                break
            }

            if (resourceBlockSignature != JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT) {

                /*
                 * Some files seem to contain invalid markers: 04 3A 00 00 in case of our test data.
                 * We just ignore these and skip to the next 8BIM (38 42 49 4D) segment.
                 * If we can't skip to the next we found everything we can interpret.
                 */
                val skipSuccessful = byteReader.skipToQuad(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

                if (!skipSuccessful)
                    break
            }

            val blockType = byteReader.read2BytesAsInt("IPTC block type", APP13_BYTE_ORDER)

            /*
             * Skip blocks that the photoshop spec recommends to.
             *
             * See discussion on https://issues.apache.org/jira/browse/IMAGING-246
             */
            if (PHOTOSHOP_IGNORED_BLOCK_TYPE.contains(blockType)) {

                /*
                 * If there is still data in this block, before the next image resource block (8BIM),
                 * then we must consume these bytes to leave a pointer ready to read the next block.
                 */
                byteReader.skipToQuad(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

                continue
            }

            val blockNameLength = byteReader.readByte("block name length").toInt()

            var blockNameBytes: ByteArray

            if (blockNameLength == 0) {

                byteReader.readByte("empty name")
                blockNameBytes = EMPTY_BYTE_ARRAY

            } else {

                blockNameBytes = try {
                    byteReader.readBytes("block name bytes", blockNameLength)
                } catch (ignore: ImageReadException) {
                    break
                }

                if (blockNameLength % 2 == 0)
                    byteReader.readByte("block name padding byte")
            }

            val blockSize = byteReader.read4BytesAsInt("block size", APP13_BYTE_ORDER)

            /*
             * Note: This doesn't catch cases where blocksize is invalid but is still less
             * than "bytes.size", but will at least prevent OutOfMemory errors.
             */
            if (blockSize > bytes.size)
                throw ImageReadException("Invalid Block Size : " + blockSize + " > " + bytes.size)

            val blockData: ByteArray = try {
                byteReader.readBytes("block data", blockSize)
            } catch (ignore: ImageReadException) {
                break
            }

            blocks.add(IptcBlock(blockType, blockNameBytes, blockData))

            if (blockSize % 2 != 0)
                byteReader.readByte("block data padding byte")
        }

        return blocks
    }

    private fun isUtf8(codedCharset: ByteArray): Boolean {

        /*
         * check if encoding is a escape sequence
         * normalize encoding byte sequence
         */
        val codedCharsetNormalized = ByteArray(codedCharset.size)

        var index = 0
        for (element in codedCharset)
            if (element != ' '.code.toByte())
                codedCharsetNormalized[index++] = element

        return UTF8_CHARACTER_ESCAPE_SEQUENCE.contentEquals(codedCharsetNormalized)
    }
}
