/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ashampoo.kim.format.heic

import com.ashampoo.kim.common.toFourCCTypeString

/**
 * Type of a Box.
 */
data class BoxType internal constructor(
    val bytes: ByteArray,
    val name: String,
    val intValue: Int
) {

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other !is BoxType)
            return false

        return bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int =
        bytes.contentHashCode()

    override fun toString(): String =
        name

    companion object {

        /** File Type box, the first box */
        val FTYP = of("ftyp".encodeToByteArray())

        /** Meta Box for metadata, usually the second box */
        val META = of("meta".encodeToByteArray())

        /** Media Data box, of which there can be many at the end. */
        val MDAT = of("mdat".encodeToByteArray())

        /** Image properties */
        val IPRP = of("iprp".encodeToByteArray())

        /** Item properties */
        val IPCO = of("ipco".encodeToByteArray())

        @Suppress("MagicNumber")
        fun of(typeBytes: ByteArray): BoxType {

            require(typeBytes.size == HeicConstants.TPYE_LENGTH) {
                "BoxType must be always 4 bytes!"
            }

            @Suppress("UnnecessaryParentheses")
            val intValue =
                (typeBytes[0].toInt() shl 24) or
                    (typeBytes[1].toInt() shl 16) or
                    (typeBytes[2].toInt() shl 8) or
                    (typeBytes[3].toInt() shl 0)

            return BoxType(
                bytes = typeBytes,
                name = intValue.toFourCCTypeString(),
                intValue = intValue
            )
        }
    }
}