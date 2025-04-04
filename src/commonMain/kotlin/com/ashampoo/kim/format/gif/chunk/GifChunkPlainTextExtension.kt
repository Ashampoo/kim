package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.format.gif.GifChunkType

public class GifChunkPlainTextExtension(
    header: ByteArray,
    subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.PLAIN_TEXT_EXTENSION,
    subChunks.fold(header) { acc, subChunk ->
        acc + subChunk
    }.plus(0x00)
)
