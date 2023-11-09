/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.tryWithImageWriteException
import com.ashampoo.kim.format.MetadataUpdater
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossless
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossy
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object PngUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        updates: Set<MetadataUpdate>
    ) = tryWithImageWriteException {

        /* Prevent accidental calls that have no effect other than unnecessary work. */
        check(updates.isNotEmpty()) { "There are no updates to perform." }

        val chunks = PngImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val kimMetadata = PngImageParser.parseMetadataFromChunks(chunks)

        val xmpMeta: XMPMeta = if (kimMetadata.xmp != null)
            XMPMetaFactory.parseFromString(kimMetadata.xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

        val exifUpdates = updates.filter {
            it is MetadataUpdate.Orientation ||
                it is MetadataUpdate.TakenDate ||
                it is MetadataUpdate.GpsCoordinates
        }

        val exifBytes: ByteArray? = if (exifUpdates.isNotEmpty()) {

            val tiffOutputSet = kimMetadata.exif?.createOutputSet() ?: TiffOutputSet()

            tiffOutputSet.applyUpdates(exifUpdates)

            val oldExifBytes = kimMetadata.exifBytes

            val writer = if (oldExifBytes != null)
                TiffImageWriterLossless(exifBytes = oldExifBytes)
            else
                TiffImageWriterLossy()

            val exifBytesWriter = ByteArrayByteWriter()

            writer.write(exifBytesWriter, tiffOutputSet)

            exifBytesWriter.toByteArray()

        } else {
            null
        }

        PngWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            /*
             * IPTC is not written because it's not recognized everywhere.
             * XMP is the better choice. If users demand it we may add it.
             * The logic is already implemented.
             */
            iptcBytes = null,
            xmp = updatedXmp
        )
    }
}
