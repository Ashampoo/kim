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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.ByteOrder

@Suppress("MagicNumber")
object JpegConstants {

    val JPEG_BYTE_ORDER = ByteOrder.BIG_ENDIAN

    /* Max segment size is 65535 bytes (around 65 kb). */
    const val MAX_SEGMENT_SIZE = 0xFFFF

    val JFIF0_SIGNATURE = byteArrayOf(
        0x4a, // J
        0x46, // F
        0x49, // I
        0x46, // F
        0x0
    )

    val JFIF0_SIGNATURE_ALTERNATIVE = byteArrayOf(
        0x4a, // J
        0x46, // F
        0x49, // I
        0x46, // F
        0x20
    )

    val EXIF_IDENTIFIER_CODE = byteArrayOf(
        0x45, // E
        0x78, // x
        0x69, // i
        0x66, // f
        0, // NUL
        0 // NUL
    )

    const val EXIF_IDENTIFIER_CODE_HEX: String = "457869660000"

    val XMP_IDENTIFIER = byteArrayOf(
        0x68, // h
        0x74, // t
        0x74, // t
        0x70, // p
        0x3A, // :
        0x2F, // /
        0x2F, // /
        0x6E, // n
        0x73, // s
        0x2E, // .
        0x61, // a
        0x64, // d
        0x6F, // o
        0x62, // b
        0x65, // e
        0x2E, // .
        0x63, // c
        0x6F, // o
        0x6D, // m
        0x2F, // /
        0x78, // x
        0x61, // a
        0x70, // p
        0x2F, // /
        0x31, // 1
        0x2E, // .
        0x30, // 0
        0x2F, // /
        0
    )

    val SOI = byteArrayOf(0xFF.toByte(), 0xd8.toByte())

    val EOI = byteArrayOf(0xFF.toByte(), 0xd9.toByte())

    const val JPEG_APP0 = 0xE0
    const val JPEG_APP0_MARKER = 0xFF00 or JPEG_APP0
    const val JPEG_APP1_MARKER = 0xFF00 or JPEG_APP0 + 1
    const val JPEG_APP2_MARKER = 0xFF00 or JPEG_APP0 + 2
    const val JPEG_APP13_MARKER = 0xFF00 or JPEG_APP0 + 13
    const val JPEG_APP14_MARKER = 0xFF00 or JPEG_APP0 + 14
    const val JPEG_APP15_MARKER = 0xFF00 or JPEG_APP0 + 15

    const val JFIF_MARKER = 0xFFE0
    const val DHT_MARKER = 0xFFC0 + 0x4
    const val DAC_MARKER = 0xFFC0 + 0xc

    const val SOF0_MARKER = 0xFFC0
    const val SOF1_MARKER = 0xFFC0 + 0x1
    const val SOF2_MARKER = 0xFFC0 + 0x2
    const val SOF3_MARKER = 0xFFC0 + 0x3
    const val SOF5_MARKER = 0xFFC0 + 0x5
    const val SOF6_MARKER = 0xFFC0 + 0x6
    const val SOF7_MARKER = 0xFFC0 + 0x7
    const val SOF8_MARKER = 0xFFC0 + 0x8
    const val SOF9_MARKER = 0xFFC0 + 0x9
    const val SOF10_MARKER = 0xFFC0 + 0xa
    const val SOF11_MARKER = 0xFFC0 + 0xb
    const val SOF13_MARKER = 0xFFC0 + 0xd
    const val SOF14_MARKER = 0xFFC0 + 0xe
    const val SOF15_MARKER = 0xFFC0 + 0xf

    // marker for restart intervals
    const val DRI_MARKER = 0xFFdd
    const val RST0_MARKER = 0xFFD0
    const val RST1_MARKER = 0xFFD0 + 0x1
    const val RST2_MARKER = 0xFFD0 + 0x2
    const val RST3_MARKER = 0xFFD0 + 0x3
    const val RST4_MARKER = 0xFFD0 + 0x4
    const val RST5_MARKER = 0xFFD0 + 0x5
    const val RST6_MARKER = 0xFFD0 + 0x6
    const val RST7_MARKER = 0xFFD0 + 0x7

    const val EOI_MARKER = 0xFFD9
    const val SOS_MARKER = 0xFFDA
    const val DQT_MARKER = 0xFFDB
    const val DNL_MARKER = 0xFFDC
    const val COM_MARKER = 0xFFFE

    val SOFN_MARKERS = listOf(
        JpegConstants.SOF0_MARKER,
        JpegConstants.SOF1_MARKER,
        JpegConstants.SOF2_MARKER,
        JpegConstants.SOF3_MARKER,
        JpegConstants.SOF5_MARKER,
        JpegConstants.SOF6_MARKER,
        JpegConstants.SOF7_MARKER,
        JpegConstants.SOF9_MARKER,
        JpegConstants.SOF10_MARKER,
        JpegConstants.SOF11_MARKER,
        JpegConstants.SOF13_MARKER,
        JpegConstants.SOF14_MARKER,
        JpegConstants.SOF15_MARKER
    )

    val MARKERS = listOf(
        JPEG_APP0, JPEG_APP0_MARKER,
        JPEG_APP1_MARKER, JPEG_APP2_MARKER, JPEG_APP13_MARKER,
        JPEG_APP14_MARKER, JPEG_APP15_MARKER, JFIF_MARKER,
        SOF0_MARKER, SOF1_MARKER, SOF2_MARKER, SOF3_MARKER, DHT_MARKER,
        SOF5_MARKER, SOF6_MARKER, SOF7_MARKER, SOF8_MARKER, SOF9_MARKER,
        SOF10_MARKER, SOF11_MARKER, DAC_MARKER, SOF13_MARKER,
        SOF14_MARKER, SOF15_MARKER, EOI_MARKER, SOS_MARKER, DQT_MARKER,
        DNL_MARKER, COM_MARKER, DRI_MARKER, RST0_MARKER, RST1_MARKER, RST2_MARKER,
        RST3_MARKER, RST4_MARKER, RST5_MARKER, RST6_MARKER, RST7_MARKER
    )

    val PHOTOSHOP_IDENTIFICATION_STRING = byteArrayOf(
        0x50, // P
        0x68, // h
        0x6F, // o
        0x74, // t
        0x6F, // o
        0x73, // s
        0x68, // h
        0x6F, // o
        0x70, // p
        0x20, //
        0x33, // 3
        0x2E, // .
        0x30, // 0
        0
    )

    const val IPTC_MAX_BLOCK_NAME_LENGTH: Int = 255

    val CONST_8BIM = charsToQuad('8', 'B', 'I', 'M')

    private fun charsToQuad(c1: Char, c2: Char, c3: Char, c4: Char): Int =
        0xFF and c1.code shl 24 or (0xFF and c2.code shl 16) or
            (0xFF and c3.code shl 8) or (0xFF and c4.code shl 0)

}
