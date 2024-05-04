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
package com.ashampoo.kim.format.jxl

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.bmff.BMFFConstants
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.format.jxl.box.CompressedBox
import com.ashampoo.kim.format.jxl.box.JxlParticalCodestreamBox
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.kim.output.writeInt
import com.ashampoo.kim.output.writeLong
import kotlin.jvm.JvmStatic

public object JxlWriter {

    /*
     * As a safety measure we don't want to write uncompressed boxes to
     * a file that already has compressed boxes. This might cause data loss.
     */
    private const val BROB_WARNING =
        "This file contains compressed data we can't yet read. " +
            "Writing to this file will result in data loss. " +
            "Please only update uncompressed metadata for now."

    @JvmStatic
    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ): Unit = writeImage(
        boxes = BoxReader.readBoxes(byteReader, false),
        byteWriter = byteWriter,
        exifBytes = exifBytes,
        xmp = xmp
    )

    @JvmStatic
    public fun writeImage(
        boxes: List<Box>,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        val modifiedBoxes = boxes.toMutableList()

        /*
         * Security check first
         *
         * TODO Remove this once we have brotli support.
         */

        val compressedBoxes = modifiedBoxes.filterIsInstance<CompressedBox>()

        if (compressedBoxes.isNotEmpty()) {

            if (exifBytes != null && compressedBoxes.any { it.actualType == BoxType.EXIF })
                throw ImageWriteException(BROB_WARNING)

            if (xmp != null && compressedBoxes.any { it.actualType == BoxType.XML })
                throw ImageWriteException(BROB_WARNING)
        }

        /*
         * Delete old boxes that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedBoxes.removeAll { it.type == BoxType.EXIF }

        if (xmp != null)
            modifiedBoxes.removeAll { it.type == BoxType.XML }

        /*
         * Write the new file
         *
         * We look first if there is a JXLP header box.
         * If so, this is the right place to insert metadata after.
         * Otherwise we insert right after FTYP.
         */
        val jxlpHeaderBox =
            modifiedBoxes.filterIsInstance<JxlParticalCodestreamBox>().firstOrNull { it.isHeader }

        for (box in modifiedBoxes) {

            byteWriter.writeInt(
                box.size.toInt(),
                BMFFConstants.BMFF_BYTE_ORDER
            )

            byteWriter.write(box.type.bytes)

            box.largeSize?.let {
                byteWriter.writeLong(
                    box.largeSize,
                    BMFFConstants.BMFF_BYTE_ORDER
                )
            }

            byteWriter.write(box.payload)

            val shouldInsertMetadata =
                jxlpHeaderBox != null && box == jxlpHeaderBox ||
                    jxlpHeaderBox == null && box.type == BoxType.FTYP

            if (shouldInsertMetadata) {

                if (exifBytes != null) {

                    val size = BMFFConstants.BOX_HEADER_LENGTH + 4 + exifBytes.size

                    byteWriter.writeInt(size, BMFFConstants.BMFF_BYTE_ORDER)
                    byteWriter.write(BoxType.EXIF.bytes)

                    /* Version and flags, all zeros. */
                    byteWriter.writeInt(0, BMFFConstants.BMFF_BYTE_ORDER)

                    byteWriter.write(exifBytes)
                }

                if (xmp != null) {

                    val xmpBytes = xmp.encodeToByteArray()

                    val size = BMFFConstants.BOX_HEADER_LENGTH + xmpBytes.size

                    byteWriter.writeInt(size, BMFFConstants.BMFF_BYTE_ORDER)

                    byteWriter.write(BoxType.XML.bytes)

                    byteWriter.write(xmpBytes)
                }
            }
        }

        byteWriter.close()
    }
}
