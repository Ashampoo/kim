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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

class JpegOrientationOffsetFinderTest {

    val expectedMap: Map<Int, Long> = mapOf<Int, Long>(
        1 to 72,
        2 to 72,
        15 to 85,
        20 to 84,
        21 to 55,
        23 to 43,
        25 to 7068,
        26 to 3447,
        28 to 66,
        29 to 114,
        30 to 54,
        31 to 54,
        34 to 72,
        36 to 73,
        37 to 114,
        38 to 102,
        39 to 66,
        40 to 54,
        41 to 54,
        42 to 66,
        43 to 66,
        46 to 49,
        48 to 55,
        49 to 54,
        50 to 54
    )

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testFindOrientationOffset() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val orientationOffset = JpegOrientationOffsetFinder.findOrientationOffset(byteReader)

            assertEquals(
                expected = expectedMap.get(index),
                actual = orientationOffset
            )
        }
    }
}
