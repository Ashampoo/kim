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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.format.png.chunks.PngChunkItxt
import com.ashampoo.kim.format.png.chunks.PngChunkZtxt
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PngImageParserTest {

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testExtractPngText() {

        val index = KimTestData.PNG_TEST_IMAGE_INDEX

        val bytes = KimTestData.getHeaderBytesOf(index)

        val chunks = PngImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(PngChunkType.IHDR, PngChunkType.TEXT, PngChunkType.ZTXT, PngChunkType.ITXT, PngChunkType.EXIF)
        )

        assertNotNull(chunks)
        assertEquals(4, chunks.size)

        val exifTxtChunk = chunks.get(1) as? PngChunkZtxt
        val iptcTxtChunk = chunks.get(2) as? PngChunkZtxt
        val xmpTxtChunk = chunks.get(3) as? PngChunkItxt

        assertNotNull(exifTxtChunk)
        assertEquals("Raw profile type exif", exifTxtChunk.keyword)

        assertNotNull(iptcTxtChunk)
        assertEquals("Raw profile type iptc", iptcTxtChunk.keyword)

        assertNotNull(xmpTxtChunk)
        assertEquals("XML:com.adobe.xmp", xmpTxtChunk.keyword)

        val expectedExif = KimTestData.getHeaderTextFile(index, "exif")
        val actualExif = exifTxtChunk.text

        assertEquals(expectedExif, actualExif, "EXIF is different.")

        val expectedIptc = KimTestData.getHeaderTextFile(index, "iptc")
        val actualIptc = iptcTxtChunk.text

        assertEquals(expectedIptc, actualIptc, "IPTC is different.")

        val expectedXmp = KimTestData.getHeaderTextFile(index, "xmp")
        val actualXmp = xmpTxtChunk.text

        assertEquals(expectedXmp, actualXmp, "XMP is different.")
    }
}
