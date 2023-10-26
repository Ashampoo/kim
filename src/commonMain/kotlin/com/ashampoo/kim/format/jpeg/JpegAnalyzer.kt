/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.TiffTags
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_ENTRY_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader

data class JpegAnalyzeResult(
    val byteOrder: ByteOrder,
    val orientationOffset: Long?
)

/**
 * This algorithm quickly identifies offsets of common fields like orientation.
 * If they are present we can make a very quick update to the file without
 * restructuring the whole EXIF.
 */
object JpegAnalyzer {

    const val SEGMENT_IDENTIFIER = 0xFF.toByte()
    const val SEGMENT_START_OF_SCAN = 0xDA.toByte()
    const val MARKER_END_OF_IMAGE = 0xD9.toByte()

    const val APP1_MARKER = 0xE1.toByte()

    private const val ADDITIONAL_BYTE_COUNT_AFTER_HEADER: Int = 12

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    fun analyze(
        byteReader: ByteReader
    ): JpegAnalyzeResult = tryWithImageReadException {

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.jpegShort.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.jpegShort) {
            "JPEG magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        var byteOrder = ByteOrder.BIG_ENDIAN
        var orientationOffset: Long? = null

        var positionCounter: Long = ImageFormatMagicNumbers.jpegShort.size.toLong()

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            var segmentIdentifier = byteReader.readByte() ?: break
            var segmentType = byteReader.readByte() ?: break

            positionCounter += 2

            /*
             * Find the segment marker. Markers are zero or more 0xFF bytes, followed by
             * a 0xFF and then a byte not equal to 0x00 or 0xFF.
             */
            while (
                segmentIdentifier != SEGMENT_IDENTIFIER ||
                segmentType == SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                positionCounter++

                segmentType = nextSegmentType
            }

            if (segmentType == SEGMENT_START_OF_SCAN || segmentType == MARKER_END_OF_IMAGE)
                break

            /* Note: Segment length includes size bytes */
            val segmentLength =
                byteReader.read2BytesAsInt("segmentLength", ByteOrder.BIG_ENDIAN) - 2

            positionCounter += 2

            if (segmentLength <= 0)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            /* We are only looking for the EXIF segment. */
            if (segmentType != APP1_MARKER) {

                byteReader.skipBytes("skip segment", segmentLength.toLong())

                positionCounter += segmentLength

                continue
            }

            val exifBytes = byteReader.readBytes("EXIF identifier", 6)

            positionCounter += 6

            /* Skip the APP1 XMP segment. */
            if (!exifBytes.contentEquals(JpegConstants.EXIF_IDENTIFIER_CODE)) {
                byteReader.skipBytes("skip segment", segmentLength.toLong() - 6)
                positionCounter += segmentLength - 6
                continue
            }

            val tiffHeader = TiffReader.readTiffHeader(byteReader)

            byteOrder = tiffHeader.byteOrder

            byteReader.skipBytes(
                "skip bytes to first IFD",
                tiffHeader.offsetToFirstIFD - TIFF_HEADER_SIZE
            )

            val entryCount = byteReader.read2BytesAsInt("entrycount", byteOrder)

            positionCounter += TIFF_HEADER_SIZE + tiffHeader.offsetToFirstIFD - TIFF_HEADER_SIZE + 2

            for (entryIndex in 0 until entryCount) {

                val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", byteOrder)

                positionCounter += 2

                if (tag == TiffTag.TIFF_TAG_ORIENTATION.tag) {

                    byteReader.skipBytes("skip type and count", 6)

                    positionCounter += 6

                    orientationOffset = positionCounter

                    byteReader.skipBytes("skip TIFF entry", 4)

                    positionCounter += 4

                } else {

                    byteReader.skipBytes("skip TIFF entry", TIFF_ENTRY_LENGTH - 2L)

                    positionCounter += TIFF_ENTRY_LENGTH - 2
                }
            }

            /* Break because we found the EXIF Header */
            break

        } while (true)

        return JpegAnalyzeResult(byteOrder, orientationOffset)
    }
}
