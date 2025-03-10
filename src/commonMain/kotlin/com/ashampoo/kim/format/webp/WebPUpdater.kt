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
package com.ashampoo.kim.format.webp

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.startsWithNullable
import com.ashampoo.kim.common.tryWithImageWriteException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.MetadataUpdater
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.tiff.write.TiffWriterBase
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object WebPUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ) = tryWithImageWriteException {

        val chunks = WebPImageParser.readChunks(byteReader, stopAfterMetadataRead = false)

        val metadata = WebPImageParser.parseMetadataFromChunks(chunks)

        val xmpMeta: XMPMeta = if (metadata.xmp != null)
            XMPMetaFactory.parseFromString(metadata.xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, update, true)

        val isExifUpdate =
            update is MetadataUpdate.Orientation ||
                update is MetadataUpdate.TakenDate ||
                update is MetadataUpdate.Description ||
                update is MetadataUpdate.GpsCoordinates ||
                update is MetadataUpdate.GpsCoordinatesAndLocationShown

        val exifBytes: ByteArray? = if (isExifUpdate) {

            val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

            outputSet.applyUpdate(update)

            val exifBytesWriter = ByteArrayByteWriter()

            TiffWriterBase
                .createTiffWriter(
                    byteOrder = outputSet.byteOrder,
                    oldExifBytes = metadata.exifBytes
                )
                .write(exifBytesWriter, outputSet)

            exifBytesWriter.toByteArray()

        } else {
            null
        }

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            xmp = updatedXmp
        )
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWithNullable(ImageFormatMagicNumbers.webP))
            throw ImageWriteException("Provided input bytes are not WebP!")

        val byteReader = ByteArrayByteReader(bytes)

        val chunks = WebPImageParser.readChunks(byteReader, stopAfterMetadataRead = false)

        val metadata = WebPImageParser.parseMetadataFromChunks(chunks)

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val exifBytesWriter = ByteArrayByteWriter()

        TiffWriterBase
            .createTiffWriter(
                byteOrder = outputSet.byteOrder,
                oldExifBytes = metadata.exifBytes
            )
            .write(exifBytesWriter, outputSet)

        val exifBytes = exifBytesWriter.toByteArray()

        val byteWriter = ByteArrayByteWriter()

        WebPWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            exifBytes = exifBytes,
            xmp = null // No change to XMP
        )

        return@tryWithImageWriteException byteWriter.toByteArray()
    }
}
