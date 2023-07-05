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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.png.chunks.PngChunk
import com.ashampoo.kim.format.png.chunks.PngChunkIhdr
import com.ashampoo.kim.format.png.chunks.PngChunkItxt
import com.ashampoo.kim.format.png.chunks.PngChunkText
import com.ashampoo.kim.format.png.chunks.PngChunkZtxt
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object PngImageParser : ImageParser() {

    init {
        byteOrder = ByteOrder.BIG_ENDIAN
    }

    private fun readChunksInternal(
        byteReader: ByteReader,
        chunkTypes: List<ChunkType>?
    ): List<PngChunk> {

        val chunks = mutableListOf<PngChunk>()

        while (true) {

            val length = byteReader.read4BytesAsInt("length", byteOrder)

            if (length < 0)
                throw ImageReadException("Invalid PNG chunk length: $length")

            val chunkType = ChunkType.of(byteReader.readBytes(4))

            val keep = chunkTypes?.contains(chunkType) ?: true

            var bytes: ByteArray? = null

            if (keep)
                bytes = byteReader.readBytes("chunk data", length)
            else
                byteReader.skipBytes("chunk data", length.toLong())

            val crc = byteReader.read4BytesAsInt("crc", byteOrder)

            if (keep) {

                requireNotNull(bytes)

                when {
                    ChunkType.TEXT == chunkType -> chunks.add(PngChunkText(length, chunkType, crc, bytes))
                    ChunkType.ZTXT == chunkType -> chunks.add(PngChunkZtxt(length, chunkType, crc, bytes))
                    ChunkType.IHDR == chunkType -> chunks.add(PngChunkIhdr(length, chunkType, crc, bytes))
                    ChunkType.ITXT == chunkType -> chunks.add(PngChunkItxt(length, chunkType, crc, bytes))
                    else -> chunks.add(PngChunk(length, chunkType, crc, bytes))
                }
            }

            if (ChunkType.IEND == chunkType)
                break
        }

        return chunks
    }

    fun readSignature(byteReader: ByteReader) =
        byteReader.readAndVerifyBytes("png signature", PngConstants.PNG_SIGNATURE)

    fun readChunks(
        byteReader: ByteReader,
        chunkTypes: List<ChunkType>?
    ): List<PngChunk> {

        readSignature(byteReader)

        return readChunksInternal(byteReader, chunkTypes)
    }

    private fun getImageSize(chunks: List<PngChunk>): ImageSize {

        val headerChunks = chunks.filterIsInstance<PngChunkIhdr>()

        if (headerChunks.size > 1)
            throw ImageReadException("PNG contains more than one Header")

        val pngChunkIHDR = headerChunks.first()

        return ImageSize(pngChunkIHDR.width, pngChunkIHDR.height)
    }

    override fun parseMetadata(byteReader: ByteReader): ImageMetadata {

        val chunks = readChunks(
            byteReader,
            listOf(ChunkType.IHDR, ChunkType.TEXT, ChunkType.ZTXT, ChunkType.ITXT, ChunkType.EXIF)
        )

        val imageSize = getImageSize(chunks)

        val exif = getExif(chunks)

        val xmp = getXmpXml(chunks)

        return ImageMetadata(ImageFormat.PNG, imageSize, exif, null, xmp)
    }

    private fun getExif(chunks: List<PngChunk>): TiffContents? {

        val exifChunk = chunks.find { it.chunkType == ChunkType.EXIF } ?: return null

        return TiffReader().read(ByteArrayByteReader(exifChunk.bytes))
    }

    private fun getXmpXml(chunks: List<PngChunk>): String? = chunks
        .filterIsInstance<PngChunkItxt>()
        .filter { it.getKeyword() == PngConstants.XMP_KEYWORD }
        .firstOrNull()
        ?.getText()

}
