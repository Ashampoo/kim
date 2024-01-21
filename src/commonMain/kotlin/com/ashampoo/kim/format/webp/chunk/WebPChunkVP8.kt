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
class WebPChunkVP8(
    bytes: ByteArray
) : WebPChunk(WebPChunkType.VP8, bytes) {

    val versionNumber: Int

    val width: Int
    val height: Int

    val horizontalScale: Int
    val verticalScale: Int

    init {

        if (bytes.size < REQUIRED_BYTE_SIZE)
            throw ImageReadException("Invalid VP8 chunk")

        /*
         * https://datatracker.ietf.org/doc/html/rfc6386#section-9
         *
         * Frame Header:
         *
         * 1. A 1-bit frame type (0 for key frames, 1 for interframes).
         *
         * 2. A 3-bit version number (0 - 3 are defined as four different profiles with different
         * decoding complexity; other values may be defined for future variants of the VP8 data format).
         *
         * 3. A 1-bit show_frame flag (0 when current frame is not for display, 1 when current frame is for display).
         *
         * 4. A 19-bit field containing the size of the first data partition in bytes.
         */
        val b0: Int = bytes[0].toInt() and 0xFF

        if (b0 and 1 != 0)
            throw ImageReadException("Invalid VP8 chunk: should be key frame")

        versionNumber = b0 and 14 shr 1

        if (b0 and 16 == 0)
            throw ImageReadException("Invalid VP8 chunk: frame should to be display")

        /*
         * Key Frame:
         *
         * Start code byte 0 0x9d Start code byte 1 0x01 Start code byte 2 0x2a
         *
         * 16 bits : (2 bits Horizontal Scale << 14) | Width (14 bits)
         * 16 bits : (2 bits Vertical Scale << 14) | Height (14 bits)
         */
        val b3: Int = bytes[3].toInt() and 0xFF
        val b4: Int = bytes[4].toInt() and 0xFF
        val b5: Int = bytes[5].toInt() and 0xFF
        val b6: Int = bytes[6].toInt() and 0xFF
        val b7: Int = bytes[7].toInt() and 0xFF
        val b8: Int = bytes[8].toInt() and 0xFF
        val b9: Int = bytes[9].toInt() and 0xFF

        if (b3 != 0x9D || b4 != 0x01 || b5 != 0x2A)
            throw ImageReadException("Invalid VP8 chunk: invalid signature")

        width = b6 + (b7 and 63 shl 8)
        height = b8 + (b9 and 63 shl 8)

        horizontalScale = b7 shr 6
        verticalScale = b9 shr 6
    }

    override fun toString(): String =
        super.toString() +
            " versionNumber=$versionNumber width=$width height=$height" +
            " horizontalScale=$horizontalScale verticalScale=$verticalScale"

    companion object {

        private const val REQUIRED_BYTE_SIZE: Int = 10
    }
}
