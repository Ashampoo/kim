/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package com.ashampoo.kim.format.png.chunks

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.decodeLatin1BytesToString
import com.ashampoo.kim.common.indexOfNullTerminator
import com.ashampoo.kim.format.png.ChunkType

class PngChunkText(
    length: Int,
    chunkType: ChunkType,
    crc: Int,
    bytes: ByteArray
) : PngTextChunk(length, chunkType, crc, bytes) {

    @kotlin.jvm.JvmField
    val keyword: String

    @kotlin.jvm.JvmField
    val text: String

    init {

        val index = bytes.indexOfNullTerminator()

        if (index < 0)
            throw ImageReadException("PNG tEXt chunk keyword is not terminated.")

        keyword = bytes.copyOfRange(
            fromIndex = 0,
            toIndex = index
        ).decodeLatin1BytesToString()

        val textLength = bytes.size - (index + 1)

        text = bytes.copyOfRange(
            fromIndex = index + 1,
            toIndex = textLength
        ).decodeLatin1BytesToString()
    }

    override fun getKeyword(): String =
        keyword

    override fun getText(): String =
        text
}
