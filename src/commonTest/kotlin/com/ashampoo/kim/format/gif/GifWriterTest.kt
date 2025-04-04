/*
 * Copyright 2025 Ramon Bouckaert
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

package com.ashampoo.kim.format.gif

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class GifWriterTest {

    /* language=XML */
    private val expectedXmp = """
        <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
          <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
            <rdf:Description rdf:about=""
                xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                xmlns:exif="http://ns.adobe.com/exif/1.0/"
              exif:DateTimeOriginal="2020-10-05T13:37:42"
              xmp:Rating="3"/>
          </rdf:RDF>
        </x:xmpmeta>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Tests that there is no loss if writing
     * the GIF chunks again without any change.
     */
    @Test
    fun testNoChange() {

        val bytes = KimTestData.getBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val byteWriter = ByteArrayByteWriter()

        GifWriter.writeImage(
            byteReader = byteReader,
            byteWriter = byteWriter,
            xmp = null
        )

        val newBytes = byteWriter.toByteArray()

        assertContentEquals(
            expected = bytes,
            actual = newBytes
        )
    }

    @Test
    fun testUpdateMetadata() {

        val bytes = KimTestData.getBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)

        val oldMetadata = Kim.readMetadata(bytes)

        assertNotNull(oldMetadata)

        val oldXmp = oldMetadata.xmp

        assertNotEquals(expectedXmp, oldXmp)

        val byteWriter = ByteArrayByteWriter()

        GifWriter.writeImage(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter,
            expectedXmp
        )

        val newBytes = byteWriter.toByteArray()

        val actualMetadata = Kim.readMetadata(newBytes)

        assertNotNull(actualMetadata)
        assertNotNull(actualMetadata.xmp)

        assertEquals(
            expected = expectedXmp,
            actual = actualMetadata.xmp
        )

        val expectedBytes = KimTestData.getModifiedBytesOf(KimTestData.GIF_TEST_IMAGE_INDEX)

        val equals = expectedBytes.contentEquals(newBytes)

        if (!equals) {

            Path("build/photo_${KimTestData.GIF_TEST_IMAGE_INDEX}_modified.gif")
                .writeBytes(newBytes)

            /* Also write a string representation to see differences more quickly. */
            Path("build/photo_${KimTestData.GIF_TEST_IMAGE_INDEX}_modified.txt")
                .writeBytes(Kim.readMetadata(newBytes).toString().encodeToByteArray())

            fail("Bytes for test image #${KimTestData.GIF_TEST_IMAGE_INDEX} are different.")
        }
    }
}
