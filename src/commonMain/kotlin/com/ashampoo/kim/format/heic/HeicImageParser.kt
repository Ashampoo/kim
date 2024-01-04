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
package com.ashampoo.kim.format.heic

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.heic.boxes.Box
import com.ashampoo.kim.format.heic.boxes.FtypBox
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PositionTrackingByteReader
import com.ashampoo.kim.model.ImageFormat

object HeicImageParser : ImageParser {

    private val BYTE_ORDER = HeicConstants.HEIC_BYTE_ORDER

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        parseMetadata(PositionTrackingByteReader(byteReader))

    private fun parseMetadata(byteReader: PositionTrackingByteReader): ImageMetadata {

        val allBoxes = mutableListOf<Box>()

        while (true) {

            val box = readBox(byteReader)

            println(box)

            if (box == null)
                break

            allBoxes.add(box)
        }

        // TODO()

        return ImageMetadata(
            imageFormat = ImageFormat.HEIC,
            imageSize = null, // TODO
            exif = null, // TODO
            exifBytes = null, // TODO
            iptc = null, // TODO
            xmp = null // TODO
        )
    }

    private fun readBox(byteReader: PositionTrackingByteReader): Box? {

        /*
         * Check if there are enough bytes for another box.
         * If so, we at least need the 8 header bytes.
         */
        if (byteReader.available < HeicConstants.BOX_HEADER_LENGTH)
            return null

        val offset: Long = byteReader.position.toLong()

        /* Note: The length includes the 8 header bytes. */
        val length: Long =
            byteReader.read4BytesAsInt("length", BYTE_ORDER).toLong()

        val type = BoxType.of(
            byteReader.readBytes("type", HeicConstants.TPYE_LENGTH)
        )

        val actualLength: Long = when (length) {

            /* A vaule of zero indicates that it's the last box. */
            0L -> byteReader.available

            /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
            1L -> byteReader.read8BytesAsLong("length", BYTE_ORDER)

            /* Keep the length we already read. */
            else -> length
        }

        val nextBoxOffset = offset + actualLength

        val remainingBytesToReadInThisBox = (nextBoxOffset - byteReader.position).toInt()

        val bytes = byteReader.readBytes("data", remainingBytesToReadInThisBox)

        return when (type) {
            BoxType.FTYP -> FtypBox(offset, actualLength, bytes)
            else -> Box(offset, type, actualLength, bytes)
        }
    }
}
