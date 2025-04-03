package com.ashampoo.kim.format.gif

import com.ashampoo.kim.format.gif.chunk.GifChunk
import com.ashampoo.kim.format.gif.chunk.GifChunkApplicationExtension
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.kim.output.writeString

public object GifWriter {
    public fun writeImage(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        xmp: String?
    ): Unit = writeImage(
        chunks = GifImageParser.readChunks(byteReader, null),
        byteWriter = byteWriter,
        xmp = xmp
    )

    public fun writeImage(
        chunks: List<GifChunk>,
        byteWriter: ByteWriter,
        xmp: String? = null
    ) {
        var xmpWritten = false
        val modifiedChunks = chunks.toMutableList()

        // Delete old chunks that are going to be replaced
        if (xmp != null) {
            modifiedChunks.removeAll {
                it is GifChunkApplicationExtension &&
                    it.applicationIdentifier == GifConstants.XMP_APPLICATION_IDENTIFIER
            }
        }

        for (chunk in modifiedChunks) {
            // Write new metadata chunk right before the first image descriptor
            if (GifChunkType.IMAGE_DESCRIPTOR == chunk.type && xmp != null && !xmpWritten) {
                writeXmpChunk(byteWriter, xmp)
                xmpWritten = true
            }

            byteWriter.write(chunk.bytes)
        }

        byteWriter.close()
    }

    private fun writeXmpChunk(byteWriter: ByteWriter, xmpXml: String) {
        byteWriter.write(GifConstants.EXTENSION_INTRODUCER)
        byteWriter.write(GifConstants.APPLICATION_EXTENSION_LABEL)
        byteWriter.write((GifConstants.XMP_APPLICATION_IDENTIFIER + GifConstants.XMP_APPLICATION_CODE).length)
        byteWriter.writeString(GifConstants.XMP_APPLICATION_IDENTIFIER)
        byteWriter.writeString(GifConstants.XMP_APPLICATION_CODE)
        byteWriter.writeString(xmpXml)

        val magicTrailer = ByteArray(256) { (0xFF - it).toByte() }
        byteWriter.write(magicTrailer)
        byteWriter.write(0x00)
    }
}
