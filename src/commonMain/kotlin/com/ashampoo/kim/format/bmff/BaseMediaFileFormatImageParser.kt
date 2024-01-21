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
package com.ashampoo.kim.format.bmff

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.MetadataOffset
import com.ashampoo.kim.common.MetadataType
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BMFFConstants.TIFF_HEADER_OFFSET_BYTE_COUNT
import com.ashampoo.kim.format.bmff.box.FileTypeBox
import com.ashampoo.kim.format.bmff.box.MetaBox
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.PositionTrackingByteReader
import com.ashampoo.kim.input.PositionTrackingByteReaderDecorator

/**
 * Reads containers that follow the ISO base media file format
 * as defined in ISO/IEC 14496-12.
 * Examples for these are HEIC, AVIF & JPEG XL.
 *
 * https://en.wikipedia.org/wiki/ISO_base_media_file_format
 */
object BaseMediaFileFormatImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        parseMetadata(PositionTrackingByteReaderDecorator(byteReader))

    private fun parseMetadata(byteReader: PositionTrackingByteReader): ImageMetadata {

        val copyByteReader = CopyByteReader(byteReader)

        val allBoxes = BoxReader.readBoxes(
            byteReader = copyByteReader,
            stopAfterMetadataRead = true,
            offsetShift = 0
        )

        if (allBoxes.isEmpty())
            throw ImageReadException("Illegal ISOBMFF: Has no boxes.")

        val fileTypeBox = allBoxes.find { it.type == BoxType.FTYP } as? FileTypeBox

        if (fileTypeBox == null)
            throw ImageReadException("Illegal ISOBMFF: Has no 'ftyp' Box.")

        /**
         * Handle JPEG XL
         *
         * This format has EXIF & XMP neatly in dedicated boxes, so we can just extract these.
         */
        if (fileTypeBox.majorBrand == FileTypeBox.JXL_BRAND)
            return JxlHandler.createMetadata(allBoxes)

        val metaBox = allBoxes.find { it.type == BoxType.META } as? MetaBox

        if (metaBox == null)
            throw ImageReadException("Illegal ISOBMFF: Has no 'meta' Box.")

        val metadataOffsets = metaBox.findMetadataOffsets()

        /* Return empty object if no metadata is found. */
        if (metadataOffsets.isEmpty())
            return ImageMetadata(
                imageFormat = null,
                imageSize = null,
                exif = null,
                exifBytes = null,
                iptc = null,
                xmp = null
            )

        val minOffset = metadataOffsets.first().offset

        /*
         * In case of Samsung Galaxy HEIC files the mdat Box comes
         * before the meta Box. We need to reset the reader here,
         * but as we may read from a cloud stream we really don't
         * have a "reset" function.
         *
         * We currently do this by having a copy of all bytes
         * in buffer and input everything we read so far in again.
         * FIXME There must be a better solution. Find it.
         */
        val byteReaderToUse = if (byteReader.position <= minOffset)
            byteReader
        else
            ByteArrayByteReader(copyByteReader.getBytes())

        var exifBytes: ByteArray? = null
        var xmp: String? = null

        for (offset in metadataOffsets) {

            when (offset.type) {

                MetadataType.EXIF ->
                    exifBytes = readExifBytes(byteReaderToUse, offset)

                MetadataType.IPTC ->
                    continue // Unsupported

                MetadataType.XMP ->
                    xmp = readXmpString(byteReaderToUse, offset)
            }
        }

        val exif = exifBytes?.let { TiffReader.read(exifBytes) }

        return ImageMetadata(
            imageFormat = null, // could be any ISO BMFF
            imageSize = null, // not covered by ISO BMFF
            exif = exif,
            exifBytes = exifBytes,
            iptc = null, // not supported by ISO BMFF
            xmp = xmp
        )
    }

    private fun readExifBytes(
        byteReader: PositionTrackingByteReader,
        offset: MetadataOffset
    ): ByteArray {

        val bytesToSkip = offset.offset - byteReader.position

        byteReader.skipBytes("offset to EXIF extent", bytesToSkip.toInt())

        val tiffHeaderOffset =
            byteReader.read4BytesAsInt("tiffHeaderOffset", BMFF_BYTE_ORDER)

        /* Usualy there are 6 bytes skipped, which are the EXIF header. ("Exif.."). */
        byteReader.skipBytes("offset to TIFF header", tiffHeaderOffset)

        return byteReader.readBytes(
            offset.length.toInt() - TIFF_HEADER_OFFSET_BYTE_COUNT - tiffHeaderOffset
        )
    }

    private fun readXmpString(
        byteReader: PositionTrackingByteReader,
        offset: MetadataOffset
    ): String {

        val bytesToSkip = offset.offset - byteReader.position

        byteReader.skipBytes("offset to MIME extent", bytesToSkip.toInt())

        val mimeBytes = byteReader.readBytes(offset.length.toInt())

        return mimeBytes.decodeToString()
    }
}
