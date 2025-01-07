/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
public object JpegConstants {

    public val JPEG_BYTE_ORDER: ByteOrder = ByteOrder.BIG_ENDIAN

    /* Max segment size is 65535 bytes (around 65 kb). */
    public const val MAX_SEGMENT_SIZE: Int = 0xFFFF

    public val JFIF0_SIGNATURE: ByteArray = byteArrayOf(
        0x4a, // J
        0x46, // F
        0x49, // I
        0x46, // F
        0x0
    )

    public val JFIF0_SIGNATURE_ALTERNATIVE: ByteArray = byteArrayOf(
        0x4A, // J
        0x46, // F
        0x49, // I
        0x46, // F
        0x20
    )

    public val EXIF_IDENTIFIER_CODE: ByteArray = byteArrayOf(
        0x45, // E
        0x78, // x
        0x69, // i
        0x66, // f
        0, // NUL
        0 // NUL
    )

    public const val EXIF_IDENTIFIER_CODE_HEX: String = "457869660000"

    public val XMP_IDENTIFIER: ByteArray = byteArrayOf(
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

    public val SOI: ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    public val EOI: ByteArray = byteArrayOf(0xFF.toByte(), 0xD9.toByte())

    public const val JPEG_APP0_MARKER: Int = 0xFFE0
    public const val JPEG_APP1_MARKER: Int = 0xFFE1
    public const val JPEG_APP2_MARKER: Int = 0xFFE2
    public const val JPEG_APP3_MARKER: Int = 0xFFE3
    public const val JPEG_APP4_MARKER: Int = 0xFFE4
    public const val JPEG_APP5_MARKER: Int = 0xFFE5
    public const val JPEG_APP6_MARKER: Int = 0xFFE6
    public const val JPEG_APP7_MARKER: Int = 0xFFE7
    public const val JPEG_APP8_MARKER: Int = 0xFFE8
    public const val JPEG_APP9_MARKER: Int = 0xFFE9
    public const val JPEG_APP10_MARKER: Int = 0xFFEA
    public const val JPEG_APP11_MARKER: Int = 0xFFEB
    public const val JPEG_APP12_MARKER: Int = 0xFFEC
    public const val JPEG_APP13_MARKER: Int = 0xFFED
    public const val JPEG_APP14_MARKER: Int = 0xFFEE
    public const val JPEG_APP15_MARKER: Int = 0xFFEF

    public const val JFIF_MARKER: Int = 0xFFE0
    public const val DHT_MARKER: Int = 0xFFC4
    public const val DAC_MARKER: Int = 0xFFCC

    public const val SOF0_MARKER: Int = 0xFFC0
    public const val SOF1_MARKER: Int = 0xFFC1
    public const val SOF2_MARKER: Int = 0xFFC2
    public const val SOF3_MARKER: Int = 0xFFC3
    public const val SOF5_MARKER: Int = 0xFFC5
    public const val SOF6_MARKER: Int = 0xFFC6
    public const val SOF7_MARKER: Int = 0xFFC7
    public const val SOF9_MARKER: Int = 0xFFC9
    public const val SOF10_MARKER: Int = 0xFFCA
    public const val SOF11_MARKER: Int = 0xFFCB
    public const val SOF13_MARKER: Int = 0xFFCD
    public const val SOF14_MARKER: Int = 0xFFCE
    public const val SOF15_MARKER: Int = 0xFFCF

    public const val DRI_MARKER: Int = 0xFFDD
    public const val RST0_MARKER: Int = 0xFFD0
    public const val RST1_MARKER: Int = 0xFFD1
    public const val RST2_MARKER: Int = 0xFFD2
    public const val RST3_MARKER: Int = 0xFFD3
    public const val RST4_MARKER: Int = 0xFFD4
    public const val RST5_MARKER: Int = 0xFFD5
    public const val RST6_MARKER: Int = 0xFFD6
    public const val RST7_MARKER: Int = 0xFFD7

    public const val SOI_MARKER: Int = 0xFFD8
    public const val EOI_MARKER: Int = 0xFFD9
    public const val SOS_MARKER: Int = 0xFFDA
    public const val DQT_MARKER: Int = 0xFFDB
    public const val DNL_MARKER: Int = 0xFFDC
    public const val JPG_EXT_MARKER: Int = 0xFFC8
    public const val DHP_MARKER: Int = 0xFFDE
    public const val EXP_MARKER: Int = 0xFFDF

    public const val COM_MARKER_1: Int = 0xFFFE
    public const val COM_MARKER_2: Int = 0xFFEE

    public val SOFN_MARKERS: List<Int> = listOf(
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

    public val SOFN_MARKER_BYTES: List<Byte> = listOf(
        0xC0.toByte(), 0xC1.toByte(), 0xC2.toByte(),
        0xC3.toByte(), 0xC5.toByte(), 0xC6.toByte(),
        0xC7.toByte(), 0xC9.toByte(), 0xCA.toByte(),
        0xCB.toByte(), 0xCD.toByte(), 0xCE.toByte(),
        0xCF.toByte()
    )

    public val APP13_IDENTIFIER: ByteArray = byteArrayOf(
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

    public const val IPTC_MAX_BLOCK_NAME_LENGTH: Int = 255

    /** Int value of "8BIM" */
    public const val IPTC_RESOURCE_BLOCK_SIGNATURE_INT: Int = 943_868_237

    /** Hex value of "8BIM" (38 42 49 4D) */
    public const val IPTC_RESOURCE_BLOCK_SIGNATURE_HEX: String = "3842494d"

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("kotlin:S1479")
    public fun markerDescription(marker: Int): String =
        when (marker) {
            /* Every file has these markers */
            SOI_MARKER -> "SOI (Start of Image)"
            DHT_MARKER -> "DHT (Define Huffman Table)"
            DQT_MARKER -> "DQT (Define Quantization Table)"
            SOS_MARKER -> "SOS (Start of Scan)"
            EOI_MARKER -> "EOI (End of Image)"
            /* Application segmements */
            JPEG_APP0_MARKER -> "APP0 (Application Segment, JFIF)"
            JPEG_APP1_MARKER -> "APP1 (Application Segment, EXIF & XMP)"
            JPEG_APP2_MARKER -> "APP2 (Application Segment, ICC & FlashPix)"
            JPEG_APP3_MARKER -> "APP3 (Application Segment)"
            JPEG_APP4_MARKER -> "APP4 (Application Segment)"
            JPEG_APP5_MARKER -> "APP5 (Application Segment)"
            JPEG_APP6_MARKER -> "APP6 (Application Segment)"
            JPEG_APP7_MARKER -> "APP7 (Application Segment)"
            JPEG_APP8_MARKER -> "APP8 (Application Segment)"
            JPEG_APP9_MARKER -> "APP9 (Application Segment)"
            JPEG_APP10_MARKER -> "APP10 (Application Segment)"
            JPEG_APP11_MARKER -> "APP11 (Application Segment)"
            JPEG_APP12_MARKER -> "APP12 (Application Segment, Ducky)"
            JPEG_APP13_MARKER -> "APP13 (Application Segment, IPTC)"
            JPEG_APP14_MARKER -> "APP14 (Application Segment)"
            JPEG_APP15_MARKER -> "APP15 (Application Segment)"
            /* S0F markers, first common first. */
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
            /* Other optional markers */
            COM_MARKER_1, COM_MARKER_2 -> "COM (Comment)"
            DRI_MARKER -> "DRI (Define Restart Interval)"
            RST0_MARKER -> "RST0 (Restart Marker)"
            RST1_MARKER -> "RST1 (Restart Marker)"
            RST2_MARKER -> "RST2 (Restart Marker)"
            RST3_MARKER -> "RST3 (Restart Marker)"
            RST4_MARKER -> "RST4 (Restart Marker)"
            RST5_MARKER -> "RST5 (Restart Marker)"
            RST6_MARKER -> "RST6 (Restart Marker)"
            RST7_MARKER -> "RST7 (Restart Marker)"
            DNL_MARKER -> "DNL (Define Number of Lines)"
            JPG_EXT_MARKER -> "JPG (JPEG Extensions)"
            DAC_MARKER -> "DAC (Define Arithmetic Coding)"
            DHP_MARKER -> "DHP (Define Hierarchical Progression)"
            EXP_MARKER -> "EXP (Expand Reference Component)"
            else -> marker.toShort().toHexString(HexFormat.UpperCase)
        }
}
