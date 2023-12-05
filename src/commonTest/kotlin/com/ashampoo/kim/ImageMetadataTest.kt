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

import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

class ImageMetadataTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testToString() {

        for (index in 1..KimTestData.TEST_PHOTO_COUNT) {

            val bytes = KimTestData.getBytesOf(index)

            val metadata = Kim.readMetadata(bytes)

            val actualToString = metadata.toString().encodeToByteArray()

            val expectedToString = KimTestData.getToStringText(index)

            val equals = expectedToString.contentEquals(actualToString)

            if (!equals) {

                Path("build/photo_$index.txt")
                    .writeBytes(actualToString)

                fail("photo_$index.txt is different.")
            }
        }
    }
}
