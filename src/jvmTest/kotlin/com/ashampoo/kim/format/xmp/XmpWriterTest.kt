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
package com.ashampoo.kim.format.xmp

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.TiffOrientation
import com.ashampoo.kim.testdata.KimTestData
import com.ashampoo.xmp.XMPMetaFactory
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.sink
import kotlin.test.Test
import kotlin.test.fail

/*
 * FIXME On native the result is slightly different,
 *   because MicrosoftPhoto gets a new prefix.
 *   The problem must lie in XMP Core.
 *   We need to investigate this further.
 */
class XmpWriterTest {

    private val updates = setOf(
        MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT),
        MetadataUpdate.TakenDate(1_690_889_862_000L), // 01.08.2023 13:37:42
        MetadataUpdate.GpsCoordinates(GpsCoordinates(53.219391, 8.239661)),
        MetadataUpdate.Rating(PhotoRating.THREE_STARS),
        MetadataUpdate.Keywords(setOf("fox", "fuchs", "<swiper>")),
        // MetadataUpdate.Faces(mapOf("John" to RegionArea(0.2, 0.3, 0.4, 0.5))),
        MetadataUpdate.Persons(setOf("John"))
    )

    fun setUp() {
        Kim.underUnitTesting = true
    }

    @Test
    fun testWriteAcdSeeXmpFile(): Unit =
        doCompare("acdsee_sample")

    @Test
    fun testWriteDigiKamXmpFile(): Unit =
        doCompare("digikam_sample")

    @Test
    fun testWriteExifToolXmpFile(): Unit =
        doCompare("exiftool_sample")

    @Test
    fun testWriteMylioXmpFile(): Unit =
        doCompare("mylio_sample")

    @Test
    fun testWriteNarrativeFromMylioXmpFile(): Unit =
        doCompare("narrative_from_mylio_sample")

    @Test
    fun testWriteNarrativeXmpFile(): Unit =
        doCompare("narrative_sample")

    @OptIn(ExperimentalStdlibApi::class)
    private fun doCompare(baseFileName: String) {

        val originalXmp = KimTestData.getXmp("$baseFileName.xmp")

        val xmpMeta = XMPMetaFactory.parseFromString(originalXmp)

        val actualXmp = XmpWriter.updateXmp(
            xmpMeta = xmpMeta,
            updates = updates,
            writePackageWrapper = true
        )

        val expectedXmp = KimTestData.getXmp("${baseFileName}_mod.xmp")

        val equals = expectedXmp.contentEquals(actualXmp)

        if (!equals) {

            Path("build/${baseFileName}_mod.xmp")
                .writeBytes(actualXmp.encodeToByteArray())

            fail("Photo $baseFileName has not the expected bytes!")
        }
    }
}
