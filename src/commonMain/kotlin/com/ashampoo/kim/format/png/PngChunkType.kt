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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.common.toFourCCTypeString

/**
 * Type of a PNG chunk.
 *
 * @see [Portable Network Graphics Specification - Chunk specifications](http://www.w3.org/TR/PNG/.11Chunks)
 */
data class PngChunkType internal constructor(
    val bytes: ByteArray,
    val name: String,
    val intValue: Int
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other !is PngChunkType)
            return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int =
        bytes.contentHashCode()

    override fun toString(): String =
        name

    companion object {

        /** Image header */
        val IHDR = of("IHDR".encodeToByteArray())

        /** Image data */
        val IDAT = of("IDAT".encodeToByteArray())

        /** Image end */
        val IEND = of("IEND".encodeToByteArray())

        /** Time */
        val TIME = of("tIME".encodeToByteArray())

        /** Text */
        val TEXT = of("tEXt".encodeToByteArray())

        /** Compressed text */
        val ZTXT = of("zTXt".encodeToByteArray())

        /** UTF-8 text, for example XMP */
        val ITXT = of("iTXt".encodeToByteArray())

        /** EXIF (since 2017) */
        val EXIF = of("eXIf".encodeToByteArray())

        @Suppress("MagicNumber")
        fun of(typeBytes: ByteArray): PngChunkType {

            require(typeBytes.size == PngConstants.TPYE_LENGTH) {
                "ChunkType must be always 4 bytes!"
            }

            @Suppress("UnnecessaryParentheses")
            val intValue =
                (typeBytes[0].toInt() shl 24) or
                    (typeBytes[1].toInt() shl 16) or
                    (typeBytes[2].toInt() shl 8) or
                    (typeBytes[3].toInt() shl 0)

            return PngChunkType(
                bytes = typeBytes,
                name = intValue.toFourCCTypeString(),
                intValue = intValue
            )
        }
    }
}