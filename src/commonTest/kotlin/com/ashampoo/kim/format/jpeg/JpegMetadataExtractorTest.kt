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

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

class JpegMetadataExtractorTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractMetadataBytes() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            /* Use the public Kim interface to ensure it works. */
            val actualMetadataBytes = Kim.extractMetadataBytes(byteReader).second

            val expectedMetadataBytes = KimTestData.getHeaderBytesOf(index)

            val equals = expectedMetadataBytes.contentEquals(actualMetadataBytes)

            if (!equals) {

                Path("build/photo_${index}_header.jpg")
                    .writeBytes(actualMetadataBytes)

                fail("Photo $index has not the expected bytes!")
            }
        }
    }
}
