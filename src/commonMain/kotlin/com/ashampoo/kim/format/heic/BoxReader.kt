/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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

import com.ashampoo.kim.format.heic.HeicConstants.HEIC_BYTE_ORDER
import com.ashampoo.kim.format.heic.boxes.Box
import com.ashampoo.kim.format.heic.boxes.FileTypeBox
import com.ashampoo.kim.format.heic.boxes.ItemInfoEntryBox
import com.ashampoo.kim.format.heic.boxes.ItemInformationBox
import com.ashampoo.kim.format.heic.boxes.ItemLocationBox
import com.ashampoo.kim.format.heic.boxes.ItemPropertiesBox
import com.ashampoo.kim.format.heic.boxes.ItemPropertyContainerBox
import com.ashampoo.kim.format.heic.boxes.MetaBox
import com.ashampoo.kim.input.PositionTrackingByteReader

object BoxReader {

    fun readBoxes(byteReader: PositionTrackingByteReader): List<Box> {

        val boxes = mutableListOf<Box>()

        while (true) {

            val box = readBox(byteReader)

            if (box == null)
                break

            boxes.add(box)
        }

        return boxes
    }

    fun readBox(byteReader: PositionTrackingByteReader): Box? {

        /*
         * Check if there are enough bytes for another box.
         * If so, we at least need the 8 header bytes.
         */
        if (byteReader.available < HeicConstants.BOX_HEADER_LENGTH)
            return null

        val offset: Long = byteReader.position.toLong()

        /* Note: The length includes the 8 header bytes. */
        val length: Long =
            byteReader.read4BytesAsInt("length", HEIC_BYTE_ORDER).toLong()

        val type = BoxType.of(
            byteReader.readBytes("type", HeicConstants.TPYE_LENGTH)
        )

        val actualLength: Long = when (length) {

            /* A vaule of zero indicates that it's the last box. */
            0L -> byteReader.available

            /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
            1L -> byteReader.read8BytesAsLong("length", HEIC_BYTE_ORDER)

            /* Keep the length we already read. */
            else -> length
        }

        val nextBoxOffset = offset + actualLength

        val remainingBytesToReadInThisBox = (nextBoxOffset - byteReader.position).toInt()

        val bytes = byteReader.readBytes("data", remainingBytesToReadInThisBox)

        return when (type) {
            BoxType.FTYP -> FileTypeBox(offset, actualLength, bytes)
            BoxType.META -> MetaBox(offset, actualLength, bytes)
            BoxType.IINF -> ItemInformationBox(offset, actualLength, bytes)
            BoxType.IPRP -> ItemPropertiesBox(offset, actualLength, bytes)
            BoxType.IPCO -> ItemPropertyContainerBox(offset, actualLength, bytes)
            BoxType.INFE -> ItemInfoEntryBox(offset, actualLength, bytes)
            BoxType.ILOC -> ItemLocationBox(offset, actualLength, bytes)
            else -> Box(offset, type, actualLength, bytes)
        }
    }
}
