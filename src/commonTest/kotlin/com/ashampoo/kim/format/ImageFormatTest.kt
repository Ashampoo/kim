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
package com.ashampoo.kim.format

import com.ashampoo.kim.model.ImageFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ImageFormatTest {

    @Test
    fun testByMimeType() {

        assertNull(ImageFormat.byMimeType("invalid"))

        assertEquals(
            expected = ImageFormat.JPEG,
            actual = ImageFormat.byMimeType("image/jpeg")
        )

        assertEquals(
            expected = ImageFormat.GIF,
            actual = ImageFormat.byMimeType("image/gif")
        )

        assertEquals(
            expected = ImageFormat.PNG,
            actual = ImageFormat.byMimeType("image/png")
        )

        assertEquals(
            expected = ImageFormat.WEBP,
            actual = ImageFormat.byMimeType("image/webp")
        )

        assertEquals(
            expected = ImageFormat.TIFF,
            actual = ImageFormat.byMimeType("image/tiff")
        )

        assertEquals(
            expected = ImageFormat.HEIC,
            actual = ImageFormat.byMimeType("image/heic")
        )

        assertEquals(
            expected = ImageFormat.CR2,
            actual = ImageFormat.byMimeType("image/x-canon-cr2")
        )

        /* OneDrive returns this wrong mime type. */
        assertEquals(
            expected = ImageFormat.CR2,
            actual = ImageFormat.byMimeType("image/CR2")
        )

        assertEquals(
            expected = ImageFormat.RAF,
            actual = ImageFormat.byMimeType("image/x-fuji-raf")
        )
    }

    @Test
    fun testByUniformTypeIdentifier() {

        assertNull(ImageFormat.byUniformTypeIdentifier("invalid"))

        assertEquals(
            expected = ImageFormat.JPEG,
            actual = ImageFormat.byUniformTypeIdentifier("public.jpeg")
        )

        assertEquals(
            expected = ImageFormat.GIF,
            actual = ImageFormat.byUniformTypeIdentifier("com.compuserve.gif")
        )

        assertEquals(
            expected = ImageFormat.PNG,
            actual = ImageFormat.byUniformTypeIdentifier("public.png")
        )

        assertEquals(
            expected = ImageFormat.WEBP,
            actual = ImageFormat.byUniformTypeIdentifier("public.webp")
        )

        assertEquals(
            expected = ImageFormat.TIFF,
            actual = ImageFormat.byUniformTypeIdentifier("public.tiff")
        )

        assertEquals(
            expected = ImageFormat.HEIC,
            actual = ImageFormat.byUniformTypeIdentifier("public.heic")
        )

        assertEquals(
            expected = ImageFormat.CR2,
            actual = ImageFormat.byUniformTypeIdentifier("com.canon.cr2-raw-image")
        )

        assertEquals(
            expected = ImageFormat.RAF,
            actual = ImageFormat.byUniformTypeIdentifier("com.fuji.raw-image")
        )
    }

    @Test
    fun testByFileNameExtension() {

        assertNull(ImageFormat.byFileNameExtension("invalid"))

        assertEquals(
            expected = ImageFormat.JPEG,
            actual = ImageFormat.byFileNameExtension("image.jpeg")
        )

        assertEquals(
            expected = ImageFormat.JPEG,
            actual = ImageFormat.byFileNameExtension("image.jpg")
        )

        assertEquals(
            expected = ImageFormat.JPEG,
            actual = ImageFormat.byFileNameExtension("image.JPG")
        )

        assertEquals(
            expected = ImageFormat.GIF,
            actual = ImageFormat.byFileNameExtension("image.gif")
        )

        assertEquals(
            expected = ImageFormat.PNG,
            actual = ImageFormat.byFileNameExtension("image.png")
        )

        assertEquals(
            expected = ImageFormat.WEBP,
            actual = ImageFormat.byFileNameExtension("image.webp")
        )

        assertEquals(
            expected = ImageFormat.TIFF,
            actual = ImageFormat.byFileNameExtension("image.tif")
        )

        assertEquals(
            expected = ImageFormat.TIFF,
            actual = ImageFormat.byFileNameExtension("image.tiff")
        )

        assertEquals(
            expected = ImageFormat.HEIC,
            actual = ImageFormat.byFileNameExtension("image.heic")
        )

        assertEquals(
            expected = ImageFormat.CR2,
            actual = ImageFormat.byFileNameExtension("image.cr2")
        )

        assertEquals(
            expected = ImageFormat.RAF,
            actual = ImageFormat.byFileNameExtension("image.raf")
        )
    }
}
