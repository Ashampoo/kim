/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.common.compress
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.png.PngCrc.continuePartialCrc
import com.ashampoo.kim.format.png.PngCrc.finishPartialCrc
import com.ashampoo.kim.format.png.PngCrc.startPartialCrc
import com.ashampoo.kim.format.png.chunks.PngTextChunk
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import io.ktor.utils.io.core.toByteArray

object PngWriter {

    private fun writeChunk(
        byteWriter: ByteWriter,
        chunkType: ChunkType,
        data: ByteArray?
    ) {

        val dataLength = data?.size ?: 0

        byteWriter.writeInt(dataLength)
        byteWriter.write(chunkType.array)

        if (data != null)
            byteWriter.write(data)

        val crc1 = startPartialCrc(chunkType.array)

        val crc2 = if (data == null)
            crc1
        else
            continuePartialCrc(crc1, data)

        val crc = finishPartialCrc(crc2).toInt()

        byteWriter.writeInt(crc)
    }

    /**
     * XMP is often uncompressed (see GIMP for example).
     * For better compatibility we also write it without compression.
     * The chunk type iTXT is the standard for this, because XMP is UTF-8.
     */
    private fun writeXmpChunk(byteWriter: ByteWriter, xmpXml: String) {

        /*
         * Keyword:             1-79 bytes (character string)
         * Null separator:      1 byte
         * Compression flag:    1 byte
         * Compression method:  1 byte
         * Language tag:        0 or more bytes (character string)
         * Null separator:      1 byte
         * Translated keyword:  0 or more bytes
         * Null separator:      1 byte
         * Text:                0 or more bytes
         */

        val writer = ByteArrayByteWriter()

        /* XMP keyword */
        writer.write(PngConstants.XMP_KEYWORD.encodeToByteArray())
        writer.write(0)

        /* No compression and no language tag */
        writer.write(0) // No compression
        writer.write(0) // No compression method
        writer.write(0) // No language tag

        /* XMP keyword - null-terminated */
        writer.write(PngConstants.XMP_KEYWORD.toByteArray())
        writer.write(0)

        /* XMP bytes */
        writer.write(xmpXml.toByteArray())

        writeChunk(byteWriter, ChunkType.ITXT, writer.toByteArray())
    }

    /*
     * A note on IPTC support:
     * We tried to write it like ExifTool, but the result can't be read anywhere.
     * We don't have a specification here and so we don't support it.
     *
     * **Note:** Don't use this. It's left here as we may pick it up in the future
     * once we at least know the specification GIMP & ExifTool follow.
     */
    @Suppress("UnusedPrivateMember", "kotlin:S1144")
    private fun writeIptcChunk(byteWriter: ByteWriter, iptcBytes: ByteArray) {

        /*
         * Keyword:            1-79 bytes (character string)
         * Null separator:     1 byte
         * Compression method: 1 byte
         * Compressed text:    n bytes
         */

        val writer = ByteArrayByteWriter()

        /* IPTC keyword */
        writer.write(PngConstants.IPTC_KEYWORD.encodeToByteArray())
        writer.write(0)

        /* Only DEFLATE compression (value 0) is defined in the spec. */
        writer.write(PngConstants.COMPRESSION_DEFLATE_INFLATE)

        val textToWrite = "IPTC profile ${iptcBytes.size} ${iptcBytes.toHex()}"

        writer.write(compress(textToWrite))

        writeChunk(byteWriter, ChunkType.ZTXT, writer.toByteArray())
    }

    fun writeImage(
        byteWriter: ByteWriter,
        originalBytes: ByteArray,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        val byteReader = ByteArrayByteReader(originalBytes)

        val chunks = PngImageParser.readChunks(byteReader, null).toMutableList()

        /*
         * Delete old chunks that are going to be replaced.
         */

        if (exifBytes != null)
            chunks.removeAll {
                it.chunkType == ChunkType.EXIF ||
                    it is PngTextChunk && it.getKeyword() == PngConstants.EXIF_KEYWORD
            }

        if (xmp != null)
            chunks.removeAll { it is PngTextChunk && it.getKeyword() == PngConstants.XMP_KEYWORD }

        /*
         * Write the new file
         */

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        for (chunk in chunks) {

            writeChunk(byteWriter, chunk.chunkType, chunk.bytes)

            /* Write EXIF chunk right after the header. */
            if (exifBytes != null && ChunkType.IHDR == chunk.chunkType)
                writeChunk(byteWriter, ChunkType.EXIF, exifBytes)

            /* Write XMP chunk right after the header. */
            if (xmp != null && ChunkType.IHDR == chunk.chunkType)
                writeXmpChunk(byteWriter, xmp)
        }

        byteWriter.close()
    }
}
