package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.format.gif.GifChunkType

public open class GifChunk(
    public val type: GifChunkType,
    public val bytes: ByteArray
)
