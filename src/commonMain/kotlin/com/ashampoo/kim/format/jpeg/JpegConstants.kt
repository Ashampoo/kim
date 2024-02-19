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
        0x4A, // J
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

    val SOI = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    val EOI = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    const val JPEG_APP0_MARKER = 0xFFE0
    const val JPEG_APP1_MARKER = 0xFFE1
    const val JPEG_APP2_MARKER = 0xFFE2
    const val JPEG_APP13_MARKER = 0xFFED
    const val JPEG_APP15_MARKER = 0xFFEF

    const val JFIF_MARKER = 0xFFE0
    const val DHT_MARKER = 0xFFC4
    const val DAC_MARKER = 0xFFCC

    const val SOF0_MARKER = 0xFFC0
    const val SOF1_MARKER = 0xFFC1
    const val SOF2_MARKER = 0xFFC2
    const val SOF3_MARKER = 0xFFC3
    const val SOF5_MARKER = 0xFFC5
    const val SOF6_MARKER = 0xFFC6
    const val SOF7_MARKER = 0xFFC7
    const val SOF9_MARKER = 0xFFC9
    const val SOF10_MARKER = 0xFFCA
    const val SOF11_MARKER = 0xFFCB
    const val SOF13_MARKER = 0xFFCD
    const val SOF14_MARKER = 0xFFCE
    const val SOF15_MARKER = 0xFFCF

    const val DRI_MARKER = 0xFFDD
//    const val RST0_MARKER = 0xFFD0
//    const val RST1_MARKER = 0xFFD1
//    const val RST2_MARKER = 0xFFD2
//    const val RST3_MARKER = 0xFFD3
//    const val RST4_MARKER = 0xFFD4
//    const val RST5_MARKER = 0xFFD5
//    const val RST6_MARKER = 0xFFD6
//    const val RST7_MARKER = 0xFFD7

    const val SOI_MARKER = 0xFFD8
    const val EOI_MARKER = 0xFFD9
    const val SOS_MARKER = 0xFFDA
    const val DQT_MARKER = 0xFFDB
    const val DNL_MARKER = 0xFFDC

    const val COM_MARKER_1 = 0xFFFE
    const val COM_MARKER_2 = 0xFFEE

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

    val SOFN_MARKER_BYTES = listOf(
        0xC0.toByte(), 0xC1.toByte(), 0xC2.toByte(),
        0xC3.toByte(), 0xC5.toByte(), 0xC6.toByte(),
        0xC7.toByte(), 0xC9.toByte(), 0xCA.toByte(),
        0xCB.toByte(), 0xCD.toByte(), 0xCE.toByte(),
        0xCF.toByte()
    )

    val APP13_IDENTIFIER = byteArrayOf(
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

    /** Int value of "8BIM" */
    const val IPTC_RESOURCE_BLOCK_SIGNATURE_INT = 943_868_237

    /** Hex value of "8BIM" (38 42 49 4D) */
    const val IPTC_RESOURCE_BLOCK_SIGNATURE_HEX = "3842494d"

    @OptIn(ExperimentalStdlibApi::class)
    fun markerDescription(marker: Int): String =
        when (marker) {
            COM_MARKER_1 -> "COM (Comment)"
            COM_MARKER_2 -> "COM (Comment)"
            DHT_MARKER -> "DHT (Define Huffman Table)"
            DQT_MARKER -> "DQT (Define Quantization Table)"
            DRI_MARKER -> "DRI (Define Restart Interval)"
            EOI_MARKER -> "EOI (End of Image)"
            JPEG_APP0_MARKER -> "APP0 JFIF"
            JPEG_APP1_MARKER -> "APP1 (Application Segment)"
            JPEG_APP2_MARKER -> "APP2 (Application Segment)"
            JPEG_APP13_MARKER -> "APP13 IPTC"
            JPEG_APP15_MARKER -> "APP15 (Application Segment)"
            SOF0_MARKER -> "SOF0 (Start of Frame, Baseline DCT)"
            SOF1_MARKER -> "SOF1 (Start of Frame, Extended Sequential DCT)"
            SOF2_MARKER -> "SOF2 (Start of Frame, Progressive DCT)"
            SOF3_MARKER -> "SOF3 (Start of Frame, Lossless (sequential))"
            SOF5_MARKER -> "SOF5 (Start of Frame, Differential sequential DCT)"
            SOF6_MARKER -> "SOF6 (Start of Frame, Differential progressive DCT)"
            SOF7_MARKER -> "SOF7 (Start of Frame, Differential lossless (sequential))"
            SOF9_MARKER -> "SOF9 (Start of Frame, Extended sequential DCT, Arithmetic coding)"
            SOF10_MARKER -> "SOF10 (Start of Frame, Progressive DCT, Arithmetic coding)"
            SOF11_MARKER -> "SOF11 (Start of Frame, Lossless (sequential), Arithmetic coding)"
            SOF13_MARKER -> "SOF13 (Start of Frame, Differential sequential DCT, Arithmetic coding)"
            SOF14_MARKER -> "SOF14 (Start of Frame, Differential progressive DCT, Arithmetic coding)"
            SOF15_MARKER -> "SOF15 (Start of Frame, Differential lossless (sequential), Arithmetic coding)"
            SOI_MARKER -> "SOI (Start of Image)"
            SOS_MARKER -> "SOS (Start of Scan)"
            else -> marker.toShort().toHexString(HexFormat.UpperCase)
        }
}
