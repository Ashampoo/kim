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
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.TiffOrientation
import com.goncalossilva.resources.Resource
import kotlinx.io.files.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

class PngUpdaterTest {

    private val keywordWithUmlauts = "Äußerst öffentlich"

    private val crashBuildingGps = GpsCoordinates(
        53.219391,
        8.239661
    )

    private val timestamp = 1_689_166_125_401 // 2023:07:12 12:48:45

    private val resourcePath: String = "src/commonTest/resources/com/ashampoo/kim/updates_png"

    private val originalBytes = Resource("$resourcePath/original.png").readBytes()

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateOrientation() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        compare("rotated_right.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateTakenDate() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.TakenDate(timestamp)
        )

        compare("new_taken_date.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateGpsCoordinates() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.GpsCoordinates(crashBuildingGps)
        )

        compare("new_gps_coordinates.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateRating() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Rating(PhotoRating.FOUR_STARS)
        )

        compare("new_rating.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateKeywords() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts))
        )

        compare("new_keywords.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdatePersons() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Persons(setOf("Swiper", "Dora"))
        )

        compare("new_persons.png", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun compare(fileName: String, actualBytes: ByteArray) {

        val resource = Resource("$resourcePath/$fileName")

        if (!resource.exists()) {

            Path("build/$fileName")
                .writeBytes(actualBytes)

            fail("Reference image $fileName does not exist.")
        }

        val expectedBytes = resource.readBytes()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals) {

            Path("build/$fileName")
                .writeBytes(actualBytes)

            fail("Photo $fileName has not the expected bytes!")
        }
    }
}
