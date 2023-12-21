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
import com.ashampoo.kim.common.toInt
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.MARKER_END_OF_IMAGE
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_IDENTIFIER
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_START_OF_SCAN
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_ENTRY_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.ByteReader

/**
 * Algorithm to find segment offsets, types and lengths
 */
object JpegSegmentAnalyzer {

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    fun findSegmentInfos(
        byteReader: ByteReader
    ): List<JpegSegmentInfo> = tryWithImageReadException {

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.jpeg.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.jpeg) {
            "JPEG magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        val segmentInfos = mutableListOf<JpegSegmentInfo>()

        var positionCounter: Long = ImageFormatMagicNumbers.jpeg.size.toLong()

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
                byteReader.read2BytesAsInt("segmentLength", JPEG_BYTE_ORDER) - 2

            segmentInfos.add(
                JpegSegmentInfo(
                    offset = positionCounter,
                    marker = byteArrayOf(segmentIdentifier, segmentType).toUInt16(JPEG_BYTE_ORDER),
                    length = segmentLength
                )
            )

            positionCounter += 2

            if (segmentLength <= 0)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            byteReader.skipBytes("skip segment", segmentLength.toLong())

            positionCounter += segmentLength

        } while (true)

        return segmentInfos
    }

    data class JpegSegmentInfo(
        val offset: Long,
        val marker: Int,
        val length: Int
    )
}
