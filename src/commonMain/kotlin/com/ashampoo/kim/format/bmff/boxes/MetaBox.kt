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
package com.ashampoo.kim.format.bmff.boxes

import com.ashampoo.kim.common.MetadataOffset
import com.ashampoo.kim.common.MetadataType
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BMFFConstants
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

/**
 * EIC/ISO 14496-12 meta box
 *
 * The Meta Box is a container for several metadata boxes.
 */
class MetaBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.META, length, payload), BoxContainer {

    val version: Int

    val flags: ByteArray

    /* Mandatory boxes in META */
    val handlerReferenceBox: HandlerReferenceBox
    val primaryItemBox: PrimaryItemBox
    val itemInfoBox: ItemInformationBox
    val itemLocationBox: ItemLocationBox

    override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            offsetShift = offset + 8
        )

        /* Find & set mandatory boxes. */
        handlerReferenceBox = boxes.find { it.type == BoxType.HDLR } as HandlerReferenceBox
        primaryItemBox = boxes.find { it.type == BoxType.PITM } as PrimaryItemBox
        itemInfoBox = boxes.find { it.type == BoxType.IINF } as ItemInformationBox
        itemLocationBox = boxes.find { it.type == BoxType.ILOC } as ItemLocationBox
    }

    fun findMetadataOffsets(): List<MetadataOffset> {

        val offsets = mutableListOf<MetadataOffset>()

        for (extent in itemLocationBox.extents) {

            val itemInfo = itemInfoBox.map.get(extent.itemId) ?: continue

            when (itemInfo.itemType) {

                BMFFConstants.ITEM_TYPE_EXIF ->
                    offsets.add(
                        MetadataOffset(
                            type = MetadataType.EXIF,
                            offset = extent.offset,
                            length = extent.length
                        )
                    )

                BMFFConstants.ITEM_TYPE_MIME ->
                    offsets.add(
                        MetadataOffset(
                            type = MetadataType.XMP,
                            offset = extent.offset,
                            length = extent.length
                        )
                    )
            }
        }

        /* Sorted for safety. */
        offsets.sortBy { it.offset }

        return offsets
    }

    override fun toString(): String =
        "$type Box version=$version flags=${flags.toHex()} boxes=${boxes.map { it.type }}"
}
