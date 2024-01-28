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
package com.ashampoo.kim.format.bmff.box

import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

/**
 * EIC/ISO 14496-12 iinf box
 */
class ItemInformationBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.IINF, offset, size, largeSize, payload), BoxContainer {

    val version: Int

    val flags: ByteArray

    val entryCount: Int

    val map: Map<Int, ItemInfoEntryBox>

    override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        if (version == 0)
            entryCount = byteReader.read2BytesAsInt("entryCount", BMFF_BYTE_ORDER)
        else
            entryCount = byteReader.read4BytesAsInt("entryCount", BMFF_BYTE_ORDER)

        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            positionOffset = 4L + if (version == 0) 2 else 4,
            offsetShift = offset + 4 + if (version == 0) 2 else 4
        )

        val map = mutableMapOf<Int, ItemInfoEntryBox>()

        for (box in boxes) {

            box as ItemInfoEntryBox

            map.put(box.itemId, box)
        }

        this.map = map
    }

    override fun toString(): String =
        "$type version=$version flags=${flags.toHex()} ($entryCount entries)"
}
