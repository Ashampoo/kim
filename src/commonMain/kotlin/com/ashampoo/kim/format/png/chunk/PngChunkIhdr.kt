/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.png.chunk

import com.ashampoo.kim.format.png.PngChunkType
import com.ashampoo.kim.format.png.PngConstants.PNG_BYTE_ORDER
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.model.ImageSize

public class PngChunkIhdr(
    bytes: ByteArray,
    crc: Int
) : PngChunk(PngChunkType.IHDR, bytes, crc) {

    public val imageSize: ImageSize

    init {

        val byteReader = ByteArrayByteReader(bytes)

        imageSize = ImageSize(
            width = byteReader.read4BytesAsInt("width", PNG_BYTE_ORDER),
            height = byteReader.read4BytesAsInt("height", PNG_BYTE_ORDER)
        )
    }
}
