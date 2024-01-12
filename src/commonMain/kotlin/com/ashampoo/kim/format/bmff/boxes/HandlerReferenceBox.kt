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

import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

class HandlerReferenceBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.HDLR, length, payload) {

    val version: Int

    val flags: ByteArray

    val handlerType: String

    val name: String

    override fun toString(): String =
        "$type " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "handlerType=$handlerType " +
            "name=$name"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()

        flags = byteReader.readBytes("flags", 3)

        byteReader.skipBytes("pre-defined", 4)

        handlerType = byteReader.readBytes("handlerType", 4).decodeToString()

        byteReader.skipBytes("reserved", 12)

        name = byteReader.readNullTerminatedString("name")
    }
}
