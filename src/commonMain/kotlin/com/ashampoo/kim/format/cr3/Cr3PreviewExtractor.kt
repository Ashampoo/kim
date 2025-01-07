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
package com.ashampoo.kim.format.cr3

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.box.MediaDataBox
import com.ashampoo.kim.format.bmff.box.MovieBox
import com.ashampoo.kim.format.bmff.box.TrackBox
import com.ashampoo.kim.format.bmff.box.UuidBox
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.read8BytesAsLong
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.skipBytes
import kotlin.jvm.JvmStatic

public object Cr3PreviewExtractor {

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? =
        extractFullSizePreviewImage(byteReader)

    /**
     * Extracts an preview image at full resolution.
     *
     * See https://github.com/lclevy/canon_cr3/blob/master/readme.md
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractFullSizePreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val movieBox = allBoxes.filterIsInstance<MovieBox>().firstOrNull()
            ?: return@tryWithImageReadException null

        val firstTrack = movieBox.boxes.filterIsInstance<TrackBox>().firstOrNull()
            ?: return@tryWithImageReadException null

        val mediaBox = firstTrack.mediaBox

        val mediaInformationContainer = mediaBox.boxes.find { it.type == BoxType.MINF }
            ?: return@tryWithImageReadException null

        val minfBoxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(mediaInformationContainer.payload),
            stopAfterMetadataRead = false
        )

        val sampleTableBox = minfBoxes.find { it.type == BoxType.STBL }
            ?: return@tryWithImageReadException null

        val stblBoxes = BoxReader.readBoxes(
            byteReader = ByteArrayByteReader(sampleTableBox.payload),
            stopAfterMetadataRead = false
        )

        val sampleSizesBox = stblBoxes.find { it.type == BoxType.STSZ }
            ?: return@tryWithImageReadException null

        val chunkOffsetBox = stblBoxes.find { it.type == BoxType.CO64 }
            ?: return@tryWithImageReadException null

        val stszReader = ByteArrayByteReader(sampleSizesBox.payload)

        /*
         * Skip one version byte, 3 bytes flags, 4 bytes sample size
         * and 4 bytes sample count.
         */
        stszReader.skipBytes("", 12)

        val length = stszReader.read4BytesAsInt("length", ByteOrder.BIG_ENDIAN)

        val co64Reader = ByteArrayByteReader(chunkOffsetBox.payload)

        /*
         * Skip one version byte, 3 bytes flags and 4 bytes entry count
         */
        co64Reader.skipBytes("", 8)

        val offset = co64Reader.read8BytesAsLong("offset", ByteOrder.BIG_ENDIAN)

        val mdatBox = allBoxes.filterIsInstance<MediaDataBox>().firstOrNull()
            ?: return@tryWithImageReadException null

        val offsetInMdatPayload = (offset - mdatBox.offset - 16).toInt()

        return@tryWithImageReadException mdatBox.payload.slice(
            startIndex = offsetInMdatPayload,
            count = length
        )
    }

    /**
     * Extracts an JPG with an resoltion of 1620 x 1080
     *
     * See https://github.com/lclevy/canon_cr3?tab=readme-ov-file#prvw-preview
     */
    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractSmallPreviewImage(
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
