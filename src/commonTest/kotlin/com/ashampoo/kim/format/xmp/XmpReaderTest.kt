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
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.PhotoMetadata
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.RegionArea
import com.ashampoo.kim.model.TiffOrientation
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class XmpReaderTest {

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    @Test
    fun testReadAcdSeeXmpFile() {

        val xmp = KimTestData.getXmp("acdsee_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_664_279_361_000,
                gpsCoordinates = null,
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.THREE_STARS,
                keywords = setOf("Braut", "Bräutigam", "Paar", "After-Party"),
                faces = emptyMap(),
                personsInImage = emptySet()
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadDigikamXmpFile() {

        val xmp = KimTestData.getXmp("digikam_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_664_279_361_000,
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.2193388,
                    longitude = 8.239984883333333
                ),
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.FOUR_STARS,
                keywords = setOf("Pflanze", "Ecke", "MacBook"),
                faces = mapOf(
                    "MacBook" to RegionArea(0.581172, 0.66247, 0.583093, 0.502398)
                ),
                personsInImage = emptySet()
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadExiftoolXmpFile() {

        val xmp = KimTestData.getXmp("exiftool_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = null,
                takenDate = 1_540_041_598_000,
                gpsCoordinates = null,
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.THREE_STARS,
                keywords = setOf(
                    "\"fuchs\"",
                    "<HALLO>",
                    "fox",
                    "fuchs",
                    "fuchs = \"süß\"",
                    "süßer fuchs",
                    "was solls"
                ),
                faces = mapOf(
                    "Swiper" to RegionArea(0.404336, 0.422313, 0.124503, 0.240097)
                ),
                personsInImage = setOf("Swiper")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadMylioXmpFile() {

        val xmp = KimTestData.getXmp("mylio_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = TiffOrientation.STANDARD,
                takenDate = 1_456_064_625_420,
                gpsCoordinates = GpsCoordinates(
                    latitude = 53.21939166666667,
                    longitude = 8.239661666666667
                ),
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.REJECTED,
                keywords = setOf("animal", "bird"),
                faces = mapOf(
                    "Eye Left" to RegionArea(0.295179, 0.278880, 0.033245, 0.05),
                    "Eye Right" to RegionArea(0.814990, 0.472579, 0.033245, 0.05),
                    "Nothing" to RegionArea(0.501552, 0.905484, 0.033245, 0.05)
                ),
                personsInImage = setOf("Eye Left", "Eye Right", "Nothing")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadNarrativeXmpFile() {

        val xmp = KimTestData.getXmp("narrative_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = TiffOrientation.ROTATE_RIGHT,
                takenDate = null,
                gpsCoordinates = null,
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.FOUR_STARS,
                keywords = emptySet(),
                faces = emptyMap(),
                personsInImage = emptySet()
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }

    @Test
    fun testReadNarrativeFromMylioXmpFile() {

        val xmp = KimTestData.getXmp("narrative_from_mylio_sample.xmp")

        assertEquals(
            expected = PhotoMetadata(
                widthPx = null,
                heightPx = null,
                orientation = null,
                takenDate = 1_540_041_598_620,
                gpsCoordinates = null,
                location = null,
                cameraMake = null,
                cameraModel = null,
                lensMake = null,
                lensModel = null,
                iso = null,
                exposureTime = null,
                fNumber = null,
                focalLength = null,
                rating = PhotoRating.FIVE_STARS,
                keywords = setOf(
                    "\"fuchs\"",
                    "<HALLO>",
                    "fox",
                    "fuchs",
                    "fuchs = \"süß\"",
                    "süßer fuchs",
                    "was solls"
                ),
                faces = mapOf(
                    "Swiper" to RegionArea(0.404336, 0.422313, 0.124503, 0.240097)
                ),
                personsInImage = setOf("Swiper")
            ),
            actual = XmpReader.readMetadata(xmp)
        )
    }
}
