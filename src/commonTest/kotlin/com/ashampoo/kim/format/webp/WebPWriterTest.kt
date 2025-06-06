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
package com.ashampoo.kim.format.webp

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.tiff.write.TiffWriterLossless
import com.ashampoo.kim.format.tiff.write.TiffWriterLossy
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

class WebPWriterTest {

    /* language=XML */
    private val expectedXmp = """
        <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                    xmlns:exif="http://ns.adobe.com/exif/1.0/"
                  exif:DateTimeOriginal="2020-10-05T13:37:42"
                  xmp:Rating="3"/>
              </rdf:RDF>
            </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Tests that there is no loss if writing
     * the WebP chunks again without any change.
     */
    @Test
    fun testNoChange() {

        for (index in KimTestData.webpPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val byteReader = ByteArrayByteReader(bytes)

            val byteWriter = ByteArrayByteWriter()

            WebPWriter.writeImage(
                byteReader = byteReader,
                byteWriter = byteWriter,
                exifBytes = null,
                xmp = null
            )

            val newBytes = byteWriter.toByteArray()

            assertContentEquals(
                expected = bytes,
                actual = newBytes
            )
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateMetadata() {

        for (index in KimTestData.webpPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val oldMetadata = Kim.readMetadata(bytes)

            assertNotNull(oldMetadata)

            val oldXmp = oldMetadata.xmp

            assertNotEquals(expectedXmp, oldXmp)

            val tiffOutputSet = oldMetadata.exif?.createOutputSet() ?: TiffOutputSet()

            val exifDirectory = tiffOutputSet.getOrCreateExifDirectory()

            /*
             * Note: We write a different date to EXIF than to XMP
             * to see which viewer gives priority to which field.
             */
            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2023:08:01 08:00:00")

            val exifBytesWriter = ByteArrayByteWriter()

            val oldExifBytes = oldMetadata.exifBytes

            val writer = if (oldExifBytes != null)
                TiffWriterLossless(
                    byteOrder = tiffOutputSet.byteOrder,
                    exifBytes = oldExifBytes
                )
            else
                TiffWriterLossy(
                    byteOrder = tiffOutputSet.byteOrder
                )

            writer.write(exifBytesWriter, tiffOutputSet)

            val exifBytes: ByteArray = exifBytesWriter.toByteArray()

            val byteWriter = ByteArrayByteWriter()

            WebPWriter.writeImage(
                byteReader = ByteArrayByteReader(bytes),
                byteWriter,
                exifBytes,
                expectedXmp
            )

            val newBytes = byteWriter.toByteArray()

            val actualMetadata = Kim.readMetadata(newBytes)

            assertNotNull(actualMetadata)
            assertNotNull(actualMetadata.exif)
            assertNotNull(actualMetadata.xmp)

            assertEquals(
                expected = expectedXmp,
                actual = actualMetadata.xmp
            )

            val expectedBytes = KimTestData.getModifiedBytesOf(index)

            val equals = expectedBytes.contentEquals(newBytes)

            if (!equals) {

                Path("build/photo_${index}_modified.webp")
                    .writeBytes(newBytes)

                /* Also write a string representation to see differences more quickly. */
                Path("build/photo_${index}_modified.txt")
                    .writeBytes(Kim.readMetadata(newBytes).toString().encodeToByteArray())

                fail("Bytes for test image #$index are different.")
            }
        }
    }
}
