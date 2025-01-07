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
package com.ashampoo.kim.format.jxl

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.format.jxl.box.ExifBox
import com.ashampoo.kim.format.jxl.box.XmlBox
import com.ashampoo.kim.model.ImageFormat

internal object JxlReader {

    fun createMetadata(allBoxes: List<Box>): ImageMetadata {

        val exifBox = allBoxes.filterIsInstance<ExifBox>().firstOrNull()
        val xmlBox = allBoxes.filterIsInstance<XmlBox>().firstOrNull()

        return ImageMetadata(
            imageFormat = ImageFormat.JXL,
            imageSize = null, // TODO https://github.com/Ashampoo/kim/issues/65
            exif = exifBox?.tiffContents,
            exifBytes = exifBox?.exifBytes,
            iptc = null, // not covered by ISO BMFF
            xmp = xmlBox?.xmp
        )
    }
}
