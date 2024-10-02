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
package com.ashampoo.kim.format.cr3;

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.format.bmff.box.MovieBox
import com.ashampoo.kim.format.bmff.box.UuidBox
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.model.ImageFormat

/**
 * Parses CR3 as documented on https://github.com/lclevy/canon_cr3
 */
internal object Cr3Reader {

    const val CR3_METADATA_UUID = "85c0b687820f11e08111f4ce462b6a48"

    fun createMetadata(allBoxes: List<Box>): ImageMetadata {

        val moovBox = allBoxes.filterIsInstance<MovieBox>().firstOrNull()
            ?: throw ImageReadException("Illegal CR3: No 'moov' box found.")

        val metadataBox = moovBox.boxes.filterIsInstance<UuidBox>().find { box ->
            box.uuidAsHex == CR3_METADATA_UUID
        } ?: throw ImageReadException("Illegal CR3: No metadata UUID box found.")

        println(metadataBox)

        val subBoxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(metadataBox.data),
            stopAfterMetadataRead = false,
            positionOffset = 4,
            offsetShift = metadataBox.offset + 16
        )

        println(subBoxes)

        // TODO
        return ImageMetadata(
            imageFormat = ImageFormat.CR3,
            imageSize = null,
            exif = null,
            exifBytes = null,
            iptc = null, // not covered by ISO BMFF
            xmp = null
        )
    }
}
