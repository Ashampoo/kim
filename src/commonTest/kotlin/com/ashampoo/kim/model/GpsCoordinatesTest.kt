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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GpsCoordinatesTest {

    @Test
    fun testLatLongString() {

        assertEquals(
            expected = "53.21939, 8.23966",
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).latLongString
        )
    }

    @Test
    fun testPreciseCoordinates() {

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.21939,
                longitude = 8.23966
            ),
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).toPreciseCoordinates()
        )
    }

    @Test
    fun testCoarseCoordinates() {

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219,
                longitude = 8.24
            ),
            actual = GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).toCoarseCoordinates()
        )
    }

    @Test
    fun testIsNullIsland() {

        assertTrue(
            GpsCoordinates(
                latitude = 0.0,
                longitude = 0.0
            ).isNullIsland()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).isNullIsland()
        )
    }

    @Test
    fun testIsValid() {

        /* Valid values */

        assertTrue(
            GpsCoordinates(
                latitude = 53.2193897123,
                longitude = 8.2396611123
            ).isValid()
        )

        /* Edge values */

        assertTrue(
            GpsCoordinates(
                latitude = 90.0,
                longitude = 180.0
            ).isValid()
        )

        assertTrue(
            GpsCoordinates(
                latitude = -90.0,
                longitude = -180.0
            ).isValid()
        )

        /* Invalid values */

        assertFalse(
            GpsCoordinates(
                latitude = 91.0,
                longitude = 180.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = -90.0,
                longitude = -181.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 0.0,
                longitude = 200.0
            ).isValid()
        )

        assertFalse(
            GpsCoordinates(
                latitude = 200.0,
                longitude = 200.0
            ).isValid()
        )
    }

    @Test
    fun testParseEmptyValues() {

        assertNull(GpsCoordinates.parse(null))
        assertNull(GpsCoordinates.parse(""))
        assertNull(GpsCoordinates.parse("    "))
        assertNull(GpsCoordinates.parse("hello"))
    }

    @Test
    fun testParseValidValues() {

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse("53.219391,8.239661")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse("53.219391, 8.239661")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 53.219391,
                longitude = 8.239661
            ),
            actual = GpsCoordinates.parse(" 53.219391, 8.239661 ")
        )

        /* Edge values */

        assertEquals(
            expected = GpsCoordinates(
                latitude = -90.0,
                longitude = -180.0
            ),
            actual = GpsCoordinates.parse("-90.0, -180.0")
        )

        assertEquals(
            expected = GpsCoordinates(
                latitude = 90.0,
                longitude = 180.0
            ),
            actual = GpsCoordinates.parse("90.0, 180.0")
        )
    }

    @Test
    fun testParseInvalidValues() {

        assertNull(GpsCoordinates.parse("91.0, 45.0"))
        assertNull(GpsCoordinates.parse("60.0, 190.0"))
    }
}
