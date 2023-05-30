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

import com.ashampoo.kim.format.png.PngCrc.continuePartialCrc
import com.ashampoo.kim.format.png.PngCrc.finishPartialCrc
import com.ashampoo.kim.format.png.PngCrc.startPartialCrc
import com.ashampoo.kim.format.png.chunks.PngChunkItxt
import com.ashampoo.kim.format.png.chunks.PngChunkZtxt
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

class PngWriter {

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
     * XMP is often uncompressed (see GIMP for example)
     * For better compatibility we also write it without compression.
     */
    private fun writeChunkXmpiTXt(byteWriter: ByteWriter, xmpXml: String) {

        val writer = ByteArrayByteWriter()

        /* XMP keyword */
        writer.write(PngConstants.XMP_KEYWORD.toByteArray(Charsets.ISO_8859_1))
        writer.write(0)

        /* No compression */
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

    fun writeImage(
        byteWriter: ByteWriter,
        originalBytes: ByteArray,
        exifBytes: ByteArray?,
        xmp: String?
    ) {

        val byteReader = ByteArrayByteReader(originalBytes)

        val chunks =
            PngImageParser
                .readChunks(byteReader, null)
                .toMutableList()

        byteWriter.write(PngConstants.PNG_SIGNATURE)

        if (xmp != null) {

            /*
             * Identify and remove old chunk
             */
            val chunkIt = chunks.iterator()

            while (chunkIt.hasNext()) {

                val chunk = chunkIt.next()

                if (ChunkType.ITXT == chunk.chunkType) {

                    val itxt = chunk as PngChunkItxt

                    if (PngConstants.XMP_KEYWORD == itxt.keyword)
                        chunkIt.remove()
                }
            }
        }

        if (exifBytes != null) {

            /*
             * Identify and remove old chunks
             */
            val chunkIt = chunks.iterator()

            while (chunkIt.hasNext()) {

                val chunk = chunkIt.next()

                if (ChunkType.EXIF == chunk.chunkType)
                    chunkIt.remove()

                if (ChunkType.ZTXT == chunk.chunkType) {

                    val ztxt = chunk as PngChunkZtxt

                    if ("Raw profile type exif" == ztxt.keyword || "Raw profile type iptc" == ztxt.keyword)
                        chunkIt.remove()
                }
            }
        }

        for (chunk in chunks) {

            writeChunk(byteWriter, chunk.chunkType, chunk.bytes)

            /* Write EXIF chunk right after the header. */
            if (exifBytes != null && ChunkType.IHDR == chunk.chunkType)
                writeChunk(byteWriter, ChunkType.EXIF, exifBytes)

            /* Write XMP chunk right after the header. */
            if (xmp != null && ChunkType.IHDR == chunk.chunkType)
                writeChunkXmpiTXt(byteWriter, xmp)
        }

        byteWriter.close()
    }
}
