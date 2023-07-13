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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object JpegUpdater {

    fun update(
        bytes: ByteArray,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        if (updates.isEmpty())
            return bytes

        val kimMetadata = Kim.readMetadata(bytes)

        if (kimMetadata == null)
            throw ImageWriteException("Could not read file.")

        if (kimMetadata.imageFormat != ImageFormat.JPEG)
            throw ImageWriteException("Can only update JPEG.")

        val xmpUpdatedBytes = updateXmp(bytes, kimMetadata.xmp, updates)

        val exifUpdatedBytes = updateExif(xmpUpdatedBytes, kimMetadata.exif, updates)

        val iptcUpdatedBytes = updateIptc(exifUpdatedBytes, kimMetadata.iptc, updates)

        return iptcUpdatedBytes
    }

    private fun updateXmp(inputBytes: ByteArray, xmp: String?, updates: Set<MetadataUpdate>): ByteArray {

        val xmpMeta: XMPMeta = if (xmp != null)
            XMPMetaFactory.parseFromString(xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            xmpXml = updatedXmp
        )

        return byteWriter.toByteArray()
    }

    private fun updateExif(
        inputBytes: ByteArray,
        exif: TiffContents?,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        /*
         * Filter out all updates we can perform on EXIF.
         */
        val exifUpdates = updates.filter {
            it is MetadataUpdate.Orientation ||
                it is MetadataUpdate.TakenDate ||
                it is MetadataUpdate.GpsCoordinates
        }

        if (exifUpdates.isEmpty())
            return inputBytes

        val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.applyUpdates(exifUpdates)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        return byteWriter.toByteArray()
    }

    private fun updateIptc(
        inputBytes: ByteArray,
        iptc: IptcMetadata?,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        val keywordsUpdate = updates.filterIsInstance<MetadataUpdate.Keywords>().firstOrNull()

        if (keywordsUpdate == null)
            return inputBytes

        /* Update IPTC keywords */

        val newKeywords = keywordsUpdate.keywords

        val newBlocks = iptc?.nonIptcBlocks ?: emptyList()
        val oldRecords = iptc?.records ?: emptyList()

        val newRecords = oldRecords.filter { it.iptcType != IptcTypes.KEYWORDS }.toMutableList()

        for (keyword in newKeywords.sorted())
            newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keyword))

        val newIptc = IptcMetadata(newRecords, newBlocks)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            metadata = newIptc
        )

        return byteWriter.toByteArray()
    }
}
