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
package com.ashampoo.kim.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PhotoRatingTest {

    private val nullString: String? = null

    @Test
    fun testOfInt() {

        assertNull(PhotoRating.Companion.of(-2))
        assertNull(PhotoRating.Companion.of(6))

        assertEquals(
            PhotoRating.REJECTED,
            PhotoRating.Companion.of(-1)
        )

        assertEquals(
            PhotoRating.UNRATED,
            PhotoRating.Companion.of(0)
        )

        assertEquals(
            PhotoRating.ONE_STAR,
            PhotoRating.Companion.of(1)
        )

        assertEquals(
            PhotoRating.TWO_STARS,
            PhotoRating.Companion.of(2)
        )

        assertEquals(
            PhotoRating.THREE_STARS,
            PhotoRating.Companion.of(3)
        )

        assertEquals(
            PhotoRating.FOUR_STARS,
            PhotoRating.Companion.of(4)
        )

        assertEquals(
            PhotoRating.FIVE_STARS,
            PhotoRating.Companion.of(5)
        )
    }

    @Test
    fun testOfString() {

        assertNull(PhotoRating.Companion.of(nullString))
        assertNull(PhotoRating.Companion.of(""))
        assertNull(PhotoRating.Companion.of("   "))
        assertNull(PhotoRating.Companion.of("hello"))
        assertNull(PhotoRating.Companion.of("-2"))
        assertNull(PhotoRating.Companion.of("6"))

        assertEquals(
            PhotoRating.REJECTED,
            PhotoRating.Companion.of("-1")
        )

        assertEquals(
            PhotoRating.UNRATED,
            PhotoRating.Companion.of("0")
        )

        assertEquals(
            PhotoRating.ONE_STAR,
            PhotoRating.Companion.of("1")
        )

        assertEquals(
            PhotoRating.TWO_STARS,
            PhotoRating.Companion.of("2")
        )

        assertEquals(
            PhotoRating.THREE_STARS,
            PhotoRating.Companion.of("3")
        )

        assertEquals(
            PhotoRating.FOUR_STARS,
            PhotoRating.Companion.of("4")
        )

        assertEquals(
            PhotoRating.FIVE_STARS,
            PhotoRating.Companion.of("5")
        )
    }
}
