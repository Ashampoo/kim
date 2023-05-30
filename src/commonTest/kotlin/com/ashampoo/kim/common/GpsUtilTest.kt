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
import kotlin.test.assertNull

class GpsUtilTest {

    @Test
    fun testDmsToDecimal() {

        /* Valid inputs */
        assertEquals(40.72568166666667, GpsUtil.dmsToDecimal("40,43.5409N"))
        assertEquals(-74.08378666666667, GpsUtil.dmsToDecimal("74,5.0272W"))
        assertEquals(-33.865144111111114, GpsUtil.dmsToDecimal("33,51,54.5188S"))
        assertEquals(151.20929999999998, GpsUtil.dmsToDecimal("151,12,33.48E"))

        /* Null island */
        assertEquals(0.0, GpsUtil.dmsToDecimal("00,00N"))
        assertEquals(0.0, GpsUtil.dmsToDecimal("00,00.0000N"))

        /* Invalid inputs */
        assertNull(GpsUtil.dmsToDecimal(null))
        assertNull(GpsUtil.dmsToDecimal(""))
        assertNull(GpsUtil.dmsToDecimal("40N"))
        assertNull(GpsUtil.dmsToDecimal("74W"))
        assertNull(GpsUtil.dmsToDecimal("40,43.5409"))
        assertNull(GpsUtil.dmsToDecimal("invalid input"))
        assertNull(GpsUtil.dmsToDecimal("40,43,32.454"))
        assertNull(GpsUtil.dmsToDecimal("40,43.5409X"))
        assertNull(GpsUtil.dmsToDecimal("N40,43.5409"))
        assertNull(GpsUtil.dmsToDecimal("40.72568166666667N"))
    }

    @Test
    fun testDecimalLatitudeToDDM() {

        assertEquals("53,13.1635N", GpsUtil.decimalLatitudeToDDM(53.219391))
        assertEquals("5,47.7178S", GpsUtil.decimalLatitudeToDDM(-5.795296))
    }

    @Test
    fun testDecimalLongitudeToDDM() {

        assertEquals("8,14.3797E", GpsUtil.decimalLongitudeToDDM(8.239661))
        assertEquals("64,26.6986W", GpsUtil.decimalLongitudeToDDM(-64.444976))
    }
}
