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

import kotlin.math.min

class DefaultRandomAccessByteReader(
    val byteReader: ByteReader,
    val size: Long
) : RandomAccessByteReader {

    private var position: Int = 0

    private val buffer = mutableListOf<Byte>()

    override fun readByte(): Byte? {

        /* Check if the end is reached. */
        if (position > size)
            return null

        /*
         * Fill the buffer as much as needed
         */
        if (position + 1 > buffer.size) {

            val missingBytesCount = position + 1 - buffer.size

            val bytes = byteReader.readBytes(missingBytesCount)

            buffer.addAll(bytes.toList())
        }

        return buffer[position++]
    }

    override fun readBytes(count: Int): ByteArray {

        /* Check if the end is reached. */
        if (position > size)
            return byteArrayOf()

        val endIndex: Int = min(position + count, size.toInt())

        /*
         * Fill the buffer as much as needed
         */
        if (endIndex + 1 > buffer.size) {

            val missingBytesCount = endIndex + 1 - buffer.size

            val bytes = byteReader.readBytes(missingBytesCount)

            buffer.addAll(bytes.toList())
        }

        val bytes = buffer.subList(position, endIndex).toByteArray()

        position += bytes.size

        return bytes
    }

    override fun reset() {
        this.position = 0
    }

    override fun skipTo(position: Int) {

        require(position <= size) {
            "Can't skip after max length: $position > $size"
        }

        this.position = position
    }

    override fun readBytes(start: Int, length: Int): ByteArray {

        val endIndex = start + length

        /*
         * Fill the buffer as much as needed
         */
        if (endIndex + 1 > buffer.size) {

            val missingBytesCount = endIndex + 1 - buffer.size

            val bytes = byteReader.readBytes(missingBytesCount)

            buffer.addAll(bytes.toList())
        }

        return buffer.subList(start, endIndex).toByteArray()
    }

    override fun getLength(): Long = size

    override fun close() =
        byteReader.close()
}
