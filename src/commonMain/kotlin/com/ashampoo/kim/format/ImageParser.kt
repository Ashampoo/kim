/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.heic.HeicImageParser
import com.ashampoo.kim.format.jpeg.JpegImageParser
import com.ashampoo.kim.format.png.PngImageParser
import com.ashampoo.kim.format.raf.RafImageParser
import com.ashampoo.kim.format.tiff.TiffImageParser
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat

fun interface ImageParser {

    @Throws(ImageReadException::class)
    fun parseMetadata(byteReader: ByteReader): ImageMetadata

    companion object {

        fun forFormat(imageFormat: ImageFormat): ImageParser? =
            when (imageFormat) {

                ImageFormat.JPEG -> JpegImageParser

                ImageFormat.PNG -> PngImageParser

                ImageFormat.TIFF,
                ImageFormat.CR2,
                ImageFormat.NEF,
                ImageFormat.ARW,
                ImageFormat.RW2,
                ImageFormat.ORF -> TiffImageParser

                ImageFormat.RAF -> RafImageParser

                ImageFormat.HEIC -> HeicImageParser

                else -> null
            }
    }
}
