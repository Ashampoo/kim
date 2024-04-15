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
package com.ashampoo.kim.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExifDateUtilTest {

    @Test
    fun testConvertExifDateToIso8601Date() {

        /* Only date. */
        assertEquals(
            "2021-12-06",
            convertExifDateToIso8601Date("2021:12:06")
        )

        /* Date with time. */
        assertEquals(
            "2021-12-06T05:27:33",
            convertExifDateToIso8601Date("2021:12:06 05:27:33")
        )

        /* Date with time, but zero seconds. */
        assertEquals(
            "2023-05-12T18:04:00",
            convertExifDateToIso8601Date("2023:05:12 18:04:  ")
        )

        /* Variant of the test before */
        assertEquals(
            "2023-05-12T18:04:00",
            convertExifDateToIso8601Date("2023:05:12 18:04: 0")
        )

        /* Empty string. */
        assertFailsWith<IllegalArgumentException>("Should not accept empty string.") {
            convertExifDateToIso8601Date("                   ")
        }

        /* Blanks string. */
        assertFailsWith<IllegalArgumentException>("Should not accept only blanks.") {
            convertExifDateToIso8601Date("    -  -  T  :  :  ")
        }

        /* Blanks string + zero seconds. */
        assertFailsWith<IllegalArgumentException>("Should not accept only blanks.") {
            convertExifDateToIso8601Date("    -  -  T  :  :00")
        }

        /* Zero string. */
        assertFailsWith<IllegalArgumentException>("Should not accept only zeros.") {
            convertExifDateToIso8601Date("0000:00:00 00:00:00")
        }
    }
}
