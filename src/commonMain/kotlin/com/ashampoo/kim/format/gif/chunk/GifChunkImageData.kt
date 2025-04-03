package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.format.gif.GifChunkType

public class GifChunkImageData(
    lzwMinimumCodeSize: Byte,
    subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.IMAGE_DATA,
    subChunks.fold(byteArrayOf(lzwMinimumCodeSize)) { acc, subChunk ->
        acc + subChunk
    }
)
