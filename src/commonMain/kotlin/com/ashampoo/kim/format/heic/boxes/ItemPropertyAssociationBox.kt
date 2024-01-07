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

import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.heic.BoxType
import com.ashampoo.kim.format.heic.HeicConstants.HEIC_BYTE_ORDER
import com.ashampoo.kim.input.ByteArrayByteReader

@OptIn(ExperimentalStdlibApi::class)
class ItemPropertyAssociationBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.IPMA, length, payload) {

    val version: Int

    val flags: ByteArray

    val entryCount: Int

    val propertyIndexWidth: Int

    /**
     * Key = ItemId
     * Value = List of associated indexes in IPCO
     */
    val entries: Map<Int, List<AssociatedProperty>>

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "propertyIndexWidth=$propertyIndexWidth " +
            "entryCount=$entryCount " +
            "entries=$entries"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        propertyIndexWidth = if (flags[2].toInt() == 1) 15 else 7

        entryCount = byteReader.read4BytesAsInt("itemId", HEIC_BYTE_ORDER)

        val entries = mutableMapOf<Int, List<AssociatedProperty>>()

        repeat(entryCount) {

            val itemId = if (version < 1)
                byteReader.read2BytesAsInt("itemId", HEIC_BYTE_ORDER)
            else
                byteReader.read4BytesAsInt("itemId", HEIC_BYTE_ORDER)

            val associationCount = if (propertyIndexWidth == 15)
                byteReader.read2BytesAsInt("associationCount", HEIC_BYTE_ORDER)
            else
                byteReader.readByteAsInt()

            val associatedProperties = mutableListOf<AssociatedProperty>()

            repeat(associationCount) {

                val fullAssociation = if (propertyIndexWidth == 15)
                    byteReader.read2BytesAsInt("associationCount", HEIC_BYTE_ORDER)
                else
                    byteReader.readByteAsInt()

                // FIXME Is this correct? Untested.

                associatedProperties.add(
                    AssociatedProperty(
                        essential = (fullAssociation shr propertyIndexWidth) and 0x01 == 1,
                        index = fullAssociation and ((1 shl propertyIndexWidth) - 1)
                    )
                )
            }

            associatedProperties.sortBy { it.index }

            entries.put(itemId, associatedProperties)
        }

        this.entries = entries
    }
}
