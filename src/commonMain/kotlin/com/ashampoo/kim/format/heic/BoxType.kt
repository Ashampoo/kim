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

        /**
         * Meta Box for metadata, usually the second box.
         * It's a container for several more boxes like IINF.
         */
        val META = of("meta".encodeToByteArray())

        /**
         * Item Information Box, container for INFE boxes
         */
        val IINF = of("iinf".encodeToByteArray())

        /*
         * Item Info Entry Box
         */
        val INFE = of("infe".encodeToByteArray())

        /**
         * Item Location Box
         */
        val ILOC = of("iloc".encodeToByteArray())

        /**
         * Image Spatial Extents Box
         */
        val ISPE = of("ispe".encodeToByteArray())

        /*
         * Image Rotation Box
         */
        val IROT = of("irot".encodeToByteArray())

        /**
         * Item Properties Box
         *
         * Contains IPCO & COLR
         */
        val IPRP = of("iprp".encodeToByteArray())

        /**
         * Item Property Container Box
         */
        val IPCO = of("ipco".encodeToByteArray())

        /** Media Data box, of which there can be many at the end. */
        val MDAT = of("mdat".encodeToByteArray())

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
