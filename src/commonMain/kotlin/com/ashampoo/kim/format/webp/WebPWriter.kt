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

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.webp.WebPConstants.WEBP_BYTE_ORDER
import com.ashampoo.kim.format.webp.chunk.ImageSizeAware
import com.ashampoo.kim.format.webp.chunk.WebPChunk
import com.ashampoo.kim.format.webp.chunk.WebPChunkVP8X
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter

object WebPWriter {

    fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ) = writeImage(
        chunks = WebPImageParser.readChunks(byteReader, false),
        byteWriter = byteWriter,
        exifBytes = exifBytes,
        xmp = xmp
    )

    fun writeImage(
        chunks: List<WebPChunk>,
        byteWriter: ByteWriter,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        if (chunks.isEmpty())
            throw ImageWriteException("No chunks to write!")

        val modifiedChunks = chunks.toMutableList()

        /*
         * Delete old chunks that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.EXIF }

        if (xmp != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.XMP }

        val headerChunk = modifiedChunks.first()

        /**
         * To write Exif & XMP we require the WebP file to have
         * a VP8X header with the correct marks set.
         *
         * If it already has one, we correct the header.
         * If it's missing the header we add it.
         */
        if (headerChunk is WebPChunkVP8X) {

            val replacementChunk = WebPChunkVP8X(
                bytes = WebPChunkVP8X.createBytes(
                    hasIcc = headerChunk.hasIcc,
                    hasAlpha = headerChunk.hasAlpha,
                    hasExif = if (exifBytes != null) true else headerChunk.hasExif,
                    hasXmp = if (xmp != null) true else headerChunk.hasXmp,
                    hasAnimation = headerChunk.hasAnimation,
                    imageSize = headerChunk.imageSize
                )
            )

            modifiedChunks.set(
                index = 0,
                element = replacementChunk
            )

        } else {

            /* Must be VP8 or VP8L */
            if (headerChunk !is ImageSizeAware)
                throw ImageWriteException("Illegal header chunk: $headerChunk")

            modifiedChunks.add(
                index = 0,
                element = WebPChunkVP8X(
                    bytes = WebPChunkVP8X.createBytes(
                        hasIcc = false,
                        hasAlpha = false,
                        hasExif = exifBytes != null,
                        hasXmp = xmp != null,
                        hasAnimation = false,
                        imageSize = headerChunk.imageSize
                    )
                )
            )
        }

        val contentByteWriter = ByteArrayByteWriter()

        contentByteWriter.write(WebPConstants.WEBP_SIGNATURE)

        /*
         * First write all other chunks in the original order.
         */
        for (chunk in modifiedChunks) {

            contentByteWriter.write(chunk.type.bytes)

            contentByteWriter.writeInt(
                chunk.bytes.size,
                WEBP_BYTE_ORDER
            )

            contentByteWriter.write(chunk.bytes)

            /*
             * If chunk size is odd, a single padding byte (which MUST be 0
             * to conform with RIFF) is added.
             */
            if (chunk.bytes.size % 2 != 0)
                contentByteWriter.write(0)
        }

        /*
         * A major design flaw of WebP is that is specifies the metadata chunks to come last.
         *
         * This is what the documentation says:
         * "All chunks SHOULD be placed in the same order as listed above. If a chunk appears in
         * the wrong place, the file is invalid, but readers MAY parse the file, ignoring the
         * chunks that are out of order."
         *
         * See https://developers.google.com/speed/webp/docs/riff_container#extended_file_format
         */

        if (exifBytes != null) {

            contentByteWriter.write(WebPChunkType.EXIF.bytes)

            contentByteWriter.writeInt(
                exifBytes.size,
                WEBP_BYTE_ORDER
            )

            contentByteWriter.write(exifBytes)

            /*
             * If chunk size is odd, a single padding byte (which MUST be 0
             * to conform with RIFF) is added.
             */
            if (exifBytes.size % 2 != 0)
                contentByteWriter.write(0)
        }

        if (xmp != null) {

            val xmpBytes = xmp.encodeToByteArray()

            contentByteWriter.write(WebPChunkType.XMP.bytes)

            contentByteWriter.writeInt(
                xmpBytes.size,
                WEBP_BYTE_ORDER
            )

            contentByteWriter.write(xmpBytes)

            /*
             * If chunk size is odd, a single padding byte (which MUST be 0
             * to conform with RIFF) is added.
             */
            if (xmpBytes.size % 2 != 0)
                contentByteWriter.write(0)
        }

        val contentBytes = contentByteWriter.toByteArray()

        byteWriter.write(WebPConstants.RIFF_SIGNATURE)

        byteWriter.writeInt(contentBytes.size, WEBP_BYTE_ORDER)

        byteWriter.write(contentBytes)

        byteWriter.close()
    }
}
