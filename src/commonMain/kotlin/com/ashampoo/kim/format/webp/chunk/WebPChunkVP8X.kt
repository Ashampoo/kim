/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
import com.ashampoo.kim.format.webp.WebPConstants.VP8X_PAYLOAD_LENGTH
import com.ashampoo.kim.model.ImageSize

/*
 * https://developers.google.com/speed/webp/docs/riff_container#extended_file_format
 */
@Suppress("MagicNumber")
class WebPChunkVP8X(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.VP8X, bytes), ImageSizeAware {

    val hasIcc: Boolean
    val hasAlpha: Boolean
    val hasExif: Boolean
    val hasXmp: Boolean
    val hasAnimation: Boolean

    override val imageSize: ImageSize

    init {

        if (bytes.size != VP8X_PAYLOAD_LENGTH)
            throw ImageReadException("VP8X chunk must be 10 bytes long.")

        val mark: Int = bytes[0].toInt() and 0xFF

        hasIcc = mark and 32 != 0
        hasAlpha = mark and 16 != 0
        hasExif = mark and 8 != 0
        hasXmp = mark and 4 != 0
        hasAnimation = mark and 2 != 0

        val canvasWidth = (bytes[4].toInt() and 0xFF) +
            (bytes[5].toInt() and 0xFF shl 8) +
            (bytes[6].toInt() and 0xFF shl 16) + 1

        val canvasHeight = (bytes[7].toInt() and 0xFF) +
            (bytes[8].toInt() and 0xFF shl 8) +
            (bytes[9].toInt() and 0xFF shl 16) + 1

        if (canvasWidth * canvasHeight < 0)
            throw ImageReadException("Illegal canvas size: $canvasWidth x $canvasHeight")

        imageSize = ImageSize(
            width = canvasWidth,
            height = canvasHeight
        )
    }

    override fun toString(): String =
        super.toString() +
            " hasIcc=$hasIcc hasAlpha=$hasAlpha hasExif=$hasExif" +
            " hasXmp=$hasXmp hasAnimation=$hasAnimation" +
            " imageSize=$imageSize"
}
