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
package com.ashampoo.kim.format.webp.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.webp.WebPChunkType
import com.ashampoo.kim.format.webp.WebPConstants
import com.ashampoo.kim.model.ImageSize

/*
 * https://developers.google.com/speed/webp/docs/riff_container#simple_file_format_lossless
 */
@Suppress("MagicNumber")
public class WebPChunkVP8L(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.VP8L, bytes), ImageSizeAware {

    override val imageSize: ImageSize

    public val hasAlpha: Boolean

    public val versionNumber: Int

    init {

        val b1: Int = bytes[1].toInt() and 0xFF
        val b2: Int = bytes[2].toInt() and 0xFF
        val b3: Int = bytes[3].toInt() and 0xFF
        val b4: Int = bytes[4].toInt() and 0xFF

        imageSize = ImageSize(
            width = b1 + (b2 and 63 shl 8) + 1,
            height = (b4 and 0x0F shl 10 or (b3 shl 2) or (b2 and 0xC0 shr 6)) + 1
        )

        if (imageSize.longestSide > WebPConstants.MAX_SIDE_LENGTH)
            throw ImageReadException("Illegal dimensions: $imageSize")

        hasAlpha = b4 and 16 != 0

        versionNumber = b4 shr 5

        if (versionNumber != 0)
            throw ImageReadException("VP8L version should be 0")
    }

    override fun toString(): String =
        super.toString() +
            " imageSize=$imageSize" +
            " hasAlpha=$hasAlpha versionNumber=$versionNumber"
}
