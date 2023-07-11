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
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.format.png.chunks.PngChunk
import com.ashampoo.kim.format.png.chunks.PngChunkIhdr
import com.ashampoo.kim.format.png.chunks.PngChunkItxt
import com.ashampoo.kim.format.png.chunks.PngChunkText
import com.ashampoo.kim.format.png.chunks.PngChunkZtxt
import com.ashampoo.kim.format.png.chunks.PngTextChunk
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object PngImageParser : ImageParser() {

    private val controlCharRegex = Regex("[\\p{Cntrl}]")

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

        /*
         * We attempt to read EXIF data from the EXIF chunk, which has been the standard
         * location since 2017. If the EXIF chunk is not present, we fallback to reading
         * it from TXT. Some older apps may still store the data there.
         */
        val exif = getExif(chunks) ?: getExifFromTextChunk(chunks)

        val xmp = getXmpXml(chunks)

        return ImageMetadata(ImageFormat.PNG, imageSize, exif, null, xmp)
    }

    private fun getExif(chunks: List<PngChunk>): TiffContents? {

        val exifChunk = chunks.find { it.chunkType == ChunkType.EXIF } ?: return null

        return TiffReader().read(ByteArrayByteReader(exifChunk.bytes))
    }

    /*
     * According to https://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_PNG_files
     * Exiv2 saves EXIF & IPTC in zTXT chunks. This library is widely used and therefore
     * we can expect a lot of files storing the information in that way.
     * According to https://exiftool.org/TagNames/PNG.html it may even be in uncompressed text.
     * So we look for all PNG text chunk types and take the first one that matches the keyword.
     */
    private fun getExifFromTextChunk(chunks: List<PngChunk>): TiffContents? {

        val chunkText = getTextChunkWithKeyword(chunks, PngConstants.EXIF_KEYWORD) ?: return null

        /*
         * Before the EXIF block starts there are some characters before that.
         * How these look seems to depend on the tool writing it. There may be no standard.
         */
        val index = chunkText.indexOf(JpegConstants.EXIF_IDENTIFIER_CODE_HEX)

        /* If we did not find the identifier we may have invalid data. */
        if (index == -1)
            return null

        /*
         * This should be a text starting with EXIF identifier code "45786966"
         * and ending with the regular "ffd9". It's HEX encoded and contains
         * control chars. We need to remove them and convert it to a ByteArray.
         */
        val exifText = chunkText
            .substring(startIndex = index)
            .replace(controlCharRegex, "")
            .trim()

        /*
         * Ensure the block is completely read and is a multiple of two.
         * We don't want the following ByteArray-conversion to fail.
         */
        if (!exifText.endsWith("ffd9") || exifText.length % 2 != 0)
            return null

        println(exifText)

        /*
         * Convert it to bytes and drop the header.
         */
        val exifTextBytes = exifText
            .chunked(2)
            .map { it.toInt(16).toByte() }
            .drop(JpegConstants.EXIF_IDENTIFIER_CODE.size)
            .toByteArray()

        /*
         * This should be fine now to be fed into the TIFF reader.
         */
        return TiffReader().read(ByteArrayByteReader(exifTextBytes))
    }

    private fun getXmpXml(chunks: List<PngChunk>): String? = chunks
        .filterIsInstance<PngChunkItxt>()
        .filter { it.getKeyword() == PngConstants.XMP_KEYWORD }
        .firstOrNull()
        ?.getText()

    private fun getTextChunkWithKeyword(chunks: List<PngChunk>, keyword: String): String? = chunks
        .filterIsInstance<PngTextChunk>()
        .filter { it.getKeyword() == keyword }
        .firstOrNull()
        ?.getText()
}
