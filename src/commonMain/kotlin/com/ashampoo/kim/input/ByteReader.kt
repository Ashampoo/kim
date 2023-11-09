/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.quadsToByteArray
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.output.ByteArrayByteWriter
import io.ktor.utils.io.core.Closeable

@Suppress("TooManyFunctions", "ComplexInterface", "MagicNumber")
interface ByteReader : Closeable {

    val contentLength: Long

    /**
     * Returns the next Byte, if any.
     */
    fun readByte(): Byte?

    fun readBytes(count: Int): ByteArray

    fun readByte(fieldName: String): Byte {

        val byte = readByte()

        if (byte == null)
            throw ImageReadException("Couldn't read byte for $fieldName")

        return byte
    }

    fun readBytes(fieldName: String, length: Int): ByteArray {

        if (length < 0)
            throw ImageReadException("Couldn't read $fieldName, invalid length: $length")

        val bytes = readBytes(length)

        if (bytes.size != length)
            throw ImageReadException("Couldn't read $length bytes for $fieldName. Got only ${bytes.size}.")

        return bytes
    }

    private fun readAsInt(): Int =
        readByte()?.let { it.toInt() and 0xFF } ?: -1

    fun read2BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

        val byte0 = readAsInt()
        val byte1 = readAsInt()

        if (byte0 or byte1 < 0)
            throw ImageReadException("Couldn't read two bytes for $fieldName")

        return if (byteOrder == ByteOrder.BIG_ENDIAN)
            byte0 shl 8 or byte1
        else
            byte1 shl 8 or byte0
    }

    fun read4BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

        val byte0 = readAsInt()
        val byte1 = readAsInt()
        val byte2 = readAsInt()
        val byte3 = readAsInt()

        if (byte0 or byte1 or byte2 or byte3 < 0)
            throw ImageReadException("Couldn't read 4 bytes for $fieldName")

        val result: Int = if (byteOrder == ByteOrder.BIG_ENDIAN)
            byte0 shl 24 or (byte1 shl 16) or (byte2 shl 8) or (byte3 shl 0)
        else
            byte3 shl 24 or (byte2 shl 16) or (byte1 shl 8) or (byte0 shl 0)

        return result
    }

    fun readAndVerifyBytes(fieldName: String, expectedBytes: ByteArray) {

        for (index in expectedBytes.indices) {

            val byte = readByte()

            if (byte == null)
                throw ImageReadException("Unexpected EOF for $fieldName")

            if (byte != expectedBytes[index])
                throw ImageReadException("Byte $index is different by reading $fieldName: ${byte.toHex()}")
        }
    }

    fun readRemainingBytes(): ByteArray {

        val os = ByteArrayByteWriter()

        while (true) {

            val bytes = readBytes(DEFAULT_BUFFER_SIZE)

            if (bytes.isEmpty())
                break

            os.write(bytes)
        }

        return os.toByteArray()
    }

    fun skipBytes(name: String, length: Long) {

        if (length == 0L)
            return

        var total: Long = 0

        while (length != total) {

            val skipped = readBytes(length.toInt()).size

            if (skipped < 1)
                throw ImageReadException("$name (skipped $skipped of $length bytes)")

            total += skipped
        }
    }

    fun skipToQuad(quad: Int): Boolean {

        val needle = quad.quadsToByteArray()

        var position = 0

        while (true) {

            val byte = readAsInt()

            if (byte == -1)
                break

            if (needle[position].toInt() == byte) {

                position++

                if (position == needle.size)
                    return true

            } else
                position = 0
        }

        return false
    }
}
