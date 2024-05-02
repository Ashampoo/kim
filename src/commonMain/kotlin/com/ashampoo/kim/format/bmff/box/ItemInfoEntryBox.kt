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

import com.ashampoo.kim.common.toFourCCTypeString
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.readNullTerminatedString

/**
 * EIC/ISO 14496-12 infe box
 */
internal class ItemInfoEntryBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.INFE, offset, size, largeSize, payload) {

    val version: Int

    val flags: ByteArray

    val itemId: Int

    val itemProtectionIndex: Int

    val itemType: Int

    val itemName: String

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        /*
         * We need more sample files for testing.
         * Everything I found so far was always version 2.
         * We don't want to write parser logic that we can't actually verify.
         */
        check(version == 2) {
            "Unsupported INFE version: $version"
        }

        flags = byteReader.readBytes("flags", 3)

        itemId = byteReader.read2BytesAsInt("itemId", BMFF_BYTE_ORDER)

        itemProtectionIndex =
            byteReader.read2BytesAsInt("itemProtectionIndex", BMFF_BYTE_ORDER)

        itemType = byteReader.read4BytesAsInt("itemType", BMFF_BYTE_ORDER)

        /* Item name was always empty in test files. */
        itemName = byteReader.readNullTerminatedString("itemName")
    }

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "itemId=$itemId " +
            "itemProtectionIndex=$itemProtectionIndex " +
            "itemType=${itemType.toFourCCTypeString()} " +
            "itemName=$itemName"
}
