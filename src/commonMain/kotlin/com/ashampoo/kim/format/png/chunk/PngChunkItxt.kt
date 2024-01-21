/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.png.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.decodeLatin1BytesToString
import com.ashampoo.kim.common.decompress
import com.ashampoo.kim.common.indexOfNullTerminator
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.format.png.PngChunkType
import com.ashampoo.kim.format.png.PngConstants

class PngChunkItxt(
    bytes: ByteArray,
    crc: Int
) : PngTextChunk(PngChunkType.ITXT, bytes, crc) {

    @kotlin.jvm.JvmField
    val keyword: String

    @kotlin.jvm.JvmField
    var text: String

    val languageTag: String

    val translatedKeyword: String

    init {

        var terminatorIndex = bytes.indexOfNullTerminator()

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk keyword is not terminated.")

        keyword = bytes.slice(
            startIndex = 0,
            count = terminatorIndex
        ).decodeLatin1BytesToString()

        var index = terminatorIndex + 1

        val compressionFlag = bytes[index++].toInt()

        if (compressionFlag != 0 && compressionFlag != 1)
            throw ImageReadException("PNG iTXt chunk has invalid compression flag: $compressionFlag")

        val compressed = compressionFlag == 1

        val compressionMethod = bytes[index++].toInt()

        if (compressed && compressionMethod != PngConstants.COMPRESSION_DEFLATE_INFLATE)
            throw ImageReadException("PNG iTXt chunk has unexpected compression method: $compressionMethod")

        terminatorIndex = bytes.indexOfNullTerminator(index)

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk language tag is not terminated.")

        languageTag = bytes.copyOfRange(
            fromIndex = index,
            toIndex = terminatorIndex
        ).decodeLatin1BytesToString()

        index = terminatorIndex + 1

        terminatorIndex = bytes.indexOfNullTerminator(index)

        if (terminatorIndex < 0)
            throw ImageReadException("PNG iTXt chunk translated keyword is not terminated.")

        translatedKeyword = bytes.copyOfRange(
            fromIndex = index,
            toIndex = terminatorIndex
        ).decodeToString()

        index = terminatorIndex + 1

        val subBytes = bytes.copyOfRange(
            fromIndex = index,
            toIndex = bytes.size
        )

        text = if (compressed)
            decompress(subBytes)
        else
            subBytes.decodeToString()
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
