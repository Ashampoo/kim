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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.model.ImageSize
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

class JpegImageParserTest {

    private val expectedImageSizesMap = mapOf(
        1 to ImageSize(4000, 2670),
        2 to ImageSize(1367, 2000),
        3 to ImageSize(2074, 2592),
        4 to ImageSize(5184, 3456),
        5 to ImageSize(3771, 2121),
        6 to ImageSize(3000, 1688),
        7 to ImageSize(6000, 3376),
        8 to ImageSize(5472, 3648),
        9 to ImageSize(5946, 3964),
        10 to ImageSize(3320, 2490),
        11 to ImageSize(4094, 2699),
        12 to ImageSize(3482, 2460),
        13 to ImageSize(1920, 1080),
        14 to ImageSize(1920, 1200),
        15 to ImageSize(5045, 4000),
        16 to ImageSize(2072, 2590),
        17 to ImageSize(3136, 3919),
        18 to ImageSize(3456, 2304),
        19 to ImageSize(2468, 4051),
        20 to ImageSize(250, 250),
        21 to ImageSize(4522, 6783),
        22 to ImageSize(3024, 4032),
        23 to ImageSize(6144, 8192),
        24 to ImageSize(3000, 2000),
        25 to ImageSize(4660, 3106),
        26 to ImageSize(2304, 1536),
        27 to ImageSize(3705, 2470),
        28 to ImageSize(2304, 1536),
        29 to ImageSize(2920, 4045),
        30 to ImageSize(3024, 4032),
        31 to ImageSize(5472, 3648),
        32 to ImageSize(3525, 1500),
        33 to ImageSize(6000, 4000),
        34 to ImageSize(4928, 3264),
        35 to ImageSize(3382, 2673),
        36 to ImageSize(4240, 2384),
        37 to ImageSize(5870, 2799),
        38 to ImageSize(2371, 1580),
        39 to ImageSize(4608, 3456),
        40 to ImageSize(4272, 2848),
        41 to ImageSize(4928, 3264),
        42 to ImageSize(3870, 3131),
        43 to ImageSize(4032, 3024),
        44 to ImageSize(6000, 4000),
        45 to ImageSize(3594, 2446),
        46 to ImageSize(4390, 2927),
        47 to ImageSize(3038, 3038),
        48 to ImageSize(4032, 3024),
        49 to ImageSize(5184, 3456),
        50 to ImageSize(6240, 4160)
    )

    @Test
    fun testGetImageSize() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            /* Use the public Kim interface to ensure it works. */
            val actualImageSize = JpegImageParser.getImageSize(byteReader)

            assertEquals(
                expected = expectedImageSizesMap.get(index),
                actual = actualImageSize,
                message = "Image size of $index is different."
            )
        }
    }
}
