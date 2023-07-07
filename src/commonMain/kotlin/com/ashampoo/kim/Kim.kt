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
package com.ashampoo.kim

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor
import com.ashampoo.kim.format.jpeg.JpegRewriter
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
import com.ashampoo.kim.format.png.PngMetadataExtractor
import com.ashampoo.kim.format.raf.RafMetadataExtractor
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.KtorInputByteReader
import com.ashampoo.kim.input.PrePendingByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.kim.xmp.XmpWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.use

object Kim {

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(bytes: ByteArray): ImageMetadata? =
        if (bytes.isEmpty()) null else readMetadata(ByteArrayByteReader(bytes))

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(input: Input): ImageMetadata? =
        readMetadata(KtorInputByteReader(input))

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(byteReader: ByteReader): ImageMetadata? = byteReader.use {

        val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val imageFormat = ImageFormat.detect(headerBytes) ?: return null

        val imageParser = ImageParser.forFormat(imageFormat)

        if (imageParser == null)
            return ImageMetadata(imageFormat, null, null, null, null)

        val newReader = PrePendingByteReader(it, headerBytes.toList())

        return@use imageParser.parseMetadata(newReader)
    }

    /**
     * Determines the file type based on file header and returns metadata bytes.
     *
     * Cloud services can not reliably tell the mime type, and so we must determine it.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun extractMetadataBytes(
        byteReader: ByteReader
    ): Pair<ImageFormat?, ByteArray> = byteReader.use {

        val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val imageFormat = ImageFormat.detect(headerBytes)

        val newReader = PrePendingByteReader(it, headerBytes.toList())

        return@use when (imageFormat) {
            ImageFormat.JPEG -> imageFormat to JpegMetadataExtractor.extractMetadataBytes(newReader)
            ImageFormat.PNG -> imageFormat to PngMetadataExtractor.extractMetadataBytes(newReader)
            ImageFormat.RAF -> imageFormat to RafMetadataExtractor.extractMetadataBytes(newReader)
            else -> imageFormat to byteArrayOf()
        }
    }

    /**
     * Updates the file with the wanted updates.
     *
     * **Note**: We don't have an good API for single-shot write all fields right now.
     * So this is inefficent at this time.
     */
    fun update(
        bytes: ByteArray,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        val kimMetadata = readMetadata(bytes)

        val imageFormat = kimMetadata?.imageFormat

        if (kimMetadata?.imageFormat?.isMetadataEmbeddable() != true)
            throw ImageWriteException("Can't update file of image format: $imageFormat")

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
            newData = newIptc
        )

        return iptcByteWriter.toByteArray()
    }
}
