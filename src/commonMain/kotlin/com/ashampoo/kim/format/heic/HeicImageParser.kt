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
import com.ashampoo.kim.format.heic.HeicConstants.ITEM_TYPE_MIME
import com.ashampoo.kim.format.heic.HeicConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import com.ashampoo.kim.format.heic.boxes.Extent
import com.ashampoo.kim.format.heic.boxes.ImageSizeBox
import com.ashampoo.kim.format.heic.boxes.MetaBox
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PositionTrackingByteReader
import com.ashampoo.kim.input.PositionTrackingByteReaderDecorator
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object HeicImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        parseMetadata(PositionTrackingByteReaderDecorator(byteReader))

    private fun parseMetadata(byteReader: PositionTrackingByteReaderDecorator): ImageMetadata {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            skipMediaBox = true
        )

        val metaBox = allBoxes.find { it.type == BoxType.META } as MetaBox

        val imageSize = extractImageSize(metaBox)

        var exifBytes: ByteArray? = null
        var xmp: String? = null

        for (extent in metaBox.itemLocationBox.extents) {

            val itemInfo = metaBox.itemInfoBox.map.get(extent.itemId) ?: continue

            if (itemInfo.itemType == ITEM_TYPE_EXIF)
                exifBytes = readExifBytes(byteReader, extent)

            if (itemInfo.itemType == ITEM_TYPE_MIME)
                xmp = readXmpString(byteReader, extent)
        }

        val exif = exifBytes?.let { TiffReader.read(exifBytes) }

        return ImageMetadata(
            imageFormat = ImageFormat.HEIC,
            imageSize = imageSize,
            exif = exif,
            exifBytes = exifBytes,
            iptc = null, // seems not to be supported by HEIC
            xmp = xmp
        )
    }

    private fun readExifBytes(byteReader: PositionTrackingByteReader, extent: Extent): ByteArray {

        val bytesToSkip = extent.offset - byteReader.position

        byteReader.skipBytes("offset to EXIF extent", bytesToSkip.toInt())

        val tiffHeaderOffset =
            byteReader.read4BytesAsInt("tiffHeaderOffset", HEIC_BYTE_ORDER)

        /* Usualy there are 6 bytes skipped, which are the EXIF header. ("Exif.."). */
        byteReader.skipBytes("offset to TIFF header", tiffHeaderOffset)

        return byteReader.readBytes(
            extent.length.toInt() - TIFF_HEADER_OFFSET_BYTE_COUNT - tiffHeaderOffset
        )
    }

    private fun readXmpString(byteReader: PositionTrackingByteReader, extent: Extent): String {

        val bytesToSkip = extent.offset - byteReader.position

        byteReader.skipBytes("offset to MIME extent", bytesToSkip.toInt())

        val mimeBytes = byteReader.readBytes(extent.length.toInt())

        return mimeBytes.decodeToString()
    }

    /*
     * The image size is stored in an ISPE (ImageSpatialExtents) box inside the
     * IPRP (ImagePropertiesBox) boxes. There are multiple boxes of this kind if
     * the image contains thumbnails and previews. That's why we need to look up
     * which IPSE box is associated with the primary item. This relation is stored
     * in an IPMA (ItemPropertyAssociationBox) box.
     */
    private fun extractImageSize(metaBox: MetaBox): ImageSize? {

        if (metaBox.itemPropertiesBox == null)
            return null

        val primaryItemId = metaBox.primaryItemBox.itemId

        val associatedEntries =
            metaBox.itemPropertiesBox.itemPropertyAssociationBox.entries.get(primaryItemId)

        if (associatedEntries == null)
            return null

        val associatedIndexes = associatedEntries.map { it.index }

        val ipcoBoxes = metaBox.itemPropertiesBox.itemPropertyContainerBox.boxes

        /*
         * Find the first ImageSize/ISPE box that is associated with the primary item.
         * There should always be only one (or none).
         */
        val relevantImageSizeBox = ipcoBoxes.filterIndexed { index, box ->
            associatedIndexes.contains(index + 1) && box.type == BoxType.ISPE
        }.firstOrNull() as? ImageSizeBox

        if (relevantImageSizeBox != null)
            return ImageSize(
                width = relevantImageSizeBox.width,
                height = relevantImageSizeBox.height
            )

        return null
    }
}
