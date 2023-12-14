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
import kotlin.test.assertEquals

class Latin1EncodingTest {

    @Test
    fun testDecodeLatin1BytesToString() {

        /* ISO 8859-1 bytes */
        assertEquals(
            "Äußerst öffentliches Ü!",
            byteArrayOf(
                0xC4.toByte(), 0x75, 0xDF.toByte(), 0x65, 0x72,
                0x73, 0x74, 0x20, 0xF6.toByte(), 0x66, 0x66,
                0x65, 0x6E, 0x74, 0x6C, 0x69, 0x63, 0x68, 0x65,
                0x73, 0x20, 0xDC.toByte(), 0x21
            ).decodeLatin1BytesToString()
        )

        /* Just for comparison the UTF-8 bytes. */
        assertEquals(
            "Äußerst öffentliches Ü!",
            byteArrayOf(
                0xC3.toByte(), 0x84.toByte(), 0x75, 0xC3.toByte(),
                0x9F.toByte(), 0x65, 0x72, 0x73, 0x74, 0x20,
                0xC3.toByte(), 0xB6.toByte(), 0x66, 0x66, 0x65,
                0x6E, 0x74, 0x6C, 0x69, 0x63, 0x68, 0x65, 0x73,
                0x20, 0xC3.toByte(), 0x9C.toByte(), 0x21
            ).decodeToString()
        )
    }
}
