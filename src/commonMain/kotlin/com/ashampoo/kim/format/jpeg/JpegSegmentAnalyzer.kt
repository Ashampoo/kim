/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.jpeg.JpegConstants.EOI_MARKER
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.format.jpeg.JpegConstants.SOI_MARKER
import com.ashampoo.kim.format.jpeg.JpegConstants.markerDescription
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_IDENTIFIER
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_START_OF_SCAN
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * Algorithm to find segment offsets, types and lengths
 */
public object JpegSegmentAnalyzer {

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    @JvmStatic
    public fun findSegmentInfos(
        byteReader: ByteReader
    ): List<JpegSegmentInfo> = tryWithImageReadException {

        val soiMarker = byteReader.read2BytesAsInt("SOI", JPEG_BYTE_ORDER)

        require(soiMarker == SOI_MARKER) {
            "JPEG magic number mismatch: ${soiMarker.toHexString()}"
        }

        val segmentInfos = mutableListOf<JpegSegmentInfo>()

        segmentInfos.add(
            JpegSegmentInfo(
                offset = 0,
                marker = SOI_MARKER,
                length = 2
            )
        )

        var positionCounter: Int = ImageFormatMagicNumbers.jpeg.size

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

            if (segmentType == SEGMENT_START_OF_SCAN) {

                val remainingBytesCount = byteReader.contentLength - positionCounter

                segmentInfos.add(
                    JpegSegmentInfo(
                        offset = positionCounter - 2,
                        marker = byteArrayOf(segmentIdentifier, segmentType).toUInt16(JPEG_BYTE_ORDER),
                        length = remainingBytesCount.toInt()
                    )
                )

                byteReader.skipBytes("image bytes", (remainingBytesCount - 2).toInt())

                positionCounter += remainingBytesCount.toInt()

                val eoiMarker = byteReader.read2BytesAsInt("EOI", JPEG_BYTE_ORDER)

                if (eoiMarker == EOI_MARKER) {

                    /* Write the EOI marker if it's really there. */

                    segmentInfos.add(
                        JpegSegmentInfo(
                            offset = positionCounter - 2,
                            marker = EOI_MARKER,
                            length = 2
                        )
                    )
                }

                break
            }

            /* Note: Segment length includes size bytes */
            val remainingSegmentLength =
                byteReader.read2BytesAsInt("segmentLength", JPEG_BYTE_ORDER) - 2

            segmentInfos.add(
                JpegSegmentInfo(
                    offset = positionCounter - 2,
                    marker = byteArrayOf(segmentIdentifier, segmentType).toUInt16(JPEG_BYTE_ORDER),
                    length = remainingSegmentLength + 4
                )
            )

            positionCounter += 2

            if (remainingSegmentLength <= 0)
                throw ImageReadException("Illegal JPEG segment length: $remainingSegmentLength")

            byteReader.skipBytes("skip segment", remainingSegmentLength)

            positionCounter += remainingSegmentLength

        } while (true)

        return segmentInfos
    }

    public data class JpegSegmentInfo(
        val offset: Int,
        val marker: Int,
        val length: Int
    ) {

        override fun toString(): String =
            "$offset = ${markerDescription(marker)} [$length bytes]"
    }
}
