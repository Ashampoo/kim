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
package com.ashampoo.kim.input

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PrePendingByteReaderTest {

    @Suppress("MagicNumber")
    private val prependedBytes = listOf(
        0xFF.toByte(),
        0xD8.toByte(),
        0xFF.toByte()
    )

    @Suppress("MagicNumber")
    private val bytesFromReader = listOf(
        42.toByte(),
        77.toByte(),
        (-4).toByte()
    )

    @Test
    @Suppress("MagicNumber")
    fun testReadByte() {

        val reader = PrePendingByteReader(
            delegate = ByteArrayByteReader(bytesFromReader.toByteArray()),
            prependedBytes = prependedBytes
        )

        /* First we get the prepended bytes. */
        assertEquals(prependedBytes[0], reader.readByte())
        assertEquals(prependedBytes[1], reader.readByte())
        assertEquals(prependedBytes[2], reader.readByte())

        /* After that we get the bytes from the reader. */
        assertEquals(bytesFromReader[0], reader.readByte())
        assertEquals(bytesFromReader[1], reader.readByte())
        assertEquals(bytesFromReader[2], reader.readByte())

        /* We get null if we read everything. */
        assertNull(reader.readByte())
    }

    @Test
    @Suppress("MagicNumber")
    fun testReadBytes() {

        val reader = PrePendingByteReader(
            delegate = ByteArrayByteReader(bytesFromReader.toByteArray()),
            prependedBytes = prependedBytes
        )

        assertEquals(
            expected = prependedBytes + bytesFromReader,
            actual = reader.readBytes(prependedBytes.size + bytesFromReader.size).toList()
        )
    }
}
