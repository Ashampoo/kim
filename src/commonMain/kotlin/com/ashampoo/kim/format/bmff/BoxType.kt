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

public data class BoxType internal constructor(
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

    public companion object {

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
         * ISOBMFF File Type box, the first box
         */
        public val FTYP: BoxType = of("ftyp".encodeToByteArray())

        /**
         * ISOBMFF Meta Box for metadata, usually the second box.
         * It's a container for several more boxes like IINF.
         */
        public val META: BoxType = of("meta".encodeToByteArray())

        /**
         * ISOBMFF Handler Reference Box
         */
        public val HDLR: BoxType = of("hdlr".encodeToByteArray())

        /**
         * ISOBMFF Primary Item Box
         */
        public val PITM: BoxType = of("pitm".encodeToByteArray())

        /**
         * ISOBMFF Item Information Box, container for INFE boxes
         */
        public val IINF: BoxType = of("iinf".encodeToByteArray())

        /*
         * ISOBMFF Item Info Entry Box
         */
        public val INFE: BoxType = of("infe".encodeToByteArray())

        /**
         * ISOBMFF Item Location Box
         */
        public val ILOC: BoxType = of("iloc".encodeToByteArray())

        /**
         * ISOBMFF Media Data box, of which there can be many at the end.
         */
        public val MDAT: BoxType = of("mdat".encodeToByteArray())

        /**
         * ISOBMFF Movie Box, used by CR3
         */
        public val MOOV: BoxType = of("moov".encodeToByteArray())

        /**
         * ISOBMFF Track Box, used by CR3
         */
        public val TRAK: BoxType = of("trak".encodeToByteArray())

        /**
         * ISOBMFF Media Box, used by CR3
         */
        public val MDIA: BoxType = of("mdia".encodeToByteArray())

        /**
         * ISOBMFF Media Information Container, used by CR3
         */
        public val MINF: BoxType = of("minf".encodeToByteArray())

        /**
         * ISOBMFF Sample Table Box, used by CR3
         */
        public val STBL: BoxType = of("stbl".encodeToByteArray())

        /**
         * ISOBMFF Sample Size Box (32-bit), used by CR3
         */
        public val STSZ: BoxType = of("stsz".encodeToByteArray())

        /**
         * ISOBMFF Chunk Offset Box 64-bit, used by CR3
         */
        public val CO64: BoxType = of("co64".encodeToByteArray())

        /**
         * ISOBMFF UUID box, used by CR3
         */
        public val UUID: BoxType = of("uuid".encodeToByteArray())

        /**
         * Extra box for EXIF data as part of the JPEG XL spec.
         */
        public val EXIF: BoxType = of("Exif".encodeToByteArray())

        /**
         * Extra box for XMP data as part of the JPEG XL spec.
         */
        public val XML: BoxType = of("xml ".encodeToByteArray())

        /**
         * Extra box for brotli compressed data as part of the JPEG XL spec.
         */
        public val BROB: BoxType = of("brob".encodeToByteArray())

        /**
         * JPEG XL partical codestream box
         */
        public val JXLP: BoxType = of("jxlp".encodeToByteArray())

        /**
         * CR3 specific: Canon Metadata Tiff (CMT) for Exif IFD0
         */
        public val CMT1: BoxType = of("CMT1".encodeToByteArray())

        /**
         * CR3 specific: Canon Metadata Tiff (CMT) for Exif ExifIFD
         */
        public val CMT2: BoxType = of("CMT2".encodeToByteArray())

        /**
         * CR3 specific: Canon Metadata Tiff (CMT) for Makernotes
         */
        public val CMT3: BoxType = of("CMT3".encodeToByteArray())

        /**
         * CR3 specific: Canon Metadata Tiff (CMT) for Exif GPS IFD
         */
        public val CMT4: BoxType = of("CMT4".encodeToByteArray())

        /**
         * CR3 specific: Thumbnail JPG bytes
         */
        public val THMB: BoxType = of("THMB".encodeToByteArray())

        @Suppress("MagicNumber")
        public fun of(typeBytes: ByteArray): BoxType {

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
