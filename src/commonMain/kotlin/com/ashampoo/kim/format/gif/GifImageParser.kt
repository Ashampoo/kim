/*
 * Copyright 2025 Ramon Bouckaert
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

package com.ashampoo.kim.format.gif

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.gif.chunk.GifChunk
import com.ashampoo.kim.format.gif.chunk.GifChunkApplicationExtension
import com.ashampoo.kim.format.gif.chunk.GifChunkCommentExtension
import com.ashampoo.kim.format.gif.chunk.GifChunkHeader
import com.ashampoo.kim.format.gif.chunk.GifChunkImageData
import com.ashampoo.kim.format.gif.chunk.GifChunkImageDescriptor
import com.ashampoo.kim.format.gif.chunk.GifChunkLogicalScreenDescriptor
import com.ashampoo.kim.format.gif.chunk.GifChunkPlainTextExtension
import com.ashampoo.kim.format.gif.chunk.GifChunkTerminator
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.model.ImageFormat
import kotlin.jvm.JvmStatic

public object GifImageParser : ImageParser {

    private val metadataChunkTypes = listOf(
        GifChunkType.HEADER,
        GifChunkType.IMAGE_DESCRIPTOR,
        GifChunkType.APPLICATION_EXTENSION
    )

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader): ImageMetadata =
        tryWithImageReadException {

            val chunks = readChunks(byteReader, metadataChunkTypes)

            if (chunks.isEmpty())
                throw ImageReadException("Did not find any chunks in file.")

            return@tryWithImageReadException parseMetadataFromChunks(chunks)
        }

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun parseMetadataFromChunks(chunks: List<GifChunk>): ImageMetadata = tryWithImageReadException {

        require(chunks.isNotEmpty()) {
            "Given chunk list was empty."
        }

        val headerChunk = chunks.filterIsInstance<GifChunkHeader>().firstOrNull()

        checkNotNull(headerChunk) {
            "Did not find mandatory header chunk. " +
                "Found chunk types: ${chunks.map { it.type }}"
        }

        val version = headerChunk.version

        val firstImageDescriptorChunk = chunks.filterIsInstance<GifChunkImageDescriptor>().firstOrNull()

        checkNotNull(firstImageDescriptorChunk) {
            "Did not find mandatory image descriptor chunk. " +
                "Found chunk types: ${chunks.map { it.type }}"
        }

        val imageSize = firstImageDescriptorChunk.imageSize

        /* Only GIF89A supports XMP metadata */
        val xmp = if (version == GifVersion.GIF89A)
            getXmpXml(chunks)
        else
            null

        return@tryWithImageReadException ImageMetadata(
            imageFormat = ImageFormat.GIF,
            imageSize = imageSize,
            exif = null,
            exifBytes = null, // GIF does not support EXIF data
            iptc = null, // GIF does not support IPTC data
            xmp = xmp
        )
    }

    private fun getXmpXml(chunks: List<GifChunk>): String? = chunks
        .filterIsInstance<GifChunkApplicationExtension>()
        .firstOrNull { it.applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER }
        ?.parseAsXmpOrThrow()

    @JvmStatic
    public fun readChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<GifChunkType>?
    ): List<GifChunk> {

        val chunks = mutableListOf<GifChunk>()

        /* Read header chunk */
        val headerBytes = byteReader.readBytes(6)

        if (chunkTypeFilter?.contains(GifChunkType.HEADER) != false)
            chunks.add(GifChunkHeader(headerBytes))

        /* Read logical screen descriptor chunk */
        val logicalScreenDescriptorBytes = byteReader.readBytes(7)
        val logicalScreenDescriptorChunk = GifChunkLogicalScreenDescriptor(logicalScreenDescriptorBytes)

        if (chunkTypeFilter?.contains(GifChunkType.LOGICAL_SCREEN_DESCRIPTOR) != false)
            chunks.add(logicalScreenDescriptorChunk)

        /* Read global color table chunk if present */
        if (logicalScreenDescriptorChunk.globalColorTableFlag) {

            val globalColorTableSize = 3 * (1 shl (logicalScreenDescriptorChunk.globalColorTableSize + 1))

            val globalColorTableBytes = byteReader.readBytes(globalColorTableSize)

            if (chunkTypeFilter?.contains(GifChunkType.GLOBAL_COLOR_TABLE) != false)
                chunks.add(GifChunk(GifChunkType.GLOBAL_COLOR_TABLE, globalColorTableBytes))
        }

        /* Read remaining chunks */
        while (true) {

            when (byteReader.readByte("introducer")) {

                GifConstants.IMAGE_SEPARATOR -> chunks.addAll(readImageChunks(byteReader, chunkTypeFilter))

                GifConstants.EXTENSION_INTRODUCER -> readExtensionChunk(byteReader, chunkTypeFilter)?.also(chunks::add)

                GifConstants.GIF_TERMINATOR -> {

                    if (chunkTypeFilter?.contains(GifChunkType.TERMINATOR) != false)
                        chunks.add(GifChunkTerminator(byteArrayOf(GifConstants.GIF_TERMINATOR)))

                    break
                }
            }
        }

        return chunks
    }

    private fun readImageChunks(
        byteReader: ByteReader,
        chunkTypeFilter: List<GifChunkType>?
    ): List<GifChunk> {

        val chunks = mutableListOf<GifChunk>()

        /* Read image descriptor */
        val imageDescriptorBytes = byteReader.readBytes("image descriptor", 9)

        val imageDescriptorChunk = GifChunkImageDescriptor(
            byteArrayOf(GifConstants.IMAGE_SEPARATOR) + imageDescriptorBytes
        )

        if (chunkTypeFilter?.contains(GifChunkType.IMAGE_DESCRIPTOR) != false)
            chunks.add(imageDescriptorChunk)

        /* Read local color table if present */
        if (imageDescriptorChunk.localColorTableFlag) {

            val localColorTableSize = 3 * (1 shl (imageDescriptorChunk.localColorTableSize + 1))
            val localColorTableBytes = byteReader.readBytes("local color table", localColorTableSize)

            if (chunkTypeFilter?.contains(GifChunkType.LOCAL_COLOR_TABLE) != false)
                chunks.add(GifChunk(GifChunkType.LOCAL_COLOR_TABLE, localColorTableBytes))
        }

        /* Read image data */
        val lzwMinimumCodeSize = byteReader.readByte("LZW minimum code size")
        val subChunks = byteReader.parseGifSubChunksUntilEmpty("image data")

        if (chunkTypeFilter?.contains(GifChunkType.IMAGE_DATA) != false)
            chunks.add(GifChunkImageData(lzwMinimumCodeSize, subChunks))

        return chunks
    }

    private fun readExtensionChunk(
        byteReader: ByteReader,
        chunkTypeFilter: List<GifChunkType>?
    ): GifChunk? =
        when (byteReader.readByte("extension label")) {

            GifConstants.GRAPHICS_CONTROL_EXTENSION_LABEL -> {

                val graphicsControlExtensionBytes = byteReader.readBytes("graphics control extension", 6)

                val graphicsControlExtensionChunk = GifChunk(
                    GifChunkType.GRAPHICS_CONTROL_EXTENSION,
                    byteArrayOf(
                        GifConstants.EXTENSION_INTRODUCER,
                        GifConstants.GRAPHICS_CONTROL_EXTENSION_LABEL
                    ) + graphicsControlExtensionBytes
                )

                if (chunkTypeFilter?.contains(GifChunkType.GRAPHICS_CONTROL_EXTENSION) != false)
                    graphicsControlExtensionChunk
                else
                    null
            }

            GifConstants.APPLICATION_EXTENSION_LABEL -> {

                val subChunks = byteReader.parseGifSubChunksUntilEmpty("application extension")

                if (chunkTypeFilter?.contains(GifChunkType.APPLICATION_EXTENSION) != false)
                    GifChunkApplicationExtension(
                        byteArrayOf(
                            GifConstants.EXTENSION_INTRODUCER,
                            GifConstants.APPLICATION_EXTENSION_LABEL
                        ),
                        subChunks
                    )
                else
                    null
            }

            GifConstants.COMMENT_EXTENSION_LABEL -> {

                val subChunks = byteReader.parseGifSubChunksUntilEmpty("comment extension")

                if (chunkTypeFilter?.contains(GifChunkType.COMMENT_EXTENSION) != false)
                    GifChunkCommentExtension(
                        byteArrayOf(GifConstants.EXTENSION_INTRODUCER, GifConstants.COMMENT_EXTENSION_LABEL),
                        subChunks
                    )
                else
                    null
            }

            GifConstants.PLAIN_TEXT_EXTENSION_LABEL -> {

                val subChunks = byteReader.parseGifSubChunksUntilEmpty("plain text extension")

                if (chunkTypeFilter?.contains(GifChunkType.PLAIN_TEXT_EXTENSION) != false)
                    GifChunkPlainTextExtension(
                        byteArrayOf(GifConstants.EXTENSION_INTRODUCER, GifConstants.PLAIN_TEXT_EXTENSION_LABEL),
                        subChunks
                    )
                else
                    null
            }

            else -> null
        }

    internal fun ByteReader.parseGifSubChunksUntilEmpty(
        fieldName: String
    ): List<ByteArray> {

        val subChunks = mutableListOf<ByteArray>()

        while (true) {

            val subChunkSize = this.readByteAsInt()

            /* Break at the end of sub chunks */
            if (subChunkSize == 0)
                break

            val subChunkBytes = this.readBytes("$fieldName sub chunk", subChunkSize)

            subChunks.add(byteArrayOf(subChunkSize.toByte()) + subChunkBytes)
        }

        return subChunks
    }
}
