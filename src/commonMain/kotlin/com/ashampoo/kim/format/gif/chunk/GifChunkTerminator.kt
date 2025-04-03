package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.gif.GifChunkType
import com.ashampoo.kim.format.gif.GifConstants

public class GifChunkTerminator(bytes: ByteArray) : GifChunk(GifChunkType.TERMINATOR, bytes) {
    init {
        if (bytes.size != 1 || bytes[0] != GifConstants.GIF_TERMINATOR)
            throw ImageReadException("Invalid GIF trailer byte(s): $bytes")
    }
}
