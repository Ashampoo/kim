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
package com.ashampoo.kim.format.arw

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.TiffPreviewExtractor
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.input.RandomAccessByteReader

public object ArwPreviewExtractor : TiffPreviewExtractor {

    @Throws(ImageReadException::class)
    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? = tryWithImageReadException {

        val ifd0 = tiffContents.directories.first()

        val previewImageStart =
            ifd0.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT) ?: return null

        val previewLength =
            ifd0.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH) ?: return null

        if (previewLength == 0)
            return null

        randomAccessByteReader.moveTo(previewImageStart)

        val previewBytes = randomAccessByteReader.readBytes(previewLength)

        return@tryWithImageReadException previewBytes
    }
}
