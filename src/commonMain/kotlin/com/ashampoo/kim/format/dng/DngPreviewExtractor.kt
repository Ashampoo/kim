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
package com.ashampoo.kim.format.dng

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.TiffPreviewExtractor
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.TiffConstants
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.RandomAccessByteReader

object DngPreviewExtractor : TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val ifd0 = tiffContents.directories.first()

        /* Ensure that the file is a DNG by checking the required tag. */
        if (ifd0.getFieldValue(TiffTag.TIFF_TAG_DNG_VERSION, false) == null)
            return null

        val subIfd1 = tiffContents.directories.find {
            it.type == TiffConstants.EXIF_SUB_IFD1
        } ?: return null

        val previewImageStart =
            subIfd1.getFieldValue(ExifTag.EXIF_TAG_PREVIEW_IMAGE_START_SUB_IFD1) ?: return null

        val previewLength =
            subIfd1.getFieldValue(ExifTag.EXIF_TAG_PREVIEW_IMAGE_LENGTH_SUB_IFD1) ?: return null

        if (previewLength == 0)
            return null

        randomAccessByteReader.moveTo(previewImageStart)

        val previewBytes = randomAccessByteReader.readBytes(previewLength)

        return@tryWithImageReadException previewBytes
    }
}
