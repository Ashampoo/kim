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
package com.ashampoo.kim.format.webp.chunk

import com.ashampoo.kim.common.convertHexStringToByteArray
import com.ashampoo.kim.format.webp.WebPConstants
import com.ashampoo.kim.model.ImageSize
import kotlin.test.Test
import kotlin.test.assertEquals

class WebPChunkVP8XTest {

    /**
     * Tests parsing headers of real WebP files.
     */
    @Test
    fun testParse() {

        assertEquals(
            expected = "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=true " +
                "hasAlpha=false " +
                "hasExif=true " +
                "hasXmp=true " +
                "hasAnimation=false " +
                "imageSize=5184 x 3456",
            actual = WebPChunkVP8X(
                convertHexStringToByteArray("2c0000003f14007f0d00")
            ).toString()
        )
    }

    /**
     *
     */
    @Test
    fun testCreateBytes() {

        assertEquals(
            expected = "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=false " +
                "hasAlpha=false " +
                "hasExif=false " +
                "hasXmp=false " +
                "hasAnimation=false " +
                "imageSize=1 x 1",
            actual = WebPChunkVP8X(
                bytes = WebPChunkVP8X.createBytes(
                    hasIcc = false,
                    hasAlpha = false,
                    hasExif = false,
                    hasXmp = false,
                    hasAnimation = false,
                    imageSize = ImageSize(
                        width = 1,
                        height = 1
                    )
                )
            ).toString()
        )

        assertEquals(
            expected = "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=true " +
                "hasAlpha=true " +
                "hasExif=true " +
                "hasXmp=true " +
                "hasAnimation=true " +
                "imageSize=512 x 512",
            actual = WebPChunkVP8X(
                bytes = WebPChunkVP8X.createBytes(
                    hasIcc = true,
                    hasAlpha = true,
                    hasExif = true,
                    hasXmp = true,
                    hasAnimation = true,
                    imageSize = ImageSize(
                        width = 512,
                        height = 512
                    )
                )
            ).toString()
        )

        assertEquals(
            expected = "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=false " +
                "hasAlpha=true " +
                "hasExif=true " +
                "hasXmp=false " +
                "hasAnimation=false " +
                "imageSize=16383 x 16383",
            actual = WebPChunkVP8X(
                bytes = WebPChunkVP8X.createBytes(
                    hasIcc = false,
                    hasAlpha = true,
                    hasExif = true,
                    hasXmp = false,
                    hasAnimation = false,
                    imageSize = ImageSize(
                        width = WebPConstants.MAX_SIDE_LENGTH,
                        height = WebPConstants.MAX_SIDE_LENGTH
                    )
                )
            ).toString()
        )

        /*
         * Test random combinations
         */
        repeat(50) {

            val hasIcc = (0..1).random() == 1
            val hasAlpha = (0..1).random() == 1
            val hasExif = (0..1).random() == 1
            val hasXmp = (0..1).random() == 1
            val hasAnimation = (0..1).random() == 1
            val imageSize = ImageSize(
                width = (1..WebPConstants.MAX_SIDE_LENGTH).random(),
                height = (1..WebPConstants.MAX_SIDE_LENGTH).random()
            )

            val expectedString = "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=$hasIcc " +
                "hasAlpha=$hasAlpha " +
                "hasExif=$hasExif " +
                "hasXmp=$hasXmp " +
                "hasAnimation=$hasAnimation " +
                "imageSize=${imageSize.width} x ${imageSize.height}"

            assertEquals(
                expected = expectedString,
                actual = WebPChunkVP8X(
                    bytes = WebPChunkVP8X.createBytes(
                        hasIcc = hasIcc,
                        hasAlpha = hasAlpha,
                        hasExif = hasExif,
                        hasXmp = hasXmp,
                        hasAnimation = hasAnimation,
                        imageSize = imageSize
                    )
                ).toString()
            )
        }
    }
}
