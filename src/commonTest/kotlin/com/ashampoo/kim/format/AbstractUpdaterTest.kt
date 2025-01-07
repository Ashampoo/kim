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
package com.ashampoo.kim.format

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.exists
import com.ashampoo.kim.common.readBytes
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.getPathForResource
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.TiffOrientation
import kotlinx.io.files.Path
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.fail

abstract class AbstractUpdaterTest(
    val format: String
) {

    private val keywordWithUmlauts = "Äußerst öffentlich"

    private val crashBuildingGps = GpsCoordinates(
        53.219391,
        8.239661
    )

    private val timestamp = 1_689_166_125_401 // 2023:07:12 12:48:45

    private val resourcePath: String = "src/commonTest/resources/com/ashampoo/kim/updates_$format"

    private val originalBytes = Path(
        getPathForResource("$resourcePath/original.$format")
    ).readBytes()

    private val noMetadataBytes = Path(
        getPathForResource("$resourcePath/no_metadata.$format")
    ).readBytes()

    private val thumbnailBytes = Path(
        getPathForResource("$resourcePath/../testdata/test_thumb.jpg")
    ).readBytes()

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

        compare("rotated_right.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateOrientationOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Orientation(TiffOrientation.ROTATE_RIGHT)
        )

        compare("rotated_right.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateTakenDate() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.TakenDate(timestamp)
        )

        compare("new_taken_date.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateTakenDateOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.TakenDate(timestamp)
        )

        compare("new_taken_date.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateGpsCoordinates() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.GpsCoordinates(crashBuildingGps)
        )

        compare("new_gps_coordinates.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateGpsCoordinatesOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.GpsCoordinates(crashBuildingGps)
        )

        compare("new_gps_coordinates.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateRating() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Rating(PhotoRating.FOUR_STARS)
        )

        compare("new_rating.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateRatingOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Rating(PhotoRating.FOUR_STARS)
        )

        compare("new_rating.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateKeywords() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts))
        )

        compare("new_keywords.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateKeywordsOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Keywords(setOf("hello", "test", keywordWithUmlauts))
        )

        compare("new_keywords.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdatePersons() {

        val newBytes = Kim.update(
            bytes = originalBytes,
            update = MetadataUpdate.Persons(setOf("Swiper", "Dora"))
        )

        compare("new_persons.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdatePersonsOnEmptyImage() {

        val newBytes = Kim.update(
            bytes = noMetadataBytes,
            update = MetadataUpdate.Persons(setOf("Swiper", "Dora"))
        )

        compare("new_persons.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateThumbnail() {

        val newBytes = Kim.updateThumbnail(
            bytes = originalBytes,
            thumbnailBytes = thumbnailBytes
        )

        compare("new_thumbnail.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testUpdateThumbnailOnEmptyImage() {

        val newBytes = Kim.updateThumbnail(
            bytes = noMetadataBytes,
            thumbnailBytes = thumbnailBytes
        )

        compare("new_thumbnail.no_metadata.$format", newBytes)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun compare(fileName: String, actualBytes: ByteArray) {

        val path = Path(getPathForResource("$resourcePath/$fileName"))

        if (!path.exists()) {

            Path("build/$fileName")
                .writeBytes(actualBytes)

            fail("Reference image $fileName does not exist.")
        }

        val expectedBytes = path.readBytes()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals) {

            Path("build/$fileName")
                .writeBytes(actualBytes)

            /* Also write a string representation to see differences more quickly. */
            Path("build/$fileName.txt")
                .writeBytes(Kim.readMetadata(actualBytes).toString().encodeToByteArray())

            fail("Photo $fileName has not the expected bytes!")
        }
    }
}
