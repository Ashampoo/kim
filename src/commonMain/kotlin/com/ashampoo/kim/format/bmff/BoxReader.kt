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
package com.ashampoo.kim.format.bmff

import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.format.bmff.box.FileTypeBox
import com.ashampoo.kim.format.bmff.box.HandlerReferenceBox
import com.ashampoo.kim.format.bmff.box.ItemInfoEntryBox
import com.ashampoo.kim.format.bmff.box.ItemInformationBox
import com.ashampoo.kim.format.bmff.box.ItemLocationBox
import com.ashampoo.kim.format.bmff.box.MediaDataBox
import com.ashampoo.kim.format.bmff.box.MetaBox
import com.ashampoo.kim.format.bmff.box.MovieBox
import com.ashampoo.kim.format.bmff.box.PrimaryItemBox
import com.ashampoo.kim.format.bmff.box.UuidBox
import com.ashampoo.kim.format.jxl.box.CompressedBox
import com.ashampoo.kim.format.jxl.box.ExifBox
import com.ashampoo.kim.format.jxl.box.JxlParticalCodestreamBox
import com.ashampoo.kim.format.jxl.box.XmlBox
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.read8BytesAsLong
import com.ashampoo.kim.input.readBytes

/**
 * Reads ISOBMFF boxes
 */
public object BoxReader {

    /**
     * @param byteReader The reader as source for the bytes
     * @param stopAfterMetadataRead If reading the file for metadata on the highest level we
     * want to stop reading after the meta boxes to prevent reading the whole image data block in.
     * For iPhone HEIC this is possible, but Samsung HEIC has "meta" coming after "mdat"
     */
    public fun readBoxes(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false,
        positionOffset: Long = 0,
        offsetShift: Long = 0,
        updatePosition: ((Long) -> Unit)? = null
    ): List<Box> {

        var haveSeenJxlHeaderBox = false

        val boxes = mutableListOf<Box>()

        var position: Long = positionOffset

        while (true) {

            val available = byteReader.contentLength - position

            /*
             * Check if there are enough bytes for another box.
             * If so, we at least need the 8 header bytes.
             */
            if (available < BMFFConstants.BOX_HEADER_LENGTH)
                break

            val offset: Long = position

            /* Note: The length includes the 8 header bytes. */
            val size: Long =
                byteReader.read4BytesAsInt("length", BMFF_BYTE_ORDER).toLong()

            val type = BoxType.of(
                byteReader.readBytes("type", BMFFConstants.TPYE_LENGTH)
            )

            position += BMFFConstants.BOX_HEADER_LENGTH

            /*
             * If we read an JXL file and we already have seen the header,
             * all reamining JXLP boxes are image data that we can skip.
             */
            if (stopAfterMetadataRead && type == BoxType.JXLP && haveSeenJxlHeaderBox)
                break

            var largeSize: Long? = null

            val actualLength: Long = when (size) {

                /* A vaule of zero indicates that it's the last box. */
                0L -> available

                /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
                1L -> {
                    largeSize = byteReader.read8BytesAsLong("length", BMFF_BYTE_ORDER)
                    largeSize
                }

                /* Keep the length we already read. */
                else -> size
            }

            val nextBoxOffset = offset + actualLength

            @Suppress("MagicNumber")
            if (size == 1L)
                position += 8

            val remainingBytesToReadInThisBox = (nextBoxOffset - position).toInt()

            val bytes = byteReader.readBytes("data", remainingBytesToReadInThisBox)

            position += remainingBytesToReadInThisBox

            val globalOffset = offset + offsetShift

            val box = when (type) {
                /* Generic EIC/ISO 14496-12 boxes. */
                BoxType.FTYP -> FileTypeBox(globalOffset, size, largeSize, bytes)
                BoxType.META -> MetaBox(globalOffset, size, largeSize, bytes)
                BoxType.HDLR -> HandlerReferenceBox(globalOffset, size, largeSize, bytes)
                BoxType.IINF -> ItemInformationBox(globalOffset, size, largeSize, bytes)
                BoxType.INFE -> ItemInfoEntryBox(globalOffset, size, largeSize, bytes)
                BoxType.ILOC -> ItemLocationBox(globalOffset, size, largeSize, bytes)
                BoxType.PITM -> PrimaryItemBox(globalOffset, size, largeSize, bytes)
                BoxType.MDAT -> MediaDataBox(globalOffset, size, largeSize, bytes)
                BoxType.MOOV -> MovieBox(globalOffset, size, largeSize, bytes)
                BoxType.UUID -> UuidBox(globalOffset, size, largeSize, bytes)
                /* JXL boxes */
                BoxType.EXIF -> ExifBox(globalOffset, size, largeSize, bytes)
                BoxType.XML -> XmlBox(globalOffset, size, largeSize, bytes)
                BoxType.JXLP -> JxlParticalCodestreamBox(globalOffset, size, largeSize, bytes)
                BoxType.BROB -> CompressedBox(globalOffset, size, largeSize, bytes)
                /* Unknown box */
                else -> Box(type, globalOffset, size, largeSize, bytes)
            }

            boxes.add(box)

            if (stopAfterMetadataRead) {

                /* This is the case for HEIC & AVIF */
                if (type == BoxType.META)
                    break

//                /* This is the case for CR3 */
//                if (type == BoxType.MOOV)
//                    break

                /*
                 * When parsing JXL we need to take a note that we saw the header.
                 * This is usually the first JXLP box.
                 */
                if (type == BoxType.JXLP) {

                    box as JxlParticalCodestreamBox

                    if (box.isHeader)
                        haveSeenJxlHeaderBox = true
                }
            }
        }

        updatePosition?.let { it(position) }

        return boxes
    }
}
