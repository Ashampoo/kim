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
package com.ashampoo.kim.format.webp.chunk

import kotlin.test.Test
import kotlin.test.assertEquals

class WebPChunkVP8XTest {

    val testBytes = byteArrayOf(
        0x2C, 0x00, 0x00, 0x00, 0x3F, 0x14, 0x00, 0x7F, 0x0D, 0x00
    )

    @Test
    fun testParse() {

        assertEquals(
            "WebPChunk 'VP8X' (10 bytes) " +
                "hasIcc=true " +
                "hasAlpha=false " +
                "hasExif=true " +
                "hasXmp=true " +
                "hasAnimation=false " +
                "imageSize=5184 x 3456",
            WebPChunkVP8X(
                testBytes
            ).toString()
        )
    }
}
