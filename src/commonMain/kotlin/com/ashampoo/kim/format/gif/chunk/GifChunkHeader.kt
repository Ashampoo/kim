/*
 * Copyright 2025 Ramon Bouckaert
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

package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.gif.GifChunkType
import com.ashampoo.kim.format.gif.GifConstants.GIF_SIGNATURE
import com.ashampoo.kim.format.gif.GifVersion
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.readBytes

public class GifChunkHeader(
    bytes: ByteArray
) : GifChunk(
    GifChunkType.HEADER,
    bytes
) {

    public val version: GifVersion

    init {

        if (bytes.size != 6)
            throw ImageReadException(
                "Invalid size for GIF header: ${bytes.size} bytes, expected 6 bytes."
            )

        val byteReader = ByteArrayByteReader(bytes)

        /* Read signature */
        val signature = byteReader.readBytes("signature", 3)

        if (!signature.contentEquals(GIF_SIGNATURE))
            throw ImageReadException("Invalid GIF signature: ${signature.decodeToString()}")

        /* Read version */
        val version = byteReader.readBytes("version", 3)

        when {
            GifVersion.GIF87A.matches(version) -> this.version = GifVersion.GIF87A
            GifVersion.GIF89A.matches(version) -> this.version = GifVersion.GIF89A
            else -> throw ImageReadException("Invalid GIF version: ${version.decodeToString()}")
        }
    }
}
