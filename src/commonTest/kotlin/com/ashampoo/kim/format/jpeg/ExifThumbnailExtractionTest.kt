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
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.fail

class ExifThumbnailExtractionTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testExtractJpegThumbnail() {

        for (index in KimTestData.photoIdsWithExifThumbnail) {

            /* Thumbnail directory IFD1 is corrupt. */
            if (index == 21)
                continue

            // TODO Thumbnail is not detected
            if (index == 64 || index == 65)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val metadata = Kim.readMetadata(bytes)

            val actualThumbnailBytes = metadata?.getExifThumbnailBytes()

            assertNotNull(actualThumbnailBytes, "Photo $index has no thumbnail bytes.")

            val expectedThumbnailBytes = KimTestData.getExifThumbnailBytesOf(index)

            val equals = expectedThumbnailBytes.contentEquals(actualThumbnailBytes)

            if (!equals) {

                Path("build/photo_${index}_exifthumb.jpg")
                    .writeBytes(actualThumbnailBytes)

                fail("Photo $index has not the expected bytes!")
            }
        }
    }
}
