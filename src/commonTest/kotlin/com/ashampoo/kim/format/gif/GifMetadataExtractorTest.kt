package com.ashampoo.kim.format.gif

import com.ashampoo.kim.Kim
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertTrue

class GifMetadataExtractorTest {
    @Test
    fun testExtractMetadataBytes() {

        val index = KimTestData.GIF_TEST_IMAGE_INDEX

        val bytes = KimTestData.getBytesOf(index)

        val byteReader = ByteArrayByteReader(bytes)

        /* Use the public Kim interface to ensure it works. */
        val actualMetadataBytes = Kim.extractMetadataBytes(byteReader).second

        val expectedMetadataBytes = KimTestData.getHeaderBytesOf(index)

        assertTrue(
            expectedMetadataBytes.contentEquals(actualMetadataBytes),
            "Photo $index has not the expected bytes!"
        )
    }
}
