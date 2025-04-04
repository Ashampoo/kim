package com.ashampoo.kim.format.gif

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.MetadataExtractor
import com.ashampoo.kim.format.gif.GifImageParser.parseGifSubChunksUntilEmpty
import com.ashampoo.kim.format.gif.chunk.GifChunkImageDescriptor
import com.ashampoo.kim.format.gif.chunk.GifChunkLogicalScreenDescriptor
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.model.ImageSize

public object GifMetadataExtractor : MetadataExtractor {

    private val fakeImageDataLength: Byte = 0x01.toByte()
    private val fakeImageData: Byte = 0x00.toByte()

    @Throws(ImageReadException::class)
    override fun extractMetadataBytes(
        byteReader: ByteReader
    ): ByteArray = tryWithImageReadException {

        val bytes = mutableListOf<Byte>()

        // Read signature
        val signatureBites = byteReader.readBytes(3)
        require(signatureBites.contentEquals(GifConstants.GIF_SIGNATURE)) {
            "GIF signature mismatch: ${signatureBites.size}, expected ${GifConstants.GIF_SIGNATURE}."
        }
        bytes.addAll(signatureBites.toList())

        // Read version
        val versionBytes = byteReader.readBytes(3)
        require(
            GifVersion.GIF87A.matches(versionBytes) ||
                GifVersion.GIF89A.matches(versionBytes)
        ) {
            "GIF version mismatch: ${versionBytes.toSingleNumberHexes()}"
        }
        bytes.addAll(versionBytes.toList())

        // Modify logical screen descriptor chunk to be a 1x1 image
        val logicalScreenDescriptorBytes = byteReader.readBytes(7)
        val logicalScreenDescriptorChunk = GifChunkLogicalScreenDescriptor(logicalScreenDescriptorBytes)
        val modifiedLogicalScreenDescriptorChunk = GifChunkLogicalScreenDescriptor.constructFromProperties(
            ImageSize(1, 1),
            logicalScreenDescriptorChunk.globalColorTableFlag,
            logicalScreenDescriptorChunk.colorResolution,
            logicalScreenDescriptorChunk.sortFlag,
            logicalScreenDescriptorChunk.globalColorTableSize,
            logicalScreenDescriptorChunk.backgroundColorIndex,
            logicalScreenDescriptorChunk.pixelAspectRatio
        )
        bytes.addAll(modifiedLogicalScreenDescriptorChunk.bytes.toList())

        // Read global color table chunk if present
        if (logicalScreenDescriptorChunk.globalColorTableFlag) {
            val globalColorTableSize = 3 * (1 shl (logicalScreenDescriptorChunk.globalColorTableSize + 1))
            val globalColorTableBytes = byteReader.readBytes(globalColorTableSize)
            bytes.addAll(globalColorTableBytes.toList())
        }

        // Read remaining chunks
        while (true) {
            when (byteReader.readByte("introducer")) {
                GifConstants.IMAGE_SEPARATOR -> modifyImageChunks(byteReader, bytes)
                GifConstants.EXTENSION_INTRODUCER -> parseAndCopyExtensionChunk(byteReader, bytes)
                GifConstants.GIF_TERMINATOR -> {
                    bytes.add(GifConstants.GIF_TERMINATOR)
                    break
                }
            }
        }

        return@tryWithImageReadException bytes.toByteArray()
    }

    private fun modifyImageChunks(byteReader: ByteReader, outputBytes: MutableList<Byte>) {
        // Modify image descriptor to be a 1x1 image
        val imageDescriptorBytes = byteReader.readBytes("image descriptor", 9)
        val imageDescriptorChunk = GifChunkImageDescriptor(
            byteArrayOf(GifConstants.IMAGE_SEPARATOR) + imageDescriptorBytes
        )
        val modifiedImageDescriptorChunk = GifChunkImageDescriptor.constructFromProperties(
            0,
            0,
            ImageSize(1, 1),
            imageDescriptorChunk.localColorTableFlag,
            imageDescriptorChunk.interlaceFlag,
            imageDescriptorChunk.sortFlag,
            imageDescriptorChunk.localColorTableSize
        )
        outputBytes.addAll(modifiedImageDescriptorChunk.bytes.toList())

        // Read local color table if present
        if (imageDescriptorChunk.localColorTableFlag) {
            val localColorTableSize = 3 * (1 shl (imageDescriptorChunk.localColorTableSize + 1))
            val localColorTableBytes = byteReader.readBytes("local color table", localColorTableSize)
            outputBytes.addAll(localColorTableBytes.toList())
        }

        // Replace image data with a single pixel of black
        val lzwMinimumCodeSize = byteReader.readByte("LZW minimum code size")
        byteReader.parseGifSubChunksUntilEmpty("image data")
        outputBytes.add(lzwMinimumCodeSize)
        outputBytes.add(fakeImageDataLength)
        outputBytes.add(fakeImageData)
        outputBytes.add(0x00)
    }

    private fun parseAndCopyExtensionChunk(byteReader: ByteReader, outputBytes: MutableList<Byte>) {
        when (val extensionLabelByte = byteReader.readByte("extension label")) {
            GifConstants.GRAPHICS_CONTROL_EXTENSION_LABEL -> {
                outputBytes.addAll(listOf(GifConstants.EXTENSION_INTRODUCER, extensionLabelByte))
                val graphicsControlExtensionBytes = byteReader.readBytes("graphics control extension", 6)
                outputBytes.addAll(graphicsControlExtensionBytes.toList())
            }

            GifConstants.APPLICATION_EXTENSION_LABEL,
            GifConstants.COMMENT_EXTENSION_LABEL,
            GifConstants.PLAIN_TEXT_EXTENSION_LABEL -> {
                outputBytes.addAll(listOf(GifConstants.EXTENSION_INTRODUCER, extensionLabelByte))
                val subChunks = byteReader.parseGifSubChunksUntilEmpty("plain text extension")
                subChunks.forEach { outputBytes.addAll(it.toList()) }
                outputBytes.add(0x00)
            }
        }
    }
}
