/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ashampoo.kim.format.webp

import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.webp.WebPConstants.RIFF_SIGNATURE
import com.ashampoo.kim.format.webp.WebPConstants.WEBP_BYTE_ORDER
import com.ashampoo.kim.format.webp.WebPConstants.WEBP_SIGNATURE
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat

object WebPImageParser : ImageParser {

    /*
     * https://developers.google.com/speed/webp/docs/riff_container
     */
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            byteReader.readAndVerifyBytes("RIFF signature", RIFF_SIGNATURE)

            byteReader.read4BytesAsInt("length", WEBP_BYTE_ORDER)

            byteReader.readAndVerifyBytes("WEBP signature", WEBP_SIGNATURE)

            // TODO

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.WEBP,
                imageSize = null,
                exif = null,
                exifBytes = null,
                iptc = null,
                xmp = null
            )
        }
}
