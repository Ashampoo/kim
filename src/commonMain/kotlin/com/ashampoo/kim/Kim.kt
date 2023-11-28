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
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.common.tryWithImageWriteException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.arw.ArwPreviewExtractor
import com.ashampoo.kim.format.cr2.Cr2PreviewExtractor
import com.ashampoo.kim.format.dng.DngPreviewExtractor
import com.ashampoo.kim.format.jpeg.JpegMetadataExtractor
import com.ashampoo.kim.format.jpeg.JpegUpdater
import com.ashampoo.kim.format.nef.NefPreviewExtractor
import com.ashampoo.kim.format.png.PngMetadataExtractor
import com.ashampoo.kim.format.png.PngUpdater
import com.ashampoo.kim.format.raf.RafMetadataExtractor
import com.ashampoo.kim.format.raf.RafPreviewExtractor
import com.ashampoo.kim.format.rw2.Rw2PreviewExtractor
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.DefaultRandomAccessByteReader
import com.ashampoo.kim.input.KotlinIoSourceByteReader
import com.ashampoo.kim.input.KtorByteReadChannelByteReader
import com.ashampoo.kim.input.KtorInputByteReader
import com.ashampoo.kim.input.PrePendingByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.use
import kotlinx.io.files.Path

object Kim {

    var underUnitTesting: Boolean = false

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(bytes: ByteArray): ImageMetadata? =
        if (bytes.isEmpty())
            null
        else
            readMetadata(ByteArrayByteReader(bytes))

    @OptIn(ExperimentalStdlibApi::class)
    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(path: Path): ImageMetadata? = tryWithImageReadException {

        KotlinIoSourceByteReader.read(path) { byteReader ->
            byteReader?.let { readMetadata(it) }
        }
    }

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(byteReadPacket: ByteReadPacket): ImageMetadata? =
        readMetadata(KtorInputByteReader(byteReadPacket))

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(byteReadChannel: ByteReadChannel, contentLength: Long): ImageMetadata? =
        readMetadata(KtorByteReadChannelByteReader(byteReadChannel, contentLength))

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun readMetadata(
        byteReader: ByteReader
    ): ImageMetadata? = tryWithImageReadException {

        byteReader.use {

            val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

            val imageFormat = ImageFormat.detect(headerBytes) ?: return@use null

            val imageParser = ImageParser.forFormat(imageFormat)

            if (imageParser == null)
                return@use ImageMetadata(imageFormat, null, null, null, null, null)

            val newReader = PrePendingByteReader(it, headerBytes.toList())

            /*
             * We re-apply the ImageFormat, because we don't want to report
             * "TIFF" for every TIFF-based RAW format like CR2.
             */
            return@use imageParser
                .parseMetadata(newReader)
                .copy(imageFormat = imageFormat)
        }
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
    ): Pair<ImageFormat?, ByteArray> = tryWithImageReadException {

        byteReader.use {

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
    }

    @kotlin.jvm.JvmStatic
    @Throws(ImageReadException::class)
    fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        byteReader.use {

            val headerBytes = it.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

            val imageFormat = ImageFormat.detect(headerBytes)

            val prePendingByteReader = PrePendingByteReader(it, headerBytes.toList())

            if (imageFormat == ImageFormat.RAF)
                return@use RafPreviewExtractor.extractPreviewImage(prePendingByteReader)

            val reader = DefaultRandomAccessByteReader(prePendingByteReader)

            val tiffContents = TiffReader.read(reader)

            /**
             * *Note:* Olympus ORF is currently unsupported because the preview offset
             * is burried in the Olympus MakerNotes, which are currently not interpreted.
             */
            return@use when (imageFormat) {
                ImageFormat.CR2 -> Cr2PreviewExtractor.extractPreviewImage(tiffContents, reader)
                ImageFormat.RW2 -> Rw2PreviewExtractor.extractPreviewImage(tiffContents, reader)
                ImageFormat.TIFF -> {

                    /* It can now be DNG, NEF or ARW. */
                    DngPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                    NefPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                    ArwPreviewExtractor.extractPreviewImage(tiffContents, reader)?.let { return@use it }
                }

                else -> null
            }
        }
    }

    /**
     * Updates the file with the desired change.
     *
     * **Note**: This method is provided for convenience, but it's not recommended for
     * very large image files that should not be entirely loaded into memory.
     * Currently, the update logic reads the entire file, which may not be efficient
     * for large files. Please be aware that this behavior is subject to change in
     * future updates.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    fun update(
        bytes: ByteArray,
        update: MetadataUpdate
    ): ByteArray = tryWithImageWriteException {

        val byteArrayByteWriter = ByteArrayByteWriter()

        update(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteArrayByteWriter,
            update = update
        )

        return@tryWithImageWriteException byteArrayByteWriter.toByteArray()
    }

    /**
     * Updates the file with the desired change.
     *
     * **Note**: We don't have an good API for single-shot write all fields right now.
     * So this is inefficent at this time as it reads the whole file in.
     *
     * But this already represents the planned future API for streaming updates.
     */
    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ) = tryWithImageWriteException {

        val headerBytes = byteReader.readBytes(ImageFormat.REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)

        val imageFormat = ImageFormat.detect(headerBytes)

        val prePendingByteReader = PrePendingByteReader(byteReader, headerBytes.toList())

        return@tryWithImageWriteException when (imageFormat) {
            ImageFormat.JPEG -> JpegUpdater.update(prePendingByteReader, byteWriter, update)
            ImageFormat.PNG -> PngUpdater.update(prePendingByteReader, byteWriter, update)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't embed metadata into $imageFormat.")
        }
    }

    @kotlin.jvm.JvmStatic
    @Throws(ImageWriteException::class)
    fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        val imageFormat = ImageFormat.detect(bytes)

        return@tryWithImageWriteException when (imageFormat) {
            ImageFormat.JPEG -> JpegUpdater.updateThumbnail(bytes, thumbnailBytes)
            ImageFormat.PNG -> PngUpdater.updateThumbnail(bytes, thumbnailBytes)
            null -> throw ImageWriteException("Unknown or unsupported file format.")
            else -> throw ImageWriteException("Can't embed thumbnail into $imageFormat.")
        }
    }
}
