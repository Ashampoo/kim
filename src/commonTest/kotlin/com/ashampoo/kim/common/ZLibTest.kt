/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ashampoo.kim.common

import com.ashampoo.kim.getPathForResource
import kotlinx.io.files.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ZLibTest {

    private val zlibTestData: Map<String, ByteArray> = mapOf(

        "Hello, World!" to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x48.toByte(), 0xcd.toByte(), 0xc9.toByte(),
            0xc9.toByte(), 0xd7.toByte(), 0x51.toByte(), 0x08.toByte(), 0xcf.toByte(), 0x2f.toByte(),
            0xca.toByte(), 0x49.toByte(), 0x51.toByte(), 0x04.toByte(), 0x00.toByte(), 0x1f.toByte(),
            0x9e.toByte(), 0x04.toByte(), 0x6a.toByte()
        ),
        "I love Kotlin!" to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x54.toByte(), 0xc8.toByte(), 0xc9.toByte(),
            0x2f.toByte(), 0x4b.toByte(), 0x55.toByte(), 0xf0.toByte(), 0xce.toByte(), 0x2f.toByte(),
            0xc9.toByte(), 0xc9.toByte(), 0xcc.toByte(), 0x53.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x23.toByte(), 0x7d.toByte(), 0x04.toByte(), 0xd2.toByte()
        ),
        "Ashampoo GmbH & Co. KG" to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x73.toByte(), 0x2c.toByte(), 0xce.toByte(), 0x48.toByte(),
            0xcc.toByte(), 0x2d.toByte(), 0xc8.toByte(), 0xcf.toByte(), 0x57.toByte(), 0x70.toByte(),
            0xcf.toByte(), 0x4d.toByte(), 0xf2.toByte(), 0x50.toByte(), 0x50.toByte(), 0x53.toByte(),
            0x70.toByte(), 0xce.toByte(), 0xd7.toByte(), 0x53.toByte(), 0xf0.toByte(), 0x76.toByte(),
            0x07.toByte(), 0x00.toByte(), 0x55.toByte(), 0x58.toByte(), 0x06.toByte(), 0xaf.toByte()
        ),
        "The quick brown fox jumps over the lazy dog." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
            0x28.toByte(), 0x2c.toByte(), 0xcd.toByte(), 0x4c.toByte(), 0xce.toByte(), 0x56.toByte(),
            0x48.toByte(), 0x2a.toByte(), 0xca.toByte(), 0x2f.toByte(), 0xcf.toByte(), 0x53.toByte(),
            0x48.toByte(), 0xcb.toByte(), 0xaf.toByte(), 0x50.toByte(), 0xc8.toByte(), 0x2a.toByte(),
            0xcd.toByte(), 0x2d.toByte(), 0x28.toByte(), 0x56.toByte(), 0xc8.toByte(), 0x2f.toByte(),
            0x4b.toByte(), 0x2d.toByte(), 0x52.toByte(), 0x28.toByte(), 0x01.toByte(), 0x4a.toByte(),
            0xe7.toByte(), 0x24.toByte(), 0x56.toByte(), 0x55.toByte(), 0x2a.toByte(), 0xa4.toByte(),
            0xe4.toByte(), 0xa7.toByte(), 0xeb.toByte(), 0x01.toByte(), 0x00.toByte(), 0x6b.toByte(),
            0xe4.toByte(), 0x10.toByte(), 0x08.toByte()
        ),
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x05.toByte(), 0xc1.toByte(), 0x81.toByte(), 0x09.toByte(),
            0x40.toByte(), 0x21.toByte(), 0x08.toByte(), 0x05.toByte(), 0xc0.toByte(), 0x55.toByte(),
            0xde.toByte(), 0x00.toByte(), 0xd1.toByte(), 0x24.toByte(), 0x7f.toByte(), 0x89.toByte(),
            0x30.toByte(), 0x89.toByte(), 0x07.toByte(), 0x99.toByte(), 0xa1.toByte(), 0xb6.toByte(),
            0xFF.toByte(), 0xbf.toByte(), 0xfb.toByte(), 0x3c.toByte(), 0xd4.toByte(), 0xc0.toByte(),
            0x9b.toByte(), 0xcf.toByte(), 0x30.toByte(), 0x7d.toByte(), 0x7b.toByte(), 0x20.toByte(),
            0x59.toByte(), 0x18.toByte(), 0xa6.toByte(), 0xd5.toByte(), 0x20.toByte(), 0x7e.toByte(),
            0x52.toByte(), 0xa5.toByte(), 0xb4.toByte(), 0x5e.toByte(), 0x60.toByte(), 0x4c.toByte(),
            0x5e.toByte(), 0xa6.toByte(), 0xf0.toByte(), 0x2c.toByte(), 0xe8.toByte(), 0x66.toByte(),
            0xf5.toByte(), 0x1f.toByte(), 0x55.toByte(), 0x03.toByte(), 0x14.toByte(), 0xf7.toByte()
        ),
        "Compressing and decompressing data using zlib is efficient." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x73.toByte(), 0xce.toByte(), 0xcf.toByte(), 0x2d.toByte(),
            0x28.toByte(), 0x4a.toByte(), 0x2d.toByte(), 0x2e.toByte(), 0xce.toByte(), 0xcc.toByte(),
            0x4b.toByte(), 0x57.toByte(), 0x48.toByte(), 0xcc.toByte(), 0x4b.toByte(), 0x51.toByte(),
            0x48.toByte(), 0x49.toByte(), 0x4d.toByte(), 0x46.toByte(), 0x12.toByte(), 0x49.toByte(),
            0x49.toByte(), 0x2c.toByte(), 0x49.toByte(), 0x54.toByte(), 0x28.toByte(), 0x05.toByte(),
            0x33.toByte(), 0xab.toByte(), 0x72.toByte(), 0x32.toByte(), 0x93.toByte(), 0x14.toByte(),
            0x32.toByte(), 0x8b.toByte(), 0x15.toByte(), 0x52.toByte(), 0xd3.toByte(), 0xd2.toByte(),
            0x32.toByte(), 0x93.toByte(), 0x33.toByte(), 0x53.toByte(), 0xf3.toByte(), 0x4a.toByte(),
            0xf4.toByte(), 0x00.toByte(), 0xa5.toByte(), 0x28.toByte(), 0x16.toByte(), 0x39.toByte()
        ),
        "I love coding and exploring new technologies." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0xf3.toByte(), 0x54.toByte(), 0xc8.toByte(), 0xc9.toByte(),
            0x2f.toByte(), 0x4b.toByte(), 0x55.toByte(), 0x48.toByte(), 0xce.toByte(), 0x4f.toByte(),
            0xc9.toByte(), 0xcc.toByte(), 0x4b.toByte(), 0x57.toByte(), 0x48.toByte(), 0xcc.toByte(),
            0x4b.toByte(), 0x51.toByte(), 0x48.toByte(), 0xad.toByte(), 0x28.toByte(), 0xc8.toByte(),
            0xc9.toByte(), 0x2f.toByte(), 0x02.toByte(), 0xf1.toByte(), 0xf2.toByte(), 0x52.toByte(),
            0xcb.toByte(), 0x15.toByte(), 0x4a.toByte(), 0x52.toByte(), 0x93.toByte(), 0x33.toByte(),
            0xf2.toByte(), 0xf2.toByte(), 0x73.toByte(), 0xf2.toByte(), 0xd3.toByte(), 0x33.toByte(),
            0x53.toByte(), 0x8b.toByte(), 0xf5.toByte(), 0x00.toByte(), 0x77.toByte(), 0xd7.toByte(),
            0x10.toByte(), 0xbb.toByte()
        ),
        "The weather today is sunny and warm." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
            0x28.toByte(), 0x4f.toByte(), 0x4d.toByte(), 0x2c.toByte(), 0xc9.toByte(), 0x48.toByte(),
            0x2d.toByte(), 0x52.toByte(), 0x28.toByte(), 0xc9.toByte(), 0x4f.toByte(), 0x49.toByte(),
            0xac.toByte(), 0x54.toByte(), 0xc8.toByte(), 0x2c.toByte(), 0x56.toByte(), 0x28.toByte(),
            0x2e.toByte(), 0xcd.toByte(), 0xcb.toByte(), 0xab.toByte(), 0x54.toByte(), 0x48.toByte(),
            0xcc.toByte(), 0x4b.toByte(), 0x51.toByte(), 0x28.toByte(), 0x4f.toByte(), 0x2c.toByte(),
            0xca.toByte(), 0xd5.toByte(), 0x03.toByte(), 0x00.toByte(), 0xf5.toByte(), 0x2b.toByte(),
            0x0d.toByte(), 0x24.toByte()
        ),
        "This is a sample sentence for testing purposes." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0xc8.toByte(), 0x2c.toByte(),
            0x56.toByte(), 0x00.toByte(), 0xa2.toByte(), 0x44.toByte(), 0x85.toByte(), 0xe2.toByte(),
            0xc4.toByte(), 0xdc.toByte(), 0x82.toByte(), 0x9c.toByte(), 0x54.toByte(), 0x85.toByte(),
            0xe2.toByte(), 0xd4.toByte(), 0xbc.toByte(), 0x92.toByte(), 0xd4.toByte(), 0xbc.toByte(),
            0xe4.toByte(), 0x54.toByte(), 0x85.toByte(), 0xb4.toByte(), 0xfc.toByte(), 0x22.toByte(),
            0x85.toByte(), 0x92.toByte(), 0xd4.toByte(), 0xe2.toByte(), 0x92.toByte(), 0xcc.toByte(),
            0xbc.toByte(), 0x74.toByte(), 0x85.toByte(), 0x82.toByte(), 0xd2.toByte(), 0xa2.toByte(),
            0x82.toByte(), 0xfc.toByte(), 0xe2.toByte(), 0xd4.toByte(), 0x62.toByte(), 0x3d.toByte(),
            0x00.toByte(), 0x9a.toByte(), 0x72.toByte(), 0x11.toByte(), 0x81.toByte()
        ),
        "The cat in the hat." to byteArrayOf(
            0x78.toByte(), 0x9c.toByte(), 0x0b.toByte(), 0xc9.toByte(), 0x48.toByte(), 0x55.toByte(),
            0x48.toByte(), 0x4e.toByte(), 0x2c.toByte(), 0x51.toByte(), 0xc8.toByte(), 0xcc.toByte(),
            0x53.toByte(), 0x28.toByte(), 0x01.toByte(), 0x32.toByte(), 0x33.toByte(), 0x12.toByte(),
            0x4b.toByte(), 0xf4.toByte(), 0x00.toByte(), 0x40.toByte(), 0x11.toByte(), 0x06.toByte(),
            0x5d.toByte()
        )
    )

    @Test
    fun testCompress() {

        for (entry in zlibTestData)
            assertContentEquals(entry.value, compress(entry.key))
    }

    @Test
    fun testDecompress() {

        for (entry in zlibTestData)
            assertEquals(entry.key, decompress(entry.value))
    }

    @Test
    fun testRoundtripWithLongText() {

        val testString = Path(getPathForResource(RESOURCE_PATH)).readBytes().decodeToString()

        val compressed = compress(testString)

        val decompressed = decompress(compressed)

        assertEquals(
            expected = testString,
            actual = decompressed,
            message = "Test string differs."
        )
    }

    companion object {

        private const val RESOURCE_PATH: String =
            "src/commonTest/resources/com/ashampoo/kim/testdata/alice_in_wonderland.txt"
    }
}
