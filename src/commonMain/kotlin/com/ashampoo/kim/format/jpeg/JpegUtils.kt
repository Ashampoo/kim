/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.readAndVerifyBytes
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.readRemainingBytes

internal object JpegUtils {

    fun traverseJFIF(byteReader: ByteReader, visitor: JpegVisitor) {

        byteReader.readAndVerifyBytes("JPEG SOI (0xFFD8)", JpegConstants.SOI)

        var readBytesCount = JpegConstants.SOI.size

        while (true) {

            val markerBytes = ByteArray(2)

            /*
             * Find next marker bytes
             *
             * If there are no more bytes left we end.
             */
            do {

                markerBytes[0] = markerBytes[1]
                markerBytes[1] = byteReader.readByte() ?: return

                readBytesCount++

            } while (
                0xFF and markerBytes[0].toInt() != 0xFF ||
                0xFF and markerBytes[1].toInt() == 0xFF
            )

            val marker = markerBytes.toUInt16(JPEG_BYTE_ORDER)

            if (marker == JpegConstants.EOI_MARKER || marker == JpegConstants.SOS_MARKER) {

                if (!visitor.beginSOS())
                    return

                val imageData = byteReader.readRemainingBytes()

                visitor.visitSOS(marker, markerBytes, imageData)

                /* Break, because the image segment is the last one. */
                break
            }

            /* If we don't have anough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - readBytesCount < 2)
                break

            val segmentLengthBytes = byteReader.readBytes("segmentLengthBytes", 2)

            readBytesCount += 2

            val segmentLength = segmentLengthBytes.toUInt16(JPEG_BYTE_ORDER)

            val segmentContentLength = segmentLength - 2

            val remainingByteCount = byteReader.contentLength - readBytesCount

            /*
             * If the segment specifies a zero length or a length that is
             * longer than the remaining bytes, it's corrupt and should be ignored.
             * That's what ExifTool does.
             */
            if (segmentContentLength <= 0 || segmentContentLength > remainingByteCount)
                continue

            val segmentData = byteReader.readBytes("segmentData", segmentContentLength)

            readBytesCount += segmentContentLength

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
