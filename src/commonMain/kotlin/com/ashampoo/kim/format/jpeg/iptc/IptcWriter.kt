/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.format.jpeg.iptc.IptcParser.APP13_BYTE_ORDER
import com.ashampoo.kim.output.BigEndianBinaryByteWriter
import com.ashampoo.kim.output.BinaryByteWriter
import com.ashampoo.kim.output.ByteArrayByteWriter
import io.ktor.utils.io.core.toByteArray

object IptcWriter {

    @Suppress("ThrowsCount")
    fun writePhotoshopApp13Segment(data: IptcMetadata): ByteArray {

        val os = ByteArrayByteWriter()

        val bos: BinaryByteWriter = BigEndianBinaryByteWriter(os)

        bos.write(JpegConstants.APP13_IDENTIFIER)

        val blocks = data.rawBlocks

        for (block in blocks) {

            bos.write4Bytes(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_INT)

            if (block.blockType < 0 || block.blockType > 0xFFFF)
                throw ImageWriteException("Invalid IPTC block type: ${block.blockType}")

            bos.write2Bytes(block.blockType)

            val blockNameBytes = block.blockNameBytes

            if (blockNameBytes.size > JpegConstants.IPTC_MAX_BLOCK_NAME_LENGTH)
                throw ImageWriteException("IPTC block name is too long: " + blockNameBytes.size)

            bos.write(blockNameBytes.size)
            bos.write(blockNameBytes)

            /* Pad to even size, including length byte */
            if (blockNameBytes.size % 2 == 0)
                bos.write(0)

            val blockData = block.blockData

            if (blockData.size > IptcConstants.IPTC_NON_EXTENDED_RECORD_MAXIMUM_SIZE)
                throw ImageWriteException("IPTC block data is too long: " + blockData.size)

            bos.write4Bytes(blockData.size)
            bos.write(blockData)

            /* Pad to even size */
            if (blockData.size % 2 == 1)
                bos.write(0)
        }

        bos.flush()

        return os.toByteArray()
    }

    /* Writes the IPTC block in UTF-8 */
    fun writeIPTCBlock(records: List<IptcRecord>): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        val binaryWriter = BinaryByteWriter.createBinaryByteWriter(byteWriter, APP13_BYTE_ORDER)

        /*
         * Set the envelope for UTF-8
         *
         * It's recommended to use UTF-8 as a default to prevent issues with umlauts.
         * And it's more consistent to always write data the same way.
         */
        binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
        binaryWriter.write(IptcConstants.IPTC_ENVELOPE_RECORD_NUMBER)
        binaryWriter.write(IptcParser.CODED_CHARACTER_SET_IPTC_CODE)
        binaryWriter.write2Bytes(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE.size)
        binaryWriter.write(IptcParser.UTF8_CHARACTER_ESCAPE_SEQUENCE)

        /* First, right record version record */
        binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
        binaryWriter.write(IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)
        binaryWriter.write(IptcTypes.RECORD_VERSION.type)
        binaryWriter.write2Bytes(2) // record version record size
        binaryWriter.write2Bytes(IptcConstants.IPTC_RECORD_VERSION_VALUE)

        val sortedRecords: List<IptcRecord> = records.sortedWith(IptcRecord.comparator)

        for ((iptcType, value) in sortedRecords) {

            /* Ignore the record version, because we already wrote it. */
            if (iptcType === IptcTypes.RECORD_VERSION)
                continue

            binaryWriter.write(IptcConstants.IPTC_RECORD_TAG_MARKER)
            binaryWriter.write(IptcConstants.IPTC_APPLICATION_2_RECORD_NUMBER)

            if (iptcType.type < 0 || iptcType.type > 0xFF)
                throw ImageWriteException("Invalid record type: " + iptcType.type)

            binaryWriter.write(iptcType.type)

            val recordData = value.toByteArray()

            binaryWriter.write2Bytes(recordData.size)
            binaryWriter.write(recordData)
        }

        return byteWriter.toByteArray()
    }
}
