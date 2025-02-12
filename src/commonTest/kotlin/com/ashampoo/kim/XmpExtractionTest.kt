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
package com.ashampoo.kim

import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class XmpExtractionTest {

    val indicesWithoutXmp: Set<Int> = setOf(
        2, 18, 20, 23, 30, 42, 43, 44, 45, 46, 47, 48,
        KimTestData.HEIC_TEST_IMAGE_INDEX,
        KimTestData.GIF_TEST_IMAGE_INDEX,
        KimTestData.NEF_TEST_IMAGE_INDEX,
        KimTestData.ARW_TEST_IMAGE_INDEX,
        KimTestData.RW2_TEST_IMAGE_INDEX,
        KimTestData.ORF_TEST_IMAGE_INDEX,
        KimTestData.HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX,
        KimTestData.GEOTIFF_PIXEL_SCALING_INDEX,
        KimTestData.GEOTIFF_AFFINE_TRANSFORM_INDEX
    )

    // TODO Support these files as they have XMP
    val indicesUnsupported: Set<Int> = setOf(
        59, 78, 79, 80
    )

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractXmp() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in 1..KimTestData.TEST_PHOTO_COUNT) {

            /* Skip files without embedded XMP */
            if (indicesWithoutXmp.contains(index) || indicesUnsupported.contains(index))
                continue

            val bytes = KimTestData.getBytesOf(index)

            val actualXmp = Kim.readMetadata(bytes)?.xmp

            assertNotNull(actualXmp, "File #$index has no XMP.")

            val actualXmpBytes = actualXmp.encodeToByteArray()

            val expectedXmp = KimTestData.getOriginalXmp(index)

            val equals = expectedXmp.contentEquals(actualXmpBytes)

            if (!equals) {

                Path("build/photo_$index.xmp")
                    .writeBytes(actualXmpBytes)

                fail("Photo $index has not the expected bytes!")
            }
        }
    }
}
