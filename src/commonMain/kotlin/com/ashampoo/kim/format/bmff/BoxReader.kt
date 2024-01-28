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
import com.ashampoo.kim.format.bmff.box.PrimaryItemBox
import com.ashampoo.kim.format.jxl.box.ExifBox
import com.ashampoo.kim.format.jxl.box.JxlParticalCodestreamBox
import com.ashampoo.kim.format.jxl.box.XmlBox
import com.ashampoo.kim.input.PositionTrackingByteReader

/**
 * Reads ISOBMFF boxes
 */
object BoxReader {

    /**
     * @param byteReader The reader as source for the bytes
     * @param stopAfterMetadataRead If reading the file for metadata on the highest level we
     * want to stop reading after the meta boxes to prevent reading the whole image data block in.
     * For iPhone HEIC this is possible, but Samsung HEIC has "meta" coming after "mdat"
     */
    fun readBoxes(
        byteReader: PositionTrackingByteReader,
        stopAfterMetadataRead: Boolean = false,
        offsetShift: Long = 0
    ): List<Box> {

        var haveSeenJxlHeaderBox: Boolean = false

        val boxes = mutableListOf<Box>()

        while (true) {

            /*
             * Check if there are enough bytes for another box.
             * If so, we at least need the 8 header bytes.
             */
            if (byteReader.available < BMFFConstants.BOX_HEADER_LENGTH)
                break

            val offset: Long = byteReader.position.toLong()

            /* Note: The length includes the 8 header bytes. */
            val length: Long =
                byteReader.read4BytesAsInt("length", BMFF_BYTE_ORDER).toLong()

            val type = BoxType.of(
                byteReader.readBytes("type", BMFFConstants.TPYE_LENGTH)
            )

            /*
             * If we read an JXL file and we already have seen the header,
             * all reamining JXLP boxes are image data that we can skip.
             */
            if (stopAfterMetadataRead && type == BoxType.JXLP && haveSeenJxlHeaderBox)
                break

            val actualLength: Long = when (length) {

                /* A vaule of zero indicates that it's the last box. */
                0L -> byteReader.available

                /* A length of 1 indicates that we should read the next 8 bytes to get a long value. */
                1L -> byteReader.read8BytesAsLong("length", BMFF_BYTE_ORDER)

                /* Keep the length we already read. */
                else -> length
            }

            val nextBoxOffset = offset + actualLength

            val remainingBytesToReadInThisBox = (nextBoxOffset - byteReader.position).toInt()

            val bytes = byteReader.readBytes("data", remainingBytesToReadInThisBox)

            val globalOffset = offset + offsetShift

            val box = when (type) {
                BoxType.FTYP -> FileTypeBox(globalOffset, actualLength, bytes)
                BoxType.META -> MetaBox(globalOffset, actualLength, bytes)
                BoxType.HDLR -> HandlerReferenceBox(globalOffset, actualLength, bytes)
                BoxType.IINF -> ItemInformationBox(globalOffset, actualLength, bytes)
                BoxType.INFE -> ItemInfoEntryBox(globalOffset, actualLength, bytes)
                BoxType.ILOC -> ItemLocationBox(globalOffset, actualLength, bytes)
                BoxType.PITM -> PrimaryItemBox(globalOffset, actualLength, bytes)
                BoxType.MDAT -> MediaDataBox(globalOffset, actualLength, bytes)
                BoxType.EXIF -> ExifBox(globalOffset, actualLength, bytes)
                BoxType.XML -> XmlBox(globalOffset, actualLength, bytes)
                BoxType.JXLP -> JxlParticalCodestreamBox(globalOffset, actualLength, bytes)
                else -> Box(type, globalOffset, actualLength, bytes)
            }

            boxes.add(box)

            if (stopAfterMetadataRead) {

                /* This is the case for HEIC & AVIF */
                if (type == BoxType.META)
                    break

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

        return boxes
    }
}
