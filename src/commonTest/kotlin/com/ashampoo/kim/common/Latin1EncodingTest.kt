/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.common

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Latin1EncodingTest {

    private val shortTestString = "Äußerst öffentliches Ü!"

    private val shortTestStringLatin1Bytes: ByteArray =
        byteArrayOf(
            0xC4.toByte(), 0x75, 0xDF.toByte(), 0x65, 0x72,
            0x73, 0x74, 0x20, 0xF6.toByte(), 0x66, 0x66,
            0x65, 0x6E, 0x74, 0x6C, 0x69, 0x63, 0x68, 0x65,
            0x73, 0x20, 0xDC.toByte(), 0x21
        )

    private val shortTestStringUtf8Bytes: ByteArray =
        byteArrayOf(
            0xC3.toByte(), 0x84.toByte(), 0x75, 0xC3.toByte(),
            0x9F.toByte(), 0x65, 0x72, 0x73, 0x74, 0x20,
            0xC3.toByte(), 0xB6.toByte(), 0x66, 0x66, 0x65,
            0x6E, 0x74, 0x6C, 0x69, 0x63, 0x68, 0x65, 0x73,
            0x20, 0xC3.toByte(), 0x9C.toByte(), 0x21
        )

    private val longTestStringLatin1Bytes: ByteArray =
        intArrayOf(
            0X20, 0X21, 0X22, 0X23, 0X24, 0X25, 0X26, 0X27, 0X28, 0X29, 0X2A, 0X2B, 0X2C, 0X2D,
            0X2E, 0X2F, 0X30, 0X31, 0X32, 0X33, 0X34, 0X35, 0X36, 0X37, 0X38, 0X39, 0X3A, 0X3B,
            0X3C, 0X3D, 0X3E, 0X3F, 0X40, 0X41, 0X42, 0X43, 0X44, 0X45, 0X46, 0X47, 0X48, 0X49,
            0X4A, 0X4B, 0X4C, 0X4D, 0X4E, 0X4F, 0X50, 0X51, 0X52, 0X53, 0X54, 0X55, 0X56, 0X57,
            0X58, 0X59, 0X5A, 0X5B, 0X5C, 0X5D, 0X5E, 0X5F, 0X60, 0X61, 0X62, 0X63, 0X64, 0X65,
            0X66, 0X67, 0X68, 0X69, 0X6A, 0X6B, 0X6C, 0X6D, 0X6E, 0X6F, 0X70, 0X71, 0X72, 0X73,
            0X74, 0X75, 0X76, 0X77, 0X78, 0X79, 0X7A, 0X7B, 0X7C, 0X7D, 0X7E, 0XA1, 0XA2, 0XA3,
            0XA4, 0XA5, 0XA6, 0XA7, 0XA8, 0XA9, 0XAA, 0XAB, 0XAC, 0XAE, 0XAF, 0XB0, 0XB1, 0XB2,
            0XB3, 0XB4, 0XB5, 0XB6, 0XB7, 0XB8, 0XB9, 0XBA, 0XBB, 0XBC, 0XBD, 0XBE, 0XBF, 0XC0,
            0XC1, 0XC2, 0XC3, 0XC4, 0XC5, 0XC6, 0XC7, 0XC8, 0XC9, 0XCA, 0XCB, 0XCC, 0XCD, 0XCE,
            0XCF, 0XD0, 0XD1, 0XD2, 0XD3, 0XD4, 0XD5, 0XD6, 0XD7, 0XD8, 0XD9, 0XDA, 0XDB, 0XDC,
            0XDD, 0XDE, 0XDF, 0XE0, 0XE1, 0XE2, 0XE3, 0XE4, 0XE5, 0XE6, 0XE7, 0XE8, 0XE9, 0XEA,
            0XEB, 0XEC, 0XED, 0XEE, 0XEF, 0XF0, 0XF1, 0XF2, 0XF3, 0XF4, 0XF5, 0XF6, 0XF7, 0XF8,
            0XF9, 0XFA, 0XFB, 0XFC, 0XFD, 0XFE, 0XFF
        ).map { it.toByte() }.toByteArray()

    private val longTestStringUtf8Bytes: ByteArray =
        intArrayOf(
            0X20, 0X21, 0X22, 0X23, 0X24, 0X25, 0X26, 0X27, 0X28, 0X29, 0X2A, 0X2B, 0X2C, 0X2D,
            0X2E, 0X2F, 0X30, 0X31, 0X32, 0X33, 0X34, 0X35, 0X36, 0X37, 0X38, 0X39, 0X3A, 0X3B,
            0X3C, 0X3D, 0X3E, 0X3F, 0X40, 0X41, 0X42, 0X43, 0X44, 0X45, 0X46, 0X47, 0X48, 0X49,
            0X4A, 0X4B, 0X4C, 0X4D, 0X4E, 0X4F, 0X50, 0X51, 0X52, 0X53, 0X54, 0X55, 0X56, 0X57,
            0X58, 0X59, 0X5A, 0X5B, 0X5C, 0X5D, 0X5E, 0X5F, 0X60, 0X61, 0X62, 0X63, 0X64, 0X65,
            0X66, 0X67, 0X68, 0X69, 0X6A, 0X6B, 0X6C, 0X6D, 0X6E, 0X6F, 0X70, 0X71, 0X72, 0X73,
            0X74, 0X75, 0X76, 0X77, 0X78, 0X79, 0X7A, 0X7B, 0X7C, 0X7D, 0X7E, 0XC2, 0XA1, 0XC2,
            0XA2, 0XC2, 0XA3, 0XC2, 0XA4, 0XC2, 0XA5, 0XC2, 0XA6, 0XC2, 0XA7, 0XC2, 0XA8, 0XC2,
            0XA9, 0XC2, 0XAA, 0XC2, 0XAB, 0XC2, 0XAC, 0XC2, 0XAE, 0XC2, 0XAF, 0XC2, 0XB0, 0XC2,
            0XB1, 0XC2, 0XB2, 0XC2, 0XB3, 0XC2, 0XB4, 0XC2, 0XB5, 0XC2, 0XB6, 0XC2, 0XB7, 0XC2,
            0XB8, 0XC2, 0XB9, 0XC2, 0XBA, 0XC2, 0XBB, 0XC2, 0XBC, 0XC2, 0XBD, 0XC2, 0XBE, 0XC2,
            0XBF, 0XC3, 0X80, 0XC3, 0X81, 0XC3, 0X82, 0XC3, 0X83, 0XC3, 0X84, 0XC3, 0X85, 0XC3,
            0X86, 0XC3, 0X87, 0XC3, 0X88, 0XC3, 0X89, 0XC3, 0X8A, 0XC3, 0X8B, 0XC3, 0X8C, 0XC3,
            0X8D, 0XC3, 0X8E, 0XC3, 0X8F, 0XC3, 0X90, 0XC3, 0X91, 0XC3, 0X92, 0XC3, 0X93, 0XC3,
            0X94, 0XC3, 0X95, 0XC3, 0X96, 0XC3, 0X97, 0XC3, 0X98, 0XC3, 0X99, 0XC3, 0X9A, 0XC3,
            0X9B, 0XC3, 0X9C, 0XC3, 0X9D, 0XC3, 0X9E, 0XC3, 0X9F, 0XC3, 0XA0, 0XC3, 0XA1, 0XC3,
            0XA2, 0XC3, 0XA3, 0XC3, 0XA4, 0XC3, 0XA5, 0XC3, 0XA6, 0XC3, 0XA7, 0XC3, 0XA8, 0XC3,
            0XA9, 0XC3, 0XAA, 0XC3, 0XAB, 0XC3, 0XAC, 0XC3, 0XAD, 0XC3, 0XAE, 0XC3, 0XAF, 0XC3,
            0XB0, 0XC3, 0XB1, 0XC3, 0XB2, 0XC3, 0XB3, 0XC3, 0XB4, 0XC3, 0XB5, 0XC3, 0XB6, 0XC3,
            0XB7, 0XC3, 0XB8, 0XC3, 0XB9, 0XC3, 0XBA, 0XC3, 0XBB, 0XC3, 0XBC, 0XC3, 0XBD, 0XC3,
            0XBE, 0XC3, 0XBF
        ).map { it.toByte() }.toByteArray()

    private val longTestString = " !\"#$%&'()*+,-./0123456789:;<=>?@" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~" +
        "¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ" +
        "×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ"

    @Test
    fun testEncodeToLatin1Bytes() {

        assertContentEquals(
            expected = shortTestStringLatin1Bytes,
            actual = shortTestString.encodeToLatin1Bytes()
        )

        assertContentEquals(
            expected = longTestStringLatin1Bytes,
            actual = longTestString.encodeToLatin1Bytes()
        )
    }

    @Test
    fun testEncodeToUtf8Bytes() {

        assertContentEquals(
            expected = shortTestStringUtf8Bytes,
            actual = shortTestString.encodeToByteArray()
        )

        assertContentEquals(
            expected = longTestStringUtf8Bytes,
            actual = longTestString.encodeToByteArray()
        )
    }

    @Test
    fun testDecodeLatin1BytesToString() {

        assertEquals(
            expected = shortTestString,
            actual = shortTestStringLatin1Bytes.decodeLatin1BytesToString()
        )

        assertEquals(
            expected = longTestString,
            actual = longTestStringLatin1Bytes.decodeLatin1BytesToString()
        )
    }

    /* Just for comparison the UTF-8 bytes. */
    @Test
    fun testDecodeUtf8BytesToString() {

        assertEquals(
            expected = shortTestString,
            actual = shortTestStringUtf8Bytes.decodeToString()
        )

        assertEquals(
            expected = longTestString,
            actual = longTestStringUtf8Bytes.decodeToString()
        )
    }
}
