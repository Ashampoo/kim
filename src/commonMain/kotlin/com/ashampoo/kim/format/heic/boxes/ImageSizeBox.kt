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

/**
 * The Meta Box is a container for several metadata boxes.
 */
class ImageSizeBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.ISPE, length, payload) {

    val version: Int

    val flags: ByteArray

    val width: Int

    val height: Int

    override fun toString(): String =
        "ISPE " +
            "version=$version " +
            "flags=${flags.toHex()} " +
            "width=$width " +
            "height=$height"

    init {

        val byteReader = ByteArrayByteReader(payload)

        version = byteReader.readByteAsInt()
        flags = byteReader.readBytes("flags", 3)

        width = byteReader.read4BytesAsInt("width", HEIC_BYTE_ORDER)
        height = byteReader.read4BytesAsInt("height", HEIC_BYTE_ORDER)

        println(this)
    }
}
