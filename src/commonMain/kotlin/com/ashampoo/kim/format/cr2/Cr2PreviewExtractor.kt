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
package com.ashampoo.kim.format.cr2

import com.ashampoo.kim.format.PreviewExtractor
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.DefaultRandomAccessByteReader

object Cr2PreviewExtractor : PreviewExtractor {

    override fun extractPreviewImage(byteReader: ByteReader, length: Long): ByteArray {

        val randomAccessByteReader = DefaultRandomAccessByteReader(byteReader, length)

        val tiffContents = TiffReader().read(randomAccessByteReader)

        val firstDirectory = tiffContents.directories.first()

        val previewImageStart = firstDirectory.getFieldValue(TiffTag.TIFF_TAG_STRIP_OFFSETS)
        val previewLength = firstDirectory.getFieldValue(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS)

        randomAccessByteReader.skipTo(previewImageStart)

        val previewBytes = randomAccessByteReader.readBytes(previewLength)

        return previewBytes
    }
}
