/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
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

        val kimMetadata = Kim.readMetadata(bytes)

        if (kimMetadata == null)
            throw ImageWriteException("Could not read file.")

        if (kimMetadata.imageFormat != ImageFormat.JPEG)
            throw ImageWriteException("Can only update JPEG.")

        /*
         * Update XMP
         */

        val xmpMeta: XMPMeta = if (kimMetadata.xmp != null)
            XMPMetaFactory.parseFromString(kimMetadata.xmp)
        else
            XMPMetaFactory.create()

        val newXmp = XmpWriter.updateXmp(xmpMeta, updates, true)

        val xmpByteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = xmpByteWriter,
            xmpXml = newXmp
        )

        val xmpUpdatedBytes = xmpByteWriter.toByteArray()

        /* Update EXIF */

        // TODO
//        val orientationUpdate = updates.filterIsInstance<MetadataUpdate.Orientation>().firstOrNull()
//
//        val exifByteWriter = ByteArrayByteWriter()
//
//        JpegRewriter.updateExifMetadataLossless(
//            byteReader = ByteArrayByteReader(xmpUpdatedBytes),
//            byteWriter = exifByteWriter,
//            outputSet = kimMetadata.exif?.createOutputSet()
//        )
//
//        val exifUpdatesBytes = exifByteWriter.toByteArray()

        val keywordsUpdate = updates.filterIsInstance<MetadataUpdate.Keywords>().firstOrNull()

        /* No keywords to update in IPTC? In that case we are done. */
        if (keywordsUpdate == null)
            return xmpUpdatedBytes

        /* Update IPTC keywords */

        val newKeywords = keywordsUpdate.keywords

        val oldIptc = kimMetadata.iptc

        val newBlocks = oldIptc?.nonIptcBlocks ?: emptyList()
        val oldRecords = oldIptc?.records ?: emptyList()

        val newRecords = oldRecords.filter { it.iptcType != IptcTypes.KEYWORDS }.toMutableList()

        for (keyword in newKeywords.sorted())
            newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keyword))

        val newIptc = IptcMetadata(newRecords, newBlocks)

        val iptcByteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(xmpUpdatedBytes),
            byteWriter = iptcByteWriter,
            metadata = newIptc
        )

        return iptcByteWriter.toByteArray()
    }
}
