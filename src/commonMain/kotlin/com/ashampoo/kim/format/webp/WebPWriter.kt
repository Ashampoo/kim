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

import com.ashampoo.kim.format.webp.chunk.ImageSizeAware
import com.ashampoo.kim.format.webp.chunk.WebPChunk
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

        val modifiedChunks = chunks.toMutableList()

        /*
         * Delete old chunks that are going to be replaced.
         */

        if (exifBytes != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.EXIF }

        if (xmp != null)
            modifiedChunks.removeAll { it.type == WebPChunkType.XMP }

        val contentByteWriter = ByteArrayByteWriter()

        contentByteWriter.write(WebPConstants.WEBP_SIGNATURE)

        for (chunk in modifiedChunks) {

            contentByteWriter.write(chunk.type.bytes)
            contentByteWriter.writeInt(WebPConstants.CHUNK_HEADER_LENGTH + chunk.bytes.size)
            contentByteWriter.write(chunk.bytes)

            val shouldInsertMetadata = false &&
                chunk is ImageSizeAware

            if (shouldInsertMetadata) {

                if (exifBytes != null) {

                    contentByteWriter.write(WebPChunkType.EXIF.bytes)
                    contentByteWriter.writeInt(WebPConstants.CHUNK_HEADER_LENGTH + exifBytes.size)
                    contentByteWriter.write(exifBytes)
                }

                if (xmp != null) {

                    val xmpBytes = xmp.encodeToByteArray()

                    contentByteWriter.write(WebPChunkType.XMP.bytes)
                    contentByteWriter.writeInt(WebPConstants.CHUNK_HEADER_LENGTH + xmpBytes.size)
                    contentByteWriter.write(xmpBytes)
                }
            }
        }

        val contentBytes = contentByteWriter.toByteArray()

        byteWriter.write(WebPConstants.RIFF_SIGNATURE)
        byteWriter.writeInt(contentBytes.size)
        byteWriter.write(contentBytes)

        byteWriter.close()
    }
}
