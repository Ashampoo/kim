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

import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossy
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PngWriterTest {

    val expectedXmp = """
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

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testUpdateMetadata() {

        for (index in KimTestData.pngPhotoIds) {

            val bytes = KimTestData.getBytesOf(index)

            val oldXmp = Kim.readMetadata(bytes)?.xmp

            assertNotEquals(expectedXmp, oldXmp)

            val tiffOutputSet = TiffOutputSet()

            /*
             * Note: We erite a different date to EXIF than to XMP
             * to see which viewer gives priority to what field.
             */
            tiffOutputSet.getOrCreateExifDirectory().add(
                ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, "2023:08:01 08:00:00"
            )

            val exifBytesWriter = ByteArrayByteWriter()

            TiffImageWriterLossy().write(exifBytesWriter, tiffOutputSet)

            val exifBytes: ByteArray = exifBytesWriter.toByteArray()

            val byteWriter = ByteArrayByteWriter()

            PngWriter().writeImage(
                byteWriter,
                bytes,
                exifBytes,
                expectedXmp
            )

            val newBytes = byteWriter.toByteArray()

            val actualXmp = Kim.readMetadata(newBytes)?.xmp

            assertEquals(expectedXmp, actualXmp)

            // File("build/photo_${index}_modified.png").writeBytes(newBytes)

            val expectedBytes = KimTestData.getModifiedBytesOf(index)

            assertTrue(
                expectedBytes.contentEquals(newBytes),
                "Bytes for $index are different."
            )
        }
    }
}
