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
import kotlin.test.assertEquals

@Suppress("UnderscoresInNumericLiterals")
class ByteConversionsTest {

    private val testBytes = byteArrayOf(-128, 0, 127, 64, -48, 110, 8, -4)

    @Test
    fun testToUInt8() {

        assertEquals(128, testBytes[0].toUInt8())
        assertEquals(0, testBytes[1].toUInt8())
        assertEquals(127, testBytes[2].toUInt8())
        assertEquals(64, testBytes[3].toUInt8())
        assertEquals(208, testBytes[4].toUInt8())
        assertEquals(110, testBytes[5].toUInt8())
        assertEquals(8, testBytes[6].toUInt8())
        assertEquals(252, testBytes[7].toUInt8())
    }
}
