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

/**
 * This class buffers the reading from the original ByteReader and
 * provides random access needed for parsing TIFF files.
 */
class DefaultRandomAccessByteReader(
    val byteReader: ByteReader
) : RandomAccessByteReader, PositionTrackingByteReader {

    override val contentLength: Long =
        byteReader.contentLength

    override val position: Int
        get() = currentPosition

    override val available: Long
        get() = contentLength - position

    private var currentPosition: Int = 0

    private val buffer: MutableList<Byte> = ArrayList(INITIAL_SIZE)

    override fun readByte(): Byte? {

        if (currentPosition >= contentLength)
            return null

        val endIndex = currentPosition + 1

        if (endIndex > buffer.size)
            readToIndex(endIndex)

        return buffer[currentPosition++]
    }

    override fun readBytes(count: Int): ByteArray {

        if (currentPosition >= contentLength)
            return byteArrayOf()

        val endIndex = currentPosition + count.coerceAtMost(contentLength.toInt())

        if (endIndex > buffer.size)
            readToIndex(endIndex)

        val bytes = buffer.subList(currentPosition, endIndex).toByteArray()

        currentPosition += bytes.size

        return bytes
    }

    override fun moveTo(position: Int) {

        require(position <= contentLength - 1) {
            "Can't skip after max length: $position > ${contentLength - 1}"
        }

        this.currentPosition = position
    }

    override fun readBytes(offset: Int, length: Int): ByteArray {

        val endIndex = offset + length

        if (endIndex > buffer.size)
            readToIndex(endIndex)

        return buffer.subList(offset, endIndex).toByteArray()
    }

    override fun close() =
        byteReader.close()

    private fun readToIndex(index: Int) {

        val missingBytesCount = index - buffer.size

        val bytes = byteReader.readBytes(missingBytesCount)

        if (bytes.size == 1)
            buffer.add(bytes.first())
        else
            buffer.addAll(bytes.asIterable())
    }

    companion object {
        const val INITIAL_SIZE = 1024
    }
}
