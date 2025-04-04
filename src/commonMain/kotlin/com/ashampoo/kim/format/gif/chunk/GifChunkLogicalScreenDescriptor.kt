package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.gif.GifChunkType
import com.ashampoo.kim.format.gif.GifConstants
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.model.ImageSize
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.write2BytesAsInt
import kotlin.jvm.JvmStatic

public class GifChunkLogicalScreenDescriptor(
    bytes: ByteArray
) : GifChunk(GifChunkType.LOGICAL_SCREEN_DESCRIPTOR, bytes) {

    public val canvasSize: ImageSize
    public val globalColorTableFlag: Boolean
    public val colorResolution: Int
    public val sortFlag: Boolean
    public val globalColorTableSize: Int
    public val backgroundColorIndex: Int
    public val pixelAspectRatio: Int

    init {
        if (bytes.size != 7)
            throw ImageReadException(
                "Invalid size for logical screen descriptor: ${bytes.size} bytes, expected 7 bytes."
            )

        val byteReader = ByteArrayByteReader(bytes)

        // Read canvas width and height
        val canvasWidth = byteReader.read2BytesAsInt("canvas width", GifConstants.GIF_BYTE_ORDER)
        val canvasHeight = byteReader.read2BytesAsInt("canvas height", GifConstants.GIF_BYTE_ORDER)
        canvasSize = ImageSize(canvasWidth, canvasHeight)

        // Read packed data
        val packed = byteReader.readByte("packed fields").toInt()
        globalColorTableFlag = (packed shr 7 and 1) == 1
        colorResolution = (packed shr 4) and 0b111
        sortFlag = (packed shr 3 and 1) == 1
        globalColorTableSize = packed and 0b111

        // Read background color index
        backgroundColorIndex = byteReader.readByteAsInt()

        // Read pixel aspect ratio
        pixelAspectRatio = byteReader.readByteAsInt()
    }

    public companion object {
        @JvmStatic
        public fun constructFromProperties(
            canvasSize: ImageSize,
            globalColorTableFlag: Boolean,
            colorResolution: Int,
            sortFlag: Boolean,
            globalColorTableSize: Int,
            backgroundColorIndex: Int,
            pixelAspectRatio: Int
        ): GifChunkLogicalScreenDescriptor {
            val byteWriter = ByteArrayByteWriter()

            byteWriter.write2BytesAsInt(canvasSize.width, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(canvasSize.height, GifConstants.GIF_BYTE_ORDER)

            val packed = (
                ((if (globalColorTableFlag) 1 else 0) shl 7) or
                    ((colorResolution and 0b111) shl 4) or
                    ((if (sortFlag) 1 else 0) shl 3) or
                    (globalColorTableSize and 0b111)
                ).toByte()

            byteWriter.write(packed)

            byteWriter.write(backgroundColorIndex)
            byteWriter.write(pixelAspectRatio)

            return GifChunkLogicalScreenDescriptor(byteWriter.toByteArray())
        }
    }
}
