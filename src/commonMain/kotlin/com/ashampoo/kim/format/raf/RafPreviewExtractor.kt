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
package com.ashampoo.kim.format.raf

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.PreviewExtractor
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor
import com.ashampoo.kim.input.ByteReader

object RafPreviewExtractor : PreviewExtractor {

    override fun extractPreviewImage(byteReader: ByteReader, length: Long): ByteArray? {

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.raf.size).toList()

        /* Ensure it's actually an RAF. */
        require(magicNumberBytes == ImageFormatMagicNumbers.raf) {
            "RAF magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        RafMetadataExtractor.skipToJpegMagicBytes(byteReader)

        val bytes = mutableListOf<Byte>()

        bytes.addAll(ImageFormatMagicNumbers.jpeg)

        /*
         * First read the whole metadata part of the file.
         */

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            var segmentIdentifier = byteReader.readByte() ?: break
            var segmentType = byteReader.readByte() ?: break

            bytes.add(segmentIdentifier)
            bytes.add(segmentType)

            /*
             * Find the segment marker. Markers are zero or more 0xFF bytes, followed by
             * a 0xFF and then a byte not equal to 0x00 or 0xFF.
             */
            while (
                segmentIdentifier != JpegMetadataExtractor.SEGMENT_IDENTIFIER ||
                segmentType == JpegMetadataExtractor.SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                bytes.add(nextSegmentType)

                segmentType = nextSegmentType
            }

            if (segmentType == JpegMetadataExtractor.SEGMENT_START_OF_SCAN)
                break

            val segmentLengthFirstByte = byteReader.readByte() ?: break
            val segmentLengthSecondByte = byteReader.readByte() ?: break

            bytes.add(segmentLengthFirstByte)
            bytes.add(segmentLengthSecondByte)

            /* Next 2-bytes are <segment-size>: [high-byte] [low-byte] */
            var segmentLength: Int = byteArrayOf(segmentLengthFirstByte, segmentLengthSecondByte)
                .toUInt16(ByteOrder.BIG_ENDIAN)

            /* Segment length includes size bytes, so subtract two */
            segmentLength -= 2

            if (segmentLength <= 0)
                throw ImageReadException("Illegal JPEG segment length: $segmentLength")

            val segmentBytes = byteReader.readBytes(segmentLength)

            if (segmentBytes.size != segmentLength)
                throw ImageReadException("Incomplete read: ${segmentBytes.size} != $segmentLength")

            bytes.addAll(segmentBytes.asList())

        } while (true)

        /*
         * Now we are in Start-of-Scan segment and need to read until FF D9 (EOI)
         */

        @Suppress("LoopWithTooManyJumpStatements")
        while (true) {

            val byte = byteReader.readByte() ?: break

            bytes.add(byte)

            /* Search the header and then break */
            if (bytes.size >= 2 &&
                bytes[bytes.lastIndex - 1] == JpegMetadataExtractor.SEGMENT_IDENTIFIER &&
                bytes[bytes.lastIndex - 0] == JpegMetadataExtractor.MARKER_END_OF_IMAGE
            ) break
        }

        return bytes.toByteArray()
    }
}
