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
package com.ashampoo.kim.format

import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffDirectory
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

data class ImageMetadata(
    val imageFormat: ImageFormat?,
    val imageSize: ImageSize?,
    val exif: TiffContents?,
    val exifBytes: ByteArray?,
    val iptc: IptcMetadata?,
    val xmp: String?
) {

    fun getExifThumbnailBytes(): ByteArray? =
        exif?.directories?.asSequence()
            ?.mapNotNull { it.jpegImageDataElement?.bytes }
            ?.firstOrNull()

    fun findStringValue(tagInfo: TagInfo): String? {

        val strings = findTiffField(tagInfo)?.value as? List<String>

        /* Looks like Canon and Fuji OOC JPEGs have lens make in an array.  */
        if (!strings.isNullOrEmpty())
            return strings.first()

        return findTiffField(tagInfo)?.value as? String
    }

    fun findShortValue(tagInfo: TagInfo): Short? =
        findTiffField(tagInfo)?.toShort()

    fun findDoubleValue(tagInfo: TagInfo): Double? =
        findTiffField(tagInfo)?.toDouble()

    /*
     * Note: Keep in sync with TiffTags.getTag()
     */
    @Suppress("UnnecessaryParentheses")
    fun findTiffField(tagInfo: TagInfo): TiffField? {
        return exif?.directories?.firstOrNull { directory ->
            directory.type == tagInfo.directoryType?.directoryType ||
                (tagInfo.directoryType?.isImageDirectory == true && directory.type >= 0) ||
                (tagInfo.directoryType?.isImageDirectory == false && directory.type < 0)
        }?.findField(tagInfo)
    }

    fun findTiffDirectory(directoryType: Int): TiffDirectory? =
        exif?.directories?.find { it.type == directoryType }

    override fun toString(): String {

        val sb = StringBuilder()
        sb.appendLine("File format : $imageFormat")
        sb.appendLine("Resolution  : $imageSize")

        if (exif != null)
            sb.appendLine(exif)

        if (iptc != null)
            sb.appendLine(iptc)

        if (xmp != null) {

            sb.appendLine("---- XMP ----")
            sb.appendLine(xmp)
        }

        return sb.toString()
    }
}
