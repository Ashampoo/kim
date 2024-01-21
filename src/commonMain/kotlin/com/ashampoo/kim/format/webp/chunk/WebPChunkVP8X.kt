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

@Suppress("MagicNumber")
class WebPChunkVP8X(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.VP8X, bytes) {

    private val hasIcc: Boolean
    private val hasAlpha: Boolean
    private val hasExif: Boolean
    private val hasXmp: Boolean
    private val hasAnimation: Boolean

    private val canvasWidth: Int
    private val canvasHeight: Int

    init {

        if (bytes.size != REQUIRED_BYTE_SIZE)
            throw ImageReadException("VP8X chunk size must be 10")

        val mark: Int = bytes[0].toInt() and 0xFF

        hasIcc = mark and 32 != 0
        hasAlpha = mark and 16 != 0
        hasExif = mark and 8 != 0
        hasXmp = mark and 4 != 0
        hasAnimation = mark and 2 != 0

        canvasWidth = (bytes[4].toInt() and 0xFF) +
            (bytes[5].toInt() and 0xFF shl 8) +
            (bytes[6].toInt() and 0xFF shl 16) + 1

        canvasHeight = (bytes[7].toInt() and 0xFF) +
            (bytes[8].toInt() and 0xFF shl 8) +
            (bytes[9].toInt() and 0xFF shl 16) + 1

        if (canvasWidth * canvasHeight < 0)
            throw ImageReadException("Illegal canvas size: $canvasWidth x $canvasHeight")
    }

    override fun toString(): String =
        super.toString() +
            " hasIcc=$hasIcc hasAlpha=$hasAlpha hasExif=$hasExif" +
            " hasXmp=$hasXmp hasAnimation=$hasAnimation" +
            " canvasWidth=$canvasWidth canvasHeight=$canvasHeight"

    companion object {

        private const val REQUIRED_BYTE_SIZE: Int = 10
    }
}
