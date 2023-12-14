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
import com.ashampoo.kim.common.decodeIso8859BytesToString
import com.ashampoo.kim.common.decompress
import com.ashampoo.kim.common.indexOfNullTerminator
import com.ashampoo.kim.format.png.ChunkType
import com.ashampoo.kim.format.png.PngConstants

class PngChunkZtxt(
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

        var index = bytes.indexOfNullTerminator()

        if (index < 0)
            throw ImageReadException("PNG zTXt chunk keyword is not terminated.")

        keyword = bytes.copyOfRange(
            fromIndex = 0,
            toIndex = index
        ).decodeIso8859BytesToString()

        index++

        val compressionMethod = bytes[index++].toInt()

        if (compressionMethod != PngConstants.COMPRESSION_DEFLATE_INFLATE)
            throw ImageReadException("PNG zTXt chunk has unexpected compression method: $compressionMethod")

        val compressedText = bytes.copyOfRange(index, bytes.size)

        text = decompress(compressedText)
    }

    /**
     * @return Returns the keyword.
     */
    override fun getKeyword(): String =
        keyword

    /**
     * @return Returns the text.
     */
    override fun getText(): String =
        text
}
