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
package com.ashampoo.kim.format.rw2

import com.ashampoo.kim.format.PreviewExtractor
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.DefaultRandomAccessByteReader

object Rw2PreviewExtractor : PreviewExtractor {

    override fun extractPreviewImage(byteReader: ByteReader, length: Long): ByteArray? {

        val randomAccessByteReader = DefaultRandomAccessByteReader(byteReader, length)

        val tiffContents = TiffReader().read(randomAccessByteReader)

        val ifd0 = tiffContents.directories.first()

        return ifd0.getFieldValue(TiffTag.TIFF_JPG_FROM_RAW, false)
    }
}
