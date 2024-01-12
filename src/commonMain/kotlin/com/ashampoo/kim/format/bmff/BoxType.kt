/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.bmff

import com.ashampoo.kim.common.toFourCCTypeString

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

        /*
         * Important: All the box types listed below are integral components of the
         * ISO base media file format specification. The patent for this specification
         * was filed over 20 years ago and is expired by now. Please exercise caution
         * and refrain from adding any new box types that may fall under an active patent!
         *
         * For example the HEIC format defines "Image spatial Extents" ("ispe") and
         * "Image Rotation" ("irot") which are for that reason not part of this implementation.
         */

        /**
         * File Type box, the first box
         */
        val FTYP = of("ftyp".encodeToByteArray())

        /**
         * Meta Box for metadata, usually the second box.
         * It's a container for several more boxes like IINF.
         */
        val META = of("meta".encodeToByteArray())

        /**
         * Handler Reference Box
         */
        val HDLR = of("hdlr".encodeToByteArray())

        /**
         * Primary Item Box
         */
        val PITM = of("pitm".encodeToByteArray())

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
         * Media Data box, of which there can be many at the end.
         */
        val MDAT = of("mdat".encodeToByteArray())

        @Suppress("MagicNumber")
        fun of(typeBytes: ByteArray): BoxType {

            require(typeBytes.size == BMFFConstants.TPYE_LENGTH) {
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
