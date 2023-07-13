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
package com.ashampoo.kim.common

import com.ashampoo.kim.Kim
import com.ashampoo.kim.model.PhotoMetadata
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PhotoMetadataConverterTest {

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testReadMetadataFromBytes() {

        val metadataMap = mutableMapOf<String, PhotoMetadata>()

        for (index in 1..56) {

            // TODO Handle broken file (bad IFD1)
            if (index == 21)
                continue

            calculateAndAppendMetadata(index, metadataMap)
        }

        assertEquals(
            expected = KimTestData.getMetadataCsvString(),
            actual = createCsvString(metadataMap)
        )
    }

    private fun calculateAndAppendMetadata(
        index: Int,
        metadataMap: MutableMap<String, PhotoMetadata>
    ) {

        /* Get full bytes for TIFF as we don't have header extraction. */
        val bytes = if (index in 54..56)
            KimTestData.getBytesOf(index)
        else
            KimTestData.getHeaderBytesOf(index)

        val photoMetadata = Kim.readMetadata(bytes)?.convertToPhotoMetadata()

        assertNotNull(photoMetadata)

        metadataMap[KimTestData.getFileName(index)] = photoMetadata
    }

    private fun createCsvString(metadataMap: Map<String, PhotoMetadata>): String {

        val stringBuilder = StringBuilder()

        stringBuilder.appendLine(
            "name;widthPx;heightPx;orientation;takenDate;latitude;longitude;" +
                "cameraMake;cameraModel;lensMake;lensModel;iso;exposureTime;fNumber;" +
                "focalLength;rating;keywords"
        )

        for (entry in metadataMap.entries) {

            val name = entry.key
            val metadata = entry.value

            stringBuilder.appendLine(
                "$name;${metadata.widthPx};${metadata.heightPx};" +
                    "${metadata.orientation};${metadata.takenDate};" +
                    "${metadata.gpsCoordinates?.latitude};${metadata.gpsCoordinates?.longitude};" +
                    "${metadata.cameraMake};${metadata.cameraModel};${metadata.lensMake};" +
                    "${metadata.lensModel};${metadata.iso};${metadata.exposureTime};" +
                    "${metadata.fNumber};${metadata.focalLength};${metadata.rating?.value};" +
                    "${metadata.keywords}"
            )
        }

        return stringBuilder.toString()
    }
}
