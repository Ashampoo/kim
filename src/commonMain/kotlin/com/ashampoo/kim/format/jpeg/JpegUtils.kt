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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.BinaryFileParser
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.input.ByteReader

object JpegUtils : BinaryFileParser() {

    init {
        byteOrder = JPEG_BYTE_ORDER
    }

    private fun findNextMarkerBytes(byteReader: ByteReader): ByteArray {

        val markerBytes = ByteArray(2)

        do {
            markerBytes[0] = markerBytes[1]
            markerBytes[1] = byteReader.readByte("JFIF marker")
        } while (
            0xFF and markerBytes[0].toInt() != 0xFF ||
            0xFF and markerBytes[1].toInt() == 0xFF
        )

        return markerBytes
    }

    fun traverseJFIF(byteReader: ByteReader, visitor: JpegVisitor) {

        byteReader.readAndVerifyBytes("JPEG SOI (0xFFD8)", JpegConstants.SOI)

        while (true) {

            val markerBytes = findNextMarkerBytes(byteReader)

            val marker = markerBytes.toUInt16(byteOrder)

            if (marker == JpegConstants.EOI_MARKER || marker == JpegConstants.SOS_MARKER) {

                if (!visitor.beginSOS())
                    return

                val imageData = byteReader.readRemainingBytes()

                visitor.visitSOS(marker, markerBytes, imageData)

                break
            }

            val segmentLengthBytes = byteReader.readBytes("segmentLengthBytes", 2)

            val segmentLength = segmentLengthBytes.toUInt16(byteOrder)

            if (segmentLength < 2)
                throw ImageReadException("Invalid segment size: $segmentLength")

            val segmentData = byteReader.readBytes("segmentData", segmentLength - 2)

            val analyzeNextSegment = visitor.visitSegment(
                marker,
                markerBytes,
                segmentLength,
                segmentLengthBytes,
                segmentData
            )

            if (!analyzeNextSegment)
                return
        }
    }
}
