/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.String

object TiffImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata {

        /*
         * TODO Implement a logic that only reads as much bytes as requested
         *  and not the whole file at once. Reading the whole file is bad for cloud hosted files.
         */
        val randomAccessByteReader = ByteArrayByteReader(byteReader.readRemainingBytes())

        val exif = TiffReader().read(randomAccessByteReader)

        val imageSize = getImageSize(exif)
        val xmp = getXmpXml(exif)

        return ImageMetadata(ImageFormat.TIFF, imageSize, exif, null, null, xmp)
    }

    private fun getImageSize(tiffContents: TiffContents): ImageSize? {

        val directory = tiffContents.directories.first()

        val widthField = directory.findField(TiffTag.TIFF_TAG_IMAGE_WIDTH, false)
        val heightField = directory.findField(TiffTag.TIFF_TAG_IMAGE_LENGTH, false)

        if (widthField == null || heightField == null)
            return null

        return ImageSize(widthField.toInt(), heightField.toInt())
    }

    private fun getXmpXml(tiffContents: TiffContents): String? {

        val firstDirectory = tiffContents.directories.first()

        val bytes = firstDirectory.getFieldValue(TiffTag.TIFF_TAG_XMP, false) ?: return null

        return String(bytes, charset = Charsets.UTF_8)
    }
}
