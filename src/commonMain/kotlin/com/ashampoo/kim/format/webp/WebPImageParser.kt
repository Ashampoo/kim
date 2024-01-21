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
import com.ashampoo.kim.format.webp.chunk.WebPChunk
import com.ashampoo.kim.format.webp.chunk.WebPChunkExif
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

            byteReader.readAndVerifyBytes("RIFF signature", RIFF_SIGNATURE)

            val length = byteReader.read4BytesAsInt("length", WEBP_BYTE_ORDER)

            byteReader.readAndVerifyBytes("WEBP signature", WEBP_SIGNATURE)

            val chunks = readChunksInternal(byteReader, length - WEBP_SIGNATURE.size)

            val exifChunk = chunks.find { it.chunkType == WebPChunkType.EXIF } as? WebPChunkExif
            val xmpChunk = chunks.find { it.chunkType == WebPChunkType.XMP } as? WebPChunkXmp

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.WEBP,
                imageSize = null,
                exif = exifChunk?.tiffContents,
                exifBytes = exifChunk?.bytes,
                iptc = null, // not supported by WebP
                xmp = xmpChunk?.xmp
            )
        }

    private fun readChunksInternal(
        byteReader: ByteReader,
        bytesToRead: Int
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
                WebPChunkType.VP8X -> WebPChunkVP8X(bytes)
                WebPChunkType.EXIF -> WebPChunkExif(bytes)
                WebPChunkType.XMP -> WebPChunkXmp(bytes)
                else -> WebPChunk(chunkType, bytes)
            }

            chunks.add(chunk)
        }

        return chunks
    }
}
