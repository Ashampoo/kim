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
package com.ashampoo.kim

import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlinx.io.files.sink
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class XmpExtractionTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractXmp() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in 1..53) {

            /* Skip files without embedded XMP */
            if (index == 2 || index == 20 || index == 48)
                continue

            // TODO Handle broken file (bad IFD1)
            if (index == 21)
                continue

            val bytes = KimTestData.getHeaderBytesOf(index)

            val actualXmp = Kim.readMetadata(bytes)?.xmp

            assertNotNull(actualXmp, "File #$index has no XMP.")

            val actualXmpBytes = actualXmp.encodeToByteArray()

            val expectedXmp = KimTestData.getOriginalXmp(index)

            val equals = expectedXmp.contentEquals(actualXmpBytes)

            if (!equals) {

                Path("build/photo_$index.xmp").sink().use { it.write(actualXmpBytes) }

                fail("Photo $index has not the expected bytes!")
            }
        }
    }
}
