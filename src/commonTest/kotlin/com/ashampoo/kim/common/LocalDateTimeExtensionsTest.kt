/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ashampoo.kim.common

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTimeExtensionsTest {

    @Test
    fun testToExifDateString() {

        assertEquals(
            expected = "2023:07:12 13:37:42",
            actual = LocalDateTime(
                LocalDate(2023, 7, 12),
                LocalTime(13, 37, 42)
            ).toExifDateString()
        )

        assertEquals(
            expected = "2023:08:01 08:05:00",
            actual = LocalDateTime(
                LocalDate(2023, 8, 1),
                LocalTime(8, 5, 0)
            ).toExifDateString()
        )
    }
}
