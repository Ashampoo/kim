package com.ashampoo.kim.format.gif

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.tryWithImageWriteException
import com.ashampoo.kim.format.MetadataUpdater
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object GifUpdater : MetadataUpdater {
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ) = tryWithImageWriteException {

        val chunks = GifImageParser.readChunks(byteReader, chunkTypeFilter = null)

        val metadata = GifImageParser.parseMetadataFromChunks(chunks)

        val xmpMeta: XMPMeta = if (metadata.xmp != null)
            XMPMetaFactory.parseFromString(metadata.xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, update, true)

        GifWriter.writeImage(
            chunks = chunks,
            byteWriter = byteWriter,
            xmp = updatedXmp
        )
    }

    override fun updateThumbnail(bytes: ByteArray, thumbnailBytes: ByteArray): ByteArray {
        throw ImageWriteException("Can't embed thumbnail into GIF.")
    }
}
