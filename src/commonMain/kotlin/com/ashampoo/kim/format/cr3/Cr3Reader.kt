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

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.model.ImageFormat

internal object Cr3Reader {

    fun createMetadata(allBoxes: List<Box>): ImageMetadata {

        println("CR3 boxes: " + allBoxes)

//        val exifBox = allBoxes.filterIsInstance<ExifBox>().firstOrNull()
//        val xmlBox = allBoxes.filterIsInstance<XmlBox>().firstOrNull()

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
