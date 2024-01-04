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
package com.ashampoo.kim.format.heic.boxes

import com.ashampoo.kim.format.heic.BoxReader
import com.ashampoo.kim.format.heic.BoxType
import com.ashampoo.kim.format.heic.HeicConstants.HEIC_BYTE_ORDER
import com.ashampoo.kim.input.ByteArrayByteReader

class ItemInformationBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.IINF, length, payload)  {

    val version: Int

    val flags: ByteArray

    val entryCount: Int

    val boxes: List<Box>

    override fun toString(): String =
        "IINF ($entryCount entries) = ${boxes.map { it.type }}"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        if (version == 0)
            entryCount = byteReader.read2BytesAsInt("entryCount", HEIC_BYTE_ORDER)
        else
            entryCount = byteReader.read4BytesAsInt("entryCount", HEIC_BYTE_ORDER)

        boxes = BoxReader.readBoxes(byteReader)
    }
}
