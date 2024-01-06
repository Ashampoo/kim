/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package com.ashampoo.kim.format.heic

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.heic.HeicConstants.HEIC_BYTE_ORDER
import com.ashampoo.kim.format.heic.HeicConstants.ITEM_TYPE_EXIF
import com.ashampoo.kim.format.heic.boxes.ItemInformationBox
import com.ashampoo.kim.format.heic.boxes.ItemLocationBox
import com.ashampoo.kim.format.heic.boxes.MetaBox
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PositionTrackingByteReaderDecorator
import com.ashampoo.kim.model.ImageFormat

object HeicImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        parseMetadata(PositionTrackingByteReaderDecorator(byteReader))

    private fun parseMetadata(byteReader: PositionTrackingByteReaderDecorator): ImageMetadata {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            skipMediaBox = true
        )

        val metaBox = allBoxes.find { it.type == BoxType.META } as MetaBox

        val itemLocationBox = metaBox.boxes.find { it.type == BoxType.ILOC } as ItemLocationBox
        val itemInfoBox = metaBox.boxes.find { it.type == BoxType.IINF } as ItemInformationBox

        var exifBytes: ByteArray? = null

        for (extent in itemLocationBox.extents) {

            val itemId = extent.itemId

            val itemInfo = itemInfoBox.map.get(itemId) ?: continue

            if (itemInfo.itemType == ITEM_TYPE_EXIF) {

                val bytesToSkip = extent.offset - byteReader.position

                byteReader.skipBytes("offset to EXIF extent", bytesToSkip.toInt())

                val tiffHeaderOffset =
                    byteReader.read4BytesAsInt("tiffHeaderOffset", HEIC_BYTE_ORDER)

                /* Usualy there are 6 bytes skipped, which are the EXIF header. ("Exif.."). */
                byteReader.skipBytes("offset to TIFF header", tiffHeaderOffset)

                exifBytes = byteReader.readBytes(
                    extent.length.toInt() - tiffHeaderOffset
                )
            }
        }

        val exif = exifBytes?.let { TiffReader.read(exifBytes) }

        return ImageMetadata(
            imageFormat = ImageFormat.HEIC,
            imageSize = null, // TODO
            exif = exif,
            exifBytes = exifBytes,
            iptc = null, // TODO
            xmp = null // TODO
        )
    }
}
