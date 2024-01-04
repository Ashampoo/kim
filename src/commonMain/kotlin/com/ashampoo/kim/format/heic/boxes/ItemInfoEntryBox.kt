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

import com.ashampoo.kim.format.heic.BoxType
import com.ashampoo.kim.format.heic.HeicConstants.HEIC_BYTE_ORDER
import com.ashampoo.kim.input.ByteArrayByteReader

class ItemInfoEntryBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.INFE, length, payload) {

    val version: Int

    val flags: ByteArray

    val itemId: Int

    val itemProtectionIndex: Int

    override fun toString(): String =
        "INFE " +
            "version=$version " +
            "itemId=$itemId " +
            "itemProtectionIndex=$itemProtectionIndex"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        itemId = if (version > 2)
            byteReader.read4BytesAsInt("itemId", HEIC_BYTE_ORDER)
        else
            byteReader.read2BytesAsInt("itemId", HEIC_BYTE_ORDER)

        itemProtectionIndex =
            byteReader.read2BytesAsInt("itemProtectionIndex", HEIC_BYTE_ORDER)

        println(this)
    }
}
