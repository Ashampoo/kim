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
package com.ashampoo.kim.format.raf

import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.jpeg.JpegImageParser
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PrePendingByteReader

object RafImageParser : ImageParser {

    /**
     * The RAF file contains a JPEG with EXIF metadata.
     * We just have to find it and read the data from there it.
     */
    @Suppress("ComplexCondition", "LoopWithTooManyJumpStatements")
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata {

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.raf.size).toList()

        /* Ensure it's actually an RAF. */
        require(magicNumberBytes == ImageFormatMagicNumbers.raf) {
            "RAF magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        @Suppress("kotlin:S1481") // false positive
        val bytes = mutableListOf<Byte>()

        while (true) {

            val byte = byteReader.readByte() ?: break

            bytes.add(byte)

            /* Search the header and then break */
            if (bytes.size >= ImageFormatMagicNumbers.jpeg.size &&
                bytes[bytes.lastIndex - 2] == ImageFormatMagicNumbers.jpeg[0] &&
                bytes[bytes.lastIndex - 1] == ImageFormatMagicNumbers.jpeg[1] &&
                bytes[bytes.lastIndex - 0] == ImageFormatMagicNumbers.jpeg[2]
            )
                break
        }

        /* Create a new reader, prepending the jpegMagicNumbers, and read the contained JPEG. */
        val newReader = PrePendingByteReader(
            delegate = byteReader,
            prependedBytes = ImageFormatMagicNumbers.jpeg
        )

        return JpegImageParser.parseMetadata(newReader)
    }
}
