/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.DefaultRandomAccessByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object TiffImageParser : ImageParser {

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            /**
             * TIFF files, such as CR2 RAW files, can be extremely large.
             * It is not advisable to load them entirely into a ByteArray.
             */
            val randomAccessByteReader = DefaultRandomAccessByteReader(byteReader)

            val exif = TiffReader.read(randomAccessByteReader)

            val imageSize = getImageSize(exif)
            val xmp = getXmpXml(exif)

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.TIFF,
                imageSize = imageSize,
                exif = exif,
                exifBytes = null,
                iptc = null,
                xmp = xmp
            )
        }

    private fun getImageSize(tiffContents: TiffContents): ImageSize? {

        /*
         * First check if ExifImageWidth & ExifImageHeight are set.
         * This is the case with Sony ARW files and the only correct information.
         */

        val exifIfdDir = tiffContents.findTiffDirectory(TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD.directoryType)

        if (exifIfdDir != null) {

            val exifImageWidth = exifIfdDir.findField(ExifTag.EXIF_TAG_EXIF_IMAGE_WIDTH)?.toInt()
            val exifImageHeight = exifIfdDir.findField(ExifTag.EXIF_TAG_EXIF_IMAGE_HEIGHT)?.toInt()

            if (exifImageWidth != null && exifImageHeight != null)
                return ImageSize(exifImageWidth, exifImageHeight)
        }

        /*
         * NEF files have the image length of the full resoltion
         * image in SubIFD1 and not in the first directory, which
         * contains the thumbnail. Just always taking the first
         * directory is wrong.
         *
         * Other vendors use the SubIFD differently.
         * Just look for the biggest size and report that.
         */

        val imageSizes = mutableListOf<ImageSize>()

        for (directory in tiffContents.directories)
            getImageSize(directory)?.let { imageSizes.add(it) }

        return imageSizes.maxByOrNull { it.width * it.height }
    }

    private fun getImageSize(directory: TiffDirectory): ImageSize? {

        val widthField = directory.findField(TiffTag.TIFF_TAG_IMAGE_WIDTH, false)
        val heightField = directory.findField(TiffTag.TIFF_TAG_IMAGE_LENGTH, false)

        if (widthField == null || heightField == null)
            return null

        return ImageSize(widthField.toInt(), heightField.toInt())
    }

    private fun getXmpXml(tiffContents: TiffContents): String? {

        val firstDirectory = tiffContents.directories.first()

        val bytes = firstDirectory.getFieldValue(TiffTag.TIFF_TAG_XMP, false) ?: return null

        if (bytes.isEmpty())
            return null

        return bytes.decodeToString()
    }
}
