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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

class JpegOrientationOffsetFinderTest {

    val expectedMap = mapOf<Int, JpegOrientationOffset>(
        1 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 72),
        2 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 72),
        15 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 84),
        19 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        20 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 84),
        21 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 54),
        25 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        26 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 3446),
        28 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 66),
        29 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 114),
        31 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        34 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 72),
        36 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 72),
        37 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 114),
        38 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 102),
        39 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 66),
        40 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        41 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 54),
        44 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        45 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        48 to JpegOrientationOffset(ByteOrder.BIG_ENDIAN, 54),
        49 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54),
        50 to JpegOrientationOffset(ByteOrder.LITTLE_ENDIAN, 54)
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

            val result = JpegOrientationOffsetFinder.findOrientationOffset(byteReader)

            assertEquals(
                expected = expectedMap.get(index),
                actual = result
            )
        }
    }
}
