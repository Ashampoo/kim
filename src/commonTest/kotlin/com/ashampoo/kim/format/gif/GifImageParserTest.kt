package com.ashampoo.kim.format.gif

import com.ashampoo.kim.format.gif.chunk.GifChunkApplicationExtension
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GifImageParserTest {
    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testExtractGifText() {

        val index = KimTestData.GIF_TEST_IMAGE_INDEX

        val bytes = KimTestData.getHeaderBytesOf(index)

        val chunks = GifImageParser.readChunks(
            ByteArrayByteReader(bytes),
            listOf(
                GifChunkType.HEADER,
                GifChunkType.LOGICAL_SCREEN_DESCRIPTOR,
                GifChunkType.GLOBAL_COLOR_TABLE,
                GifChunkType.APPLICATION_EXTENSION,
                GifChunkType.IMAGE_DESCRIPTOR,
                GifChunkType.IMAGE_DATA,
                GifChunkType.TERMINATOR
            )
        )

        assertNotNull(chunks)
        assertEquals(7, chunks.size)

        val xmpApplicationChunk = chunks[3] as? GifChunkApplicationExtension

        assertNotNull(xmpApplicationChunk)
        assertEquals("XMP Data", xmpApplicationChunk.applicationIdentifier)
        assertEquals("XMP", xmpApplicationChunk.applicationCode)

        val expectedXmp = KimTestData.getHeaderTextFile(index, "xmp").trim()
        val actualXmp = xmpApplicationChunk.parseAsXmpOrThrow().trim()

        assertEquals(expectedXmp, actualXmp, "XMP is different.")
    }
}
