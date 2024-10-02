/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.cr3

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.box.UuidBox
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.skipBytes
import kotlin.jvm.JvmStatic

/**
 * See https://github.com/lclevy/canon_cr3?tab=readme-ov-file#prvw-preview
 */
public object Cr3PreviewExtractor {

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val previewUuidBox = allBoxes.filterIsInstance<UuidBox>().find {
            it.uuidAsHex == Cr3Reader.CR3_PREVIEW_UUID
        } ?: return@tryWithImageReadException null

        val payloadReader = ByteArrayByteReader(previewUuidBox.data)

        /* Skip unknown bytes */
        payloadReader.skipBytes("", 8)

        /* Skip size */
        payloadReader.skipBytes("size", 4)

        val marker = payloadReader.readBytes("marker", 4).decodeToString()

        if (marker != "PRVW")
            throw ImageReadException("Expected marker PRVW, but got: $marker")

        /* Not interesting bytes */
        payloadReader.skipBytes("header", 12)

        val jpegSize = payloadReader.read4BytesAsInt("jpegSize", ByteOrder.BIG_ENDIAN)

        val jpegBytes = payloadReader.readBytes(jpegSize)

        return@tryWithImageReadException jpegBytes
    }
}
