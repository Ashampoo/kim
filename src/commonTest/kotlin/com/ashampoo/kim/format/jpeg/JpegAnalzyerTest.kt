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

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class JpegAnalzyerTest {

    val expectedMap = mapOf<Int, JpegAnalyzeResult>(
        1 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 72),
        2 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 72),
        3 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        4 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        5 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        6 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        7 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        8 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        9 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        10 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        11 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        12 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        13 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, null),
        14 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, null),
        15 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 84),
        16 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        17 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        18 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        19 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        20 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 84),
        21 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 54),
        22 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        23 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        24 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        25 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        26 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 3446),
        27 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        28 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 66),
        29 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 114),
        30 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        31 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        32 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        33 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        34 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 72),
        35 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        36 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 72),
        37 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 114),
        38 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 102),
        39 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 66),
        40 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        41 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 54),
        42 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        43 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, null),
        44 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        45 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        46 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        47 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, null),
        48 to JpegAnalyzeResult(ByteOrder.BIG_ENDIAN, 54),
        49 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54),
        50 to JpegAnalyzeResult(ByteOrder.LITTLE_ENDIAN, 54)
    )

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testAnalyze() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val analyzeResult = JpegAnalyzer.analyze(byteReader)

            // println("$index to JpegAnalyzeResult(ByteOrder.${analyzeResult.byteOrder.name}, ${analyzeResult.orientationOffset}),")

            assertEquals(
                expected = expectedMap.get(index),
                actual = analyzeResult
            )

//            analyzeResult.orientationOffset?.let {
//
//                println(bytes.take(100).map { it.toHexString() })
//
//                println(bytes.drop(it.toInt()).take(2).map { it.toHexString() })
//
//                println(index.toString() + " = " + analyzeResult)
//            }
        }
    }
}
