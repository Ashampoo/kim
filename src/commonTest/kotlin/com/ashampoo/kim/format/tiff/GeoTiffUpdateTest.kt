/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.readBytes
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.format.tiff.constant.GeoTiffTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.tiff.write.TiffWriterLossy
import com.ashampoo.kim.getPathForResource
import com.ashampoo.kim.output.ByteArrayByteWriter
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.fail

class GeoTiffUpdateTest {

    private val resourcePath: String = "src/commonTest/resources/com/ashampoo/kim/updates_tif"

    private val originalBytes = Path(
        getPathForResource("$resourcePath/empty.tif")
    ).readBytes()

    private val expectedBytes = Path(
        getPathForResource("$resourcePath/geotiff.tif")
    ).readBytes()

    @Test
    fun testSetGeoTiff() {

        val metadata = Kim.readMetadata(originalBytes) ?: return

        val outputSet: TiffOutputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
            doubleArrayOf(0.0002303616678184751, -0.0001521606816798535, 0.0)
        )

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_MODEL_TIEPOINT_TAG,
            doubleArrayOf(0.0, 0.0, 0.0, 8.915687629578438, 48.92432542097789, 0.0)
        )

        rootDirectory.add(
            GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
            shortArrayOf(1, 0, 2, 3, 1024, 0, 1, 2, 2048, 0, 1, 4326, 1025, 0, 1, 2)
        )

        val byteWriter = ByteArrayByteWriter()

        val writer = TiffWriterLossy(
            ByteOrder.LITTLE_ENDIAN
        )

        writer.write(
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        val actualBytes = byteWriter.toByteArray()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals) {

            Path("build/geotiff.tif")
                .writeBytes(actualBytes)

            fail("geotiff.tif has not the expected bytes!")
        }
    }

}
