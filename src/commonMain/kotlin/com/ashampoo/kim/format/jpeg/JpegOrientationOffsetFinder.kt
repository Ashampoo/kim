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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.MARKER_END_OF_IMAGE
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_IDENTIFIER
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor.SEGMENT_START_OF_SCAN
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_LENGTH
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.skipBytes

/**
 * This algorithm quickly identifies the EXIF orientation offset.
 * If the file already has one no restructuring of the whole file is necessary.
 */
public object JpegOrientationOffsetFinder {

    private const val APP1_MARKER = 0xE1.toByte()

    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    @Suppress("ComplexMethod")
    public fun findOrientationOffset(
        byteReader: ByteReader
    ): Long? = tryWithImageReadException {

        val magicNumberBytes = byteReader.readBytes(ImageFormatMagicNumbers.jpeg.size).toList()

        /* Ensure it's actually a JPEG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.jpeg) {
            "JPEG magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        var positionCounter: Long = ImageFormatMagicNumbers.jpeg.size.toLong()

        @Suppress("LoopWithTooManyJumpStatements")
        do {

            var segmentIdentifier = byteReader.readByte() ?: break
            var segmentType = byteReader.readByte() ?: break

            positionCounter += 2

            /*
             * Find the segment marker. Markers are zero or more 0xFF bytes, followed by
             * a 0xFF and then a byte not equal to 0x00 or 0xFF.
             */
            while (
                segmentIdentifier != SEGMENT_IDENTIFIER ||
                segmentType == SEGMENT_IDENTIFIER ||
                segmentType.toInt() == 0
            ) {

                segmentIdentifier = segmentType

                val nextSegmentType = byteReader.readByte() ?: break

                positionCounter++

                segmentType = nextSegmentType
            }

            if (segmentType == SEGMENT_START_OF_SCAN || segmentType == MARKER_END_OF_IMAGE)
                break

            /* If we don't have anough bytes for the segment count we are done reading. */
            if (byteReader.contentLength - positionCounter < 2)
                break

            /* Note: Segment length includes size bytes */
            val segmentLength =
                byteReader.read2BytesAsInt("segmentLength", JPEG_BYTE_ORDER) - 2

            positionCounter += 2

            /* Ignore invalid segment lengths */
            if (segmentLength <= 0)
                continue

            /* We are only looking for the EXIF segment. */
            if (segmentType != APP1_MARKER) {

                val remainingByteCount = byteReader.contentLength - positionCounter

                /* Ignore invalid segment lengths */
                if (segmentLength > remainingByteCount)
                    continue

                byteReader.skipBytes("skip segment", segmentLength)

                positionCounter += segmentLength

                continue
            }

            val exifIdentifierBytes = byteReader.readBytes(
                "EXIF identifier",
                JpegConstants.EXIF_IDENTIFIER_CODE.size
            )

            positionCounter += JpegConstants.EXIF_IDENTIFIER_CODE.size

            /* Skip the APP1 XMP segment. */
            if (!exifIdentifierBytes.contentEquals(JpegConstants.EXIF_IDENTIFIER_CODE)) {

                byteReader.skipBytes(
                    "skip segment",
                    segmentLength - JpegConstants.EXIF_IDENTIFIER_CODE.size
                )

                positionCounter += segmentLength - JpegConstants.EXIF_IDENTIFIER_CODE.size

                continue
            }

            val tiffHeader = TiffReader.readTiffHeader(byteReader)

            val exifByteOrder = tiffHeader.byteOrder

            byteReader.skipBytes(
                "skip bytes to first IFD",
                tiffHeader.offsetToFirstIFD - TIFF_HEADER_SIZE
            )

            val entryCount = byteReader.read2BytesAsInt("entrycount", exifByteOrder)

            positionCounter += tiffHeader.offsetToFirstIFD + 2

            for (entryIndex in 0 until entryCount) {

                val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", exifByteOrder)

                if (tag == TiffTag.TIFF_TAG_ORIENTATION.tag) {

                    positionCounter += 8

                    if (exifByteOrder == ByteOrder.BIG_ENDIAN)
                        positionCounter++

                    return positionCounter

                } else {

                    byteReader.skipBytes("skip TIFF entry", TIFF_ENTRY_LENGTH - 2)

                    positionCounter += TIFF_ENTRY_LENGTH
                }
            }

            /*
             * We are now past the EXIF segment.
             * If we reach this point there is no orientation flag.
             */
            return null

        } while (true)

        return null
    }
}
