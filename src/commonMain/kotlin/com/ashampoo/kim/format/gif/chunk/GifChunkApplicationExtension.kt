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
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readBytes

public class GifChunkApplicationExtension(
    header: ByteArray,
    subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.APPLICATION_EXTENSION,
    subChunks
        .fold(header) { acc, subChunk ->
            acc + subChunk
        }
        .plus(0x00)
) {

    private val xmpMetaTag: String = "x:xmpmeta"

    public val applicationIdentifier: String
    public val applicationCode: String

    init {

        if (subChunks.isEmpty())
            throw ImageReadException("Application extension must have at least 1 subchunk.")

        val firstSubChunkByteReader = ByteArrayByteReader(subChunks.first())

        val firstSubChunkSize = firstSubChunkByteReader.readByte("first sub chunk size").toInt()

        if (firstSubChunkSize < 8)
            throw ImageReadException(
                "Invalid size for initial application extension sub chunk: $firstSubChunkSize bytes," +
                    " expected at least 8 bytes (typically 11)."
            )

        applicationIdentifier = firstSubChunkByteReader.readBytes(
            fieldName = "application identifier",
            count = 8
        ).decodeToString()

        applicationCode = firstSubChunkByteReader.readBytes(
            fieldName = "application code",
            count = firstSubChunkSize - 8
        ).decodeToString()
    }

    public fun parseAsXmpOrThrow(): String {

        /*
         * XMP data is stored unchunked, and instead is terminated by a huge "magic trailer".
         * It is easier to just parse the whole thing as a string and then manually extract the XMP data.
         */
        val extensionContentAsString = try {
            bytes.decodeToString()
        } catch (e: CharacterCodingException) {
            throw ImageReadException("Failed to decode application extension bytes as string.", e)
        }

        if (!extensionContentAsString.contains("<x:xmpmeta"))
            throw ImageReadException("No XMP data found in application extension.")

        return "<$xmpMetaTag" + extensionContentAsString
            .substringAfter("<$xmpMetaTag")
            .substringBefore("</$xmpMetaTag>")
            .plus("</$xmpMetaTag>")
    }
}
