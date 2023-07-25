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
import com.ashampoo.kim.format.cr2.Cr2PreviewExtractor
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor
import com.ashampoo.kim.format.jpeg.JpegUpdater
import com.ashampoo.kim.format.nef.NefPreviewExtractor
import com.ashampoo.kim.format.png.PngMetadataExtractor
import com.ashampoo.kim.format.png.PngUpdater
import com.ashampoo.kim.format.raf.RafMetadataExtractor
import com.ashampoo.kim.format.raf.RafPreviewExtractor
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.KtorInputByteReader
import com.ashampoo.kim.input.PrePendingByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.use

object Kim {

    var underUnitTesting: Boolean = false

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(bytes: ByteArray): ImageMetadata? =
        if (bytes.isEmpty()) null else readMetadata(ByteArrayByteReader(bytes), bytes.size.toLong())

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(input: Input): ImageMetadata? =
        readMetadata(KtorInputByteReader(input), input.remaining)

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(byteReader: ByteReader, length: Long): ImageMetadata? = byteReader.use {

        val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val imageFormat = ImageFormat.detect(headerBytes) ?: return null

        val imageParser = ImageParser.forFormat(imageFormat)

        if (imageParser == null)
            return ImageMetadata(imageFormat, null, null, null, null, null)

        val newReader = PrePendingByteReader(it, headerBytes.toList())

        /*
         * We re-apply the ImageFormat, because we don't want to report
         * "TIFF" for every TIFF-based RAW format like CR2.
         */
        return@use imageParser
            .parseMetadata(newReader, length)
            .copy(imageFormat = imageFormat)
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

    @Throws(ImageReadException::class)
    fun extractPreviewImage(
        byteReader: ByteReader,
        length: Long
    ): ByteArray? = byteReader.use {

        val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val imageFormat = ImageFormat.detect(headerBytes)

        val newReader = PrePendingByteReader(it, headerBytes.toList())

        return@use when (imageFormat) {
            ImageFormat.CR2 -> Cr2PreviewExtractor.extractPreviewImage(newReader, length)
            ImageFormat.RAF -> RafPreviewExtractor.extractPreviewImage(newReader, length)

            /* Unfortunately NEF has no separate magic bytes */
            ImageFormat.TIFF -> NefPreviewExtractor.extractPreviewImage(newReader, length)
            else -> null
        }
    }

    /**
     * Updates the file with the wanted updates.
     *
     * **Note**: We don't have an good API for single-shot write all fields right now.
     * So this is inefficent at this time. This method is experimental and will likely change.
     */
    fun update(
        bytes: ByteArray,
        updates: Set<MetadataUpdate>
    ): ByteArray {

        if (updates.isEmpty())
            return bytes

        val imageFormat = ImageFormat.detect(bytes)

        return when (imageFormat) {
            ImageFormat.JPEG -> JpegUpdater.update(bytes, updates)
            ImageFormat.PNG -> PngUpdater.update(bytes, updates)
            null -> throw ImageWriteException("Unsupported/Undetected file format.")
            else -> throw ImageWriteException("Can't embedd into $imageFormat")
        }
    }
}
