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
package com.ashampoo.kim.format.isobmff.boxes

import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.isobmff.BoxReader
import com.ashampoo.kim.format.isobmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

/**
 * The Meta Box is a container for several metadata boxes.
 */
class MetaBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.META, length, payload) {

    val version: Int

    val flags: ByteArray

    /* Mandatory boxes in META */
    val handlerReferenceBox: HandlerReferenceBox
    val primaryItemBox: PrimaryItemBox
    val itemInfoBox: ItemInformationBox
    val itemLocationBox: ItemLocationBox

    val boxes: List<Box>

    override fun toString(): String =
        "$type Box version=$version flags=${flags.toHex()} boxes=${boxes.map { it.type }}"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        boxes = BoxReader.readBoxes(byteReader)

        /* Find & set mandatory boxes. */
        handlerReferenceBox = boxes.find { it.type == BoxType.HDLR } as HandlerReferenceBox
        primaryItemBox = boxes.find { it.type == BoxType.PITM } as PrimaryItemBox
        itemInfoBox = boxes.find { it.type == BoxType.IINF } as ItemInformationBox
        itemLocationBox = boxes.find { it.type == BoxType.ILOC } as ItemLocationBox
    }
}
