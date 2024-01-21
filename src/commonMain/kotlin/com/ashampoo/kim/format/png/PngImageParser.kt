/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.convertHexStringToByteArray
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcParser
import com.ashampoo.kim.format.png.PngConstants.PNG_BYTE_ORDER
import com.ashampoo.kim.format.png.chunk.PngChunk
import com.ashampoo.kim.format.png.chunk.PngChunkIhdr
import com.ashampoo.kim.format.png.chunk.PngChunkItxt
import com.ashampoo.kim.format.png.chunk.PngChunkText
import com.ashampoo.kim.format.png.chunk.PngChunkZtxt
import com.ashampoo.kim.format.png.chunk.PngTextChunk
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object PngImageParser : ImageParser {

    private val controlCharRegex = Regex("[\\p{Cntrl}]")

    private val metadataChunkTypes = listOf(
        PngChunkType.IHDR,
        PngChunkType.TEXT,
        PngChunkType.ZTXT,
        PngChunkType.ITXT,
        PngChunkType.EXIF
    )

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            val chunks = readChunks(byteReader, metadataChunkTypes)

            return@tryWithImageReadException parseMetadataFromChunks(chunks)
        }

    @Throws(ImageReadException::class)
    fun parseMetadataFromChunks(chunks: List<PngChunk>): ImageMetadata =
        tryWithImageReadException {

            val imageSize = getImageSize(chunks)

            /*
             * We attempt to read EXIF data from the EXIF chunk, which has been the standard
             * location since 2017. If the EXIF chunk is not present, we fallback to reading
             * it from TXT. Some older apps may still store the data there.
             */
            val exifPair = getExif(chunks) ?: getExifFromTextChunk(chunks)

            val iptc = getIptcFromTextChunk(chunks)

            val xmp = getXmpXml(chunks)

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.PNG,
                imageSize = imageSize,
                exif = exifPair?.second,
                exifBytes = exifPair?.first,
                iptc = iptc,
                xmp = xmp
            )
        }

    private fun getImageSize(chunks: List<PngChunk>): ImageSize {

        val headerChunks = chunks.filterIsInstance<PngChunkIhdr>()

        if (headerChunks.size > 1)
            throw ImageReadException("PNG contains more than one Header")

        val pngChunkIHDR = headerChunks.first()

        return ImageSize(pngChunkIHDR.width, pngChunkIHDR.height)
    }

    private fun getExif(chunks: List<PngChunk>): Pair<ByteArray, TiffContents>? {

        val exifChunk = chunks.find { it.chunkType == PngChunkType.EXIF } ?: return null

        return exifChunk.bytes to TiffReader.read(exifChunk.bytes)
    }

    /*
     * According to https://dev.exiv2.org/projects/exiv2/wiki/The_Metadata_in_PNG_files
     * Exiv2 saves EXIF & IPTC in zTXT chunks. This library is widely used and therefore
     * we can expect a lot of files storing the information in that way.
     * According to https://exiftool.org/TagNames/PNG.html it may even be in uncompressed text.
     * So we look for all PNG text chunk types and take the first one that matches the keyword.
     */
    private fun getExifFromTextChunk(chunks: List<PngChunk>): Pair<ByteArray, TiffContents>? {

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

        /*
         * Convert it to bytes and drop the header.
         */
        val exifBytes = convertHexStringToByteArray(exifText)

        val exifBytesWithoutIdentifier =
            exifBytes.drop(JpegConstants.EXIF_IDENTIFIER_CODE.size)
                .toByteArray()

        /*
         * This should be fine now to be fed into the TIFF reader.
         */
        return exifBytesWithoutIdentifier to
            TiffReader.read(exifBytesWithoutIdentifier)
    }

    private fun getIptcFromTextChunk(chunks: List<PngChunk>): IptcMetadata? {

        val chunkText = getTextChunkWithKeyword(chunks, PngConstants.IPTC_KEYWORD) ?: return null

        /*
         * Before the IPTC block starts there are some characters before that.
         * How these look seems to depend on the tool writing it. There may be no standard.
         */
        val index = chunkText.indexOf(JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX)

        /* If we did not find the identifier we may have invalid data. */
        if (index == -1)
            return null

        /*
         * This text is HEX encoded and contains control chars.
         * We need to remove them and convert it to a ByteArray.
         */
        val iptcText = chunkText
            .substring(startIndex = index)
            .replace(controlCharRegex, "")
            .trim()

        /*
         * Ensure the block is completely read and is a multiple of two.
         * We don't want the following ByteArray-conversion to fail.
         */
        if (iptcText.length % 2 != 0)
            return null

        /*
         * Convert it to bytes.
         */
        val iptcBytes = convertHexStringToByteArray(iptcText)

        /*
         * This should be fine now to be fed into the IPTC reader.
         * The bytes don't have the APP13 header, because it's not taken from an JPEG segment.
         */
        return IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )
    }

    private fun getXmpXml(chunks: List<PngChunk>): String? {

        val text = chunks
            .filterIsInstance<PngChunkItxt>()
            .filter { it.getKeyword() == PngConstants.XMP_KEYWORD }
            .firstOrNull()
            ?.getText()

        if (text.isNullOrBlank())
            return text

        return text
    }

    private fun getTextChunkWithKeyword(chunks: List<PngChunk>, keyword: String): String? {

        val text = chunks
            .filterIsInstance<PngTextChunk>()
            .filter { it.getKeyword() == keyword }
            .firstOrNull()
            ?.getText()

        if (text.isNullOrBlank())
            return text

        return text
    }

    private fun readAndVerifySignature(byteReader: ByteReader) =
        byteReader.readAndVerifyBytes("PNG signature", PngConstants.PNG_SIGNATURE)

    fun readChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?
    ): List<PngChunk> = tryWithImageReadException {

        readAndVerifySignature(byteReader)

        return readChunksInternal(byteReader, chunkTypeFilter)
    }

    private fun readChunksInternal(
        byteReader: ByteReader,
        chunkTypeFilter: List<PngChunkType>?
    ): List<PngChunk> {

        val chunks = mutableListOf<PngChunk>()

        while (true) {

            val length = byteReader.read4BytesAsInt("chunk length", PNG_BYTE_ORDER)

            if (length < 0)
                throw ImageReadException("Invalid PNG chunk length: $length")

            val chunkType = PngChunkType.of(
                byteReader.readBytes("chunk type", PngConstants.TPYE_LENGTH)
            )

            val keep = chunkTypeFilter?.contains(chunkType) ?: true

            var bytes: ByteArray? = null

            if (keep)
                bytes = byteReader.readBytes("chunk data", length)
            else
                byteReader.skipBytes("chunk data", length)

            val crc = byteReader.read4BytesAsInt("crc", PNG_BYTE_ORDER)

            if (keep) {

                requireNotNull(bytes)

                val chunk = when (chunkType) {
                    PngChunkType.TEXT -> PngChunkText(PngChunkType.TEXT, crc, bytes)
                    PngChunkType.ZTXT -> PngChunkZtxt(crc, bytes)
                    PngChunkType.IHDR -> PngChunkIhdr(crc, bytes)
                    PngChunkType.ITXT -> PngChunkItxt(crc, bytes)
                    else -> PngChunk(chunkType, bytes, crc)
                }

                chunks.add(chunk)
            }

            if (PngChunkType.IEND == chunkType)
                break
        }

        return chunks
    }
}
