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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.toUInt16
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.MetadataExtractor
import com.ashampoo.kim.input.ByteReader

public object JpegMetadataExtractor : MetadataExtractor {

    internal const val SEGMENT_IDENTIFIER = 0xFF.toByte()
    internal const val SEGMENT_START_OF_SCAN = 0xDA.toByte()
    internal const val MARKER_END_OF_IMAGE = 0xD9.toByte()

    private const val ADDITIONAL_BYTE_COUNT_AFTER_HEADER: Int = 12

    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    override fun extractMetadataBytes(
        byteReader: ByteReader
    ): ByteArray = tryWithImageReadException {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.jpeg.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.jpeg) {
            "JPEG magic number mismatch: ${magicNumberBytes.toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        readSegmentBytesIntoList(byteReader, bytes)

        /*
         * Add some more bytes after the header, so it's recognized
         * by most image viewers as a valid (but broken) file.
         */
        repeat(ADDITIONAL_BYTE_COUNT_AFTER_HEADER) {

            byteReader.readByte()?.let {
                bytes.add(it)
            }
        }

        return@tryWithImageReadException bytes.toByteArray()
    }

    internal fun readSegmentBytesIntoList(
        byteReader: ByteReader,
        bytes: MutableList<Byte>
    ) {

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
                segmentIdentifier != SEGMENT_IDENTIFIER ||
                segmentType == SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                bytes.add(nextSegmentType)

                segmentType = nextSegmentType
            }

            if (segmentType == SEGMENT_START_OF_SCAN || segmentType == MARKER_END_OF_IMAGE)
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

            /* Ignore invalid segment lengths */
            if (segmentLength <= 0)
                continue

            val remainingByteCount = byteReader.contentLength - bytes.size

            /* Ignore invalid segment lengths */
            if (segmentLength > remainingByteCount)
                continue

            val segmentBytes = byteReader.readBytes(segmentLength)

            if (segmentBytes.size != segmentLength)
                throw ImageReadException("Incomplete read: ${segmentBytes.size} != $segmentLength")

            bytes.addAll(segmentBytes.asList())

        } while (true)
    }
}
