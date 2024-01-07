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
package com.ashampoo.kim.format.isobmff

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.isobmff.ISOBMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.isobmff.ISOBMFFConstants.ITEM_TYPE_EXIF
import com.ashampoo.kim.format.isobmff.ISOBMFFConstants.ITEM_TYPE_MIME
import com.ashampoo.kim.format.isobmff.ISOBMFFConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import com.ashampoo.kim.format.isobmff.boxes.Extent
import com.ashampoo.kim.format.isobmff.boxes.MetaBox
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PositionTrackingByteReader
import com.ashampoo.kim.input.PositionTrackingByteReaderDecorator

/**
 * Reads containers that follow the ISO base media file format
 * as defined in ISO/IEC 14496-12. Examples for these are MP4, HEIC & JPEG XL.
 *
 * https://en.wikipedia.org/wiki/ISO_base_media_file_format
 */
object ISOBMFFImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        parseMetadata(PositionTrackingByteReaderDecorator(byteReader))

    private fun parseMetadata(byteReader: PositionTrackingByteReaderDecorator): ImageMetadata {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            skipMediaBox = true
        )

        val metaBox = allBoxes.find { it.type == BoxType.META } as MetaBox

        var exifBytes: ByteArray? = null
        var xmp: String? = null

        for (extent in metaBox.itemLocationBox.extents) {

            val itemInfo = metaBox.itemInfoBox.map.get(extent.itemId) ?: continue

            when (itemInfo.itemType) {

                ITEM_TYPE_EXIF ->
                    exifBytes = readExifBytes(byteReader, extent)

                ITEM_TYPE_MIME ->
                    xmp = readXmpString(byteReader, extent)
            }
        }

        val exif = exifBytes?.let { TiffReader.read(exifBytes) }

        return ImageMetadata(
            imageFormat = null, // could be any ISO BMFF
            imageSize = null, // not covered by ISO BMFF
            exif = exif,
            exifBytes = exifBytes,
            iptc = null, // not covered by ISO BMFF
            xmp = xmp
        )
    }

    private fun readExifBytes(byteReader: PositionTrackingByteReader, extent: Extent): ByteArray {

        val bytesToSkip = extent.offset - byteReader.position

        byteReader.skipBytes("offset to EXIF extent", bytesToSkip.toInt())

        val tiffHeaderOffset =
            byteReader.read4BytesAsInt("tiffHeaderOffset", BMFF_BYTE_ORDER)

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
}
