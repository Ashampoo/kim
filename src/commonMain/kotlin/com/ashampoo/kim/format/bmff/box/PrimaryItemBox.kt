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
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes

/**
 * EIC/ISO 14496-12 pitm box
 */
internal class PrimaryItemBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.PITM, offset, size, largeSize, payload) {

    val version: Int

    val flags: ByteArray

    val itemId: Int

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        if (version == 0)
            itemId = byteReader.read2BytesAsInt("itemId", BMFF_BYTE_ORDER)
        else
            itemId = byteReader.read4BytesAsInt("itemId", BMFF_BYTE_ORDER)
    }

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "itemId=$itemId"
}
