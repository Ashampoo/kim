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
package com.ashampoo.kim.format.webp

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.webp.WebPConstants.CHUNK_SIZE_LENGTH
import com.ashampoo.kim.format.webp.WebPConstants.RIFF_SIGNATURE
import com.ashampoo.kim.format.webp.WebPConstants.TPYE_LENGTH
import com.ashampoo.kim.format.webp.WebPConstants.WEBP_BYTE_ORDER
import com.ashampoo.kim.format.webp.WebPConstants.WEBP_SIGNATURE
import com.ashampoo.kim.format.webp.chunk.ImageSizeAware
import com.ashampoo.kim.format.webp.chunk.WebPChunk
import com.ashampoo.kim.format.webp.chunk.WebPChunkExif
import com.ashampoo.kim.format.webp.chunk.WebPChunkVP8
import com.ashampoo.kim.format.webp.chunk.WebPChunkVP8L
import com.ashampoo.kim.format.webp.chunk.WebPChunkVP8X
import com.ashampoo.kim.format.webp.chunk.WebPChunkXmp
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat

object WebPImageParser : ImageParser {

    /*
     * https://developers.google.com/speed/webp/docs/riff_container
     */
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            val chunks = readChunks(
                byteReader = byteReader,
                stopAfterMetadataRead = true
            )

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            val imageSizeAwareChunk = chunks.filterIsInstance<ImageSizeAware>().firstOrNull()

            checkNotNull(imageSizeAwareChunk) {
                "Did not find a header chunk containing the image size. " +
                    "Found chunk types: ${chunks.map { it.type }}"
            }

            val imageSize = imageSizeAwareChunk.imageSize

            val exifChunk = chunks.filterIsInstance<WebPChunkExif>().firstOrNull()
            val xmpChunk = chunks.filterIsInstance<WebPChunkXmp>().firstOrNull()

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.WEBP,
                imageSize = imageSize,
                exif = exifChunk?.tiffContents,
                exifBytes = exifChunk?.bytes,
                iptc = null, // not supported by WebP
                xmp = xmpChunk?.xmp
            )
        }

    fun readChunks(
        byteReader: ByteReader,
        stopAfterMetadataRead: Boolean = false
    ): List<WebPChunk> = tryWithImageReadException {

        byteReader.readAndVerifyBytes("RIFF signature", RIFF_SIGNATURE)

        val length = byteReader.read4BytesAsInt("length", WEBP_BYTE_ORDER)



        byteReader.readAndVerifyBytes("WEBP signature", WEBP_SIGNATURE)

        return readChunksInternal(
            byteReader = byteReader,
            bytesToRead = length - WEBP_SIGNATURE.size,
            stopAfterMetadataRead = stopAfterMetadataRead
        )
    }

    private fun readChunksInternal(
        byteReader: ByteReader,
        bytesToRead: Int,
        stopAfterMetadataRead: Boolean
    ): List<WebPChunk> {

        val chunks = mutableListOf<WebPChunk>()

        var bytesReadCount = 0

        while (bytesReadCount < bytesToRead) {

            val chunkType = WebPChunkType.of(
                byteReader.readBytes("chunk type", TPYE_LENGTH)
            )

            val chunkSize = byteReader.read4BytesAsInt("chunk size", WEBP_BYTE_ORDER)

            if (chunkSize < 0)
                throw ImageReadException("Invalid WebP chunk length: $chunkSize")

            val bytes: ByteArray = byteReader.readBytes(
                "chunk data",
                chunkSize
            )

            /*
             * If chunk size is odd, a single padding byte (which MUST be 0
             * to conform with RIFF) is added.
             */
            val hasPadding = chunkSize % 2 != 0

            if (hasPadding)
                byteReader.skipBytes("padding byte", 1)

            bytesReadCount += TPYE_LENGTH + CHUNK_SIZE_LENGTH + chunkSize + if (hasPadding) 1 else 0

            val chunk = when (chunkType) {
                WebPChunkType.VP8 -> WebPChunkVP8(bytes)
                WebPChunkType.VP8L -> WebPChunkVP8L(bytes)
                WebPChunkType.VP8X -> WebPChunkVP8X(bytes)
                WebPChunkType.EXIF -> WebPChunkExif(bytes)
                WebPChunkType.XMP -> WebPChunkXmp(bytes)
                else -> WebPChunk(chunkType, bytes)
            }

            chunks.add(chunk)

            /*
             * After reading the header we can decide if we need to
             * read the rest of the file for metadata.
             */
            if (stopAfterMetadataRead) {

                /*
                 * Older chunk header types do not support Exif & XMP.
                 * So we can stop right here for those old formats.
                 */
                if (chunkType == WebPChunkType.VP8 && chunkType == WebPChunkType.VP8L)
                    break

                /*
                 * If the header reveals that there will be no EXIF and no XMP
                 * we don't need to read the whole file.
                 */
                if (chunk is WebPChunkVP8X && !chunk.hasExif && !chunk.hasXmp)
                    break
            }
        }

        return chunks
    }
}
