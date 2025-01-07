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
package com.ashampoo.kim.format.raf

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.jpeg.JpegImageParser
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readAndVerifyBytes
import com.ashampoo.kim.input.skipBytes
import com.ashampoo.kim.model.ImageFormat

public object RafImageParser : ImageParser {

    /**
     * The RAF file contains a JPEG with EXIF metadata.
     * We just have to find it and read the data from there it.
     */
    @Throws(ImageReadException::class)
    @Suppress("ComplexCondition", "LoopWithTooManyJumpStatements")
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            byteReader.readAndVerifyBytes(
                "RAF magic number",
                ImageFormatMagicNumbers.raf.toByteArray()
            )

            byteReader.skipBytes("86 header bytes", RafMetadataExtractor.REMAINING_HEADER_BYTE_COUNT)

            val offset = byteReader.read4BytesAsInt("JPEG offset", ByteOrder.BIG_ENDIAN)

            @Suppress("MagicNumber")
            val remainingBytesToOffset = offset -
                (RafMetadataExtractor.REMAINING_HEADER_BYTE_COUNT + ImageFormatMagicNumbers.raf.size + 4)

            byteReader.skipBytes("Skip JPEG offset", remainingBytesToOffset)

            return@tryWithImageReadException JpegImageParser
                .parseMetadata(byteReader)
                .copy(imageFormat = ImageFormat.RAF)
        }
}
