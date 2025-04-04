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
import com.ashampoo.kim.format.cr3.Cr3Reader
import com.ashampoo.kim.format.jxl.JxlReader
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.readRemainingBytes
import com.ashampoo.kim.input.skipBytes

/**
 * Reads containers that follow the ISO base media file format
 * as defined in ISO/IEC 14496-12.
 * Examples for these are HEIC, AVIF & JPEG XL.
 *
 * https://en.wikipedia.org/wiki/ISO_base_media_file_format
 */
public object BaseMediaFileFormatImageParser : ImageParser {

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata {

        val copyByteReader = CopyByteReader(byteReader)

        var position: Long = 0

        val allBoxes = BoxReader.readBoxes(
            byteReader = copyByteReader,
            stopAfterMetadataRead = true,
            positionOffset = 0,
            offsetShift = 0,
            updatePosition = { position = it }
        )

        if (allBoxes.isEmpty())
            throw ImageReadException("Illegal ISOBMFF: Has no boxes.")

        val fileTypeBox = allBoxes.filterIsInstance<FileTypeBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: Has no 'ftyp' Box.")

        /**
         * Handle JPEG XL
         *
         * This format has EXIF & XMP neatly in dedicated boxes, so we can just extract these.
         */
        if (fileTypeBox.majorBrand == FileTypeBox.JXL_BRAND)
            return JxlReader.createMetadata(allBoxes)

        /**
         * Handle CR3
         */
        if (fileTypeBox.majorBrand == FileTypeBox.CR3_BRAND)
            return Cr3Reader.createMetadata(allBoxes)

        val metaBox = allBoxes.filterIsInstance<MetaBox>().firstOrNull()
            ?: throw ImageReadException("Illegal ISOBMFF: Has no 'meta' Box.")

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
        val onPositionBeforeMinimumOffset = position <= minOffset

        val byteReaderToUse = if (onPositionBeforeMinimumOffset) {

            byteReader

        } else {

            /* Read all remaining bytes. */
            copyByteReader.readRemainingBytes()

            ByteArrayByteReader(copyByteReader.getBytes())
        }

        check(byteReader.contentLength == byteReaderToUse.contentLength) {
            "Content length is different: ${byteReader.contentLength} != ${byteReaderToUse.contentLength}"
        }

        if (!onPositionBeforeMinimumOffset)
            position = 0

        var exifBytes: ByteArray? = null
        var exif: TiffContents? = null
        var xmp: String? = null

        @Suppress("LoopWithTooManyJumpStatements")
        for (offset in metadataOffsets) {

            /*
             * Ignore illegal offsets.
             * endPosition is checked for negative values to also catch value overflows.
             */
            if (offset.endPosition < 0 || offset.endPosition > byteReader.contentLength)
                continue

            when (offset.type) {

                MetadataType.EXIF -> {

                    exifBytes = readExifBytes(byteReaderToUse, position, offset)

                    /* Parse EXIF in place to fail fast if reading went wrong. */
                    exif = TiffReader.read(exifBytes)

                    position = offset.endPosition
                }

                MetadataType.IPTC ->
                    continue // Unsupported

                MetadataType.XMP -> {
                    xmp = readXmpString(byteReaderToUse, position, offset)
                    position = offset.endPosition
                }
            }
        }

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
        byteReader: ByteReader,
        position: Long,
        offset: MetadataOffset
    ): ByteArray {

        val bytesToSkip = offset.offset - position

        check(bytesToSkip >= 0) {
            "Position must be before extent offset: position=$position offset=$offset"
        }

        byteReader.skipBytes("offset to EXIF extent", bytesToSkip.toInt())

        val tiffHeaderOffset =
            byteReader.read4BytesAsInt("tiffHeaderOffset", BMFF_BYTE_ORDER)

        /* Usualy there are 6 bytes skipped, which are the EXIF header. ("Exif.."). */
        byteReader.skipBytes("offset to TIFF header", tiffHeaderOffset)

        val exifBytesLength =
            offset.length.toInt() - TIFF_HEADER_OFFSET_BYTE_COUNT - tiffHeaderOffset

        return byteReader.readBytes("EXIF extent data", exifBytesLength)
    }

    private fun readXmpString(
        byteReader: ByteReader,
        position: Long,
        offset: MetadataOffset
    ): String {

        val bytesToSkip = offset.offset - position

        check(bytesToSkip >= 0) {
            "Position must be before extent offset: position=$position offset=$offset"
        }

        byteReader.skipBytes("offset to MIME extent", bytesToSkip.toInt())

        val mimeBytes = byteReader.readBytes("MIME extent data", offset.length.toInt())

        return mimeBytes.decodeToString()
    }
}
