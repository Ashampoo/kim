package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.format.gif.GifChunkType

public class GifChunkCommentExtension(
    header: ByteArray,
    subChunks: List<ByteArray>
) : GifChunk(
    GifChunkType.COMMENT_EXTENSION,
    subChunks.fold(header) { acc, subChunk ->
        acc + subChunk
    }.plus(0x00)
)
