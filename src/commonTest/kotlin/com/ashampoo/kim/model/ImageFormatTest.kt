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
package com.ashampoo.kim.model

import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

class ImageFormatTest {

    @Test
    fun testDetect() {

        for (index in 1..KimTestData.TEST_PHOTO_COUNT) {

            val expectedFileType = when {
                index <= KimTestData.HIGHEST_JPEG_INDEX -> ImageFormat.JPEG
                index == KimTestData.GIF_TEST_IMAGE_INDEX -> ImageFormat.GIF
                index == KimTestData.WEBP_TEST_IMAGE_INDEX -> ImageFormat.WEBP
                index == KimTestData.PNG_TEST_IMAGE_INDEX -> ImageFormat.PNG
                index == KimTestData.CR2_TEST_IMAGE_INDEX -> ImageFormat.CR2
                index == KimTestData.RAF_TEST_IMAGE_INDEX -> ImageFormat.RAF
                index == KimTestData.TIFF_NONE_TEST_IMAGE_INDEX -> ImageFormat.TIFF
                index == KimTestData.TIFF_ZIP_TEST_IMAGE_INDEX -> ImageFormat.TIFF
                index == KimTestData.TIFF_LZW_TEST_IMAGE_INDEX -> ImageFormat.TIFF
                index == KimTestData.PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX -> ImageFormat.PNG
                index == KimTestData.PNG_GIMP_TEST_IMAGE_INDEX -> ImageFormat.PNG
                else -> null
            }

            val bytes = KimTestData.getBytesOf(index)

            val actualFileType = ImageFormat.detect(bytes)

            assertEquals(expectedFileType, actualFileType, "Photo $index has a different type.")
        }
    }
}
