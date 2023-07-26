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
package com.ashampoo.kim.format.nef

import com.ashampoo.kim.format.TiffPreviewExtractor
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.constants.TiffConstants
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.RandomAccessByteReader

object NefPreviewExtractor : TiffPreviewExtractor {

    override fun extractPreviewImage(
        tiffContents: TiffContents,
        randomAccessByteReader: RandomAccessByteReader
    ): ByteArray? {

        val ifd1 = tiffContents.directories.find {
            it.type == TiffConstants.TIFF_IFD1
        } ?: return null

        val previewImageStart =
            ifd1.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT) ?: return null

        val previewLength =
            ifd1.getFieldValue(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH) ?: return null

        if (previewLength == 0)
            return null

        randomAccessByteReader.skipTo(previewImageStart)

        val previewBytes = randomAccessByteReader.readBytes(previewLength)

        return previewBytes
    }
}
