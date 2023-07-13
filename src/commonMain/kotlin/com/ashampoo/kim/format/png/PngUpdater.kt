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
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossless
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossy
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object PngUpdater {

    fun update(
        bytes: ByteArray,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        if (updates.isEmpty())
            return bytes

        val kimMetadata = Kim.readMetadata(bytes)

        if (kimMetadata == null)
            throw ImageWriteException("Could not read file.")

        if (kimMetadata.imageFormat != ImageFormat.PNG)
            throw ImageWriteException("Can only update PNG.")

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

        } else
            null

        val byteWriter = ByteArrayByteWriter()

        PngWriter.writeImage(
            byteWriter = byteWriter,
            originalBytes = bytes,
            exifBytes = exifBytes,
            /*
             * IPTC is not written because it's not recognized everywhere.
             * XMP is the better choice. If users demand it we may add it.
             * The logic is already implemented.
             */
            iptcBytes = null,
            xmp = updatedXmp
        )

        return byteWriter.toByteArray()
    }
}
