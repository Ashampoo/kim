/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

class ByteArrayExtensionsTest {

    @Test
    fun testToHex() {

        assertEquals(
            "eff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff000102030405060708090a0b0c0d0e0f1011",
            byteArrayOf(
                -17, -16, -15, -14, -13, -12, -11,
                -10, -9, -8, -7, -6, -5, -4, -3, -2, -1,
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 14, 15, 16, 17
            ).toHex()
        )

        assertEquals(
            "000102040820407f",
            byteArrayOf(0, 1, 2, 4, 8, 32, 64, 127).toHex()
        )

        assertEquals(
            "00fffefcf8e0c080",
            byteArrayOf(-0, -1, -2, -4, -8, -32, -64, -128).toHex()
        )

        assertEquals(
            "fc9e97d362b5fef0bae291aa185877ca",
            byteArrayOf(
                -4, -98, -105, -45, 98, -75, -2, -16,
                -70, -30, -111, -86, 24, 88, 119, -54
            ).toHex()
        )

        assertEquals(
            "7c99f026ec276618401d8dd7fc2e16d6",
            byteArrayOf(
                124, -103, -16, 38, -20, 39, 102, 24,
                64, 29, -115, -41, -4, 46, 22, -42
            ).toHex()
        )

        assertEquals(
            "8b019b80f3c0d8dcfc38df6db4b2f2b1",
            byteArrayOf(
                -117, 1, -101, -128, -13, -64, -40, -36,
                -4, 56, -33, 109, -76, -78, -14, -79
            ).toHex()
        )

        assertEquals(
            "9e527212b84f2f74c793d736c7f75bd4",
            byteArrayOf(
                -98, 82, 114, 18, -72, 79, 47, 116,
                -57, -109, -41, 54, -57, -9, 91, -44
            ).toHex()
        )

        assertEquals(
            "37c5f932c1a3d7dda4bb06a3d42fa375",
            byteArrayOf(
                55, -59, -7, 50, -63, -93, -41, -35,
                -92, -69, 6, -93, -44, 47, -93, 117
            ).toHex()
        )
    }

    @Test
    fun testConvertHexStringToByteArray() {

        assertContentEquals(
            expected = byteArrayOf(0x38, 0x42, 0x49, 0x4d, 0x04, 0x04, 0x00, 0x00),
            actual = convertHexStringToByteArray("3842494d0404000")
        )

        assertContentEquals(
            expected = byteArrayOf(
                55, -59, -7, 50, -63, -93, -41, -35, -92, -69, 6, -93, -44, 47, -93, 117
            ),
            actual = convertHexStringToByteArray("37c5f932c1a3d7dda4bb06a3d42fa375")
        )
    }

    @Test
    fun testToSingleNumberHexes() {

        assertEquals(
            "0x37, 0xc5, 0xf9, 0x32, 0xc1, 0xa3, 0xd7, " +
                "0xdd, 0xa4, 0xbb, 0x06, 0xa3, 0xd4, 0x2f, 0xa3, 0x75",
            byteArrayOf(
                55, -59, -7, 50, -63, -93, -41, -35,
                -92, -69, 6, -93, -44, 47, -93, 117
            ).toSingleNumberHexes()
        )
    }
}
