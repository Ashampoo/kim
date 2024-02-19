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

import kotlin.math.max

/**
 * This class buffers the reading from the original ByteReader and
 * provides random access needed for parsing TIFF files.
 */
class DefaultRandomAccessByteReader(
    val byteReader: ByteReader
) : RandomAccessByteReader {

    override val contentLength: Long =
        byteReader.contentLength

    private var currentPosition: Int = 0

    private var bufferedBytesSize: Int = 0

    private var buffer = ByteArray(0)

    override fun readByte(): Byte? {

        if (currentPosition >= contentLength)
            return null

        val endIndex = currentPosition + 1

        if (endIndex > bufferedBytesSize)
            readToIndex(endIndex)

        return buffer[currentPosition++]
    }

    override fun readBytes(count: Int): ByteArray {

        if (currentPosition >= contentLength)
            return byteArrayOf()

        val endIndex = currentPosition + count.coerceAtMost(contentLength.toInt())

        if (endIndex > bufferedBytesSize)
            readToIndex(endIndex)

        val bytes = buffer.copyOfRange(currentPosition, endIndex)

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

        if (endIndex > bufferedBytesSize)
            readToIndex(endIndex)

        return buffer.copyOfRange(offset, endIndex)
    }

    override fun close() =
        byteReader.close()

    private fun readToIndex(index: Int) {

        val missingBytesCount = index - bufferedBytesSize

        val bytes = byteReader.readBytes(missingBytesCount)

        val oldSize = buffer.size

        val newSizeRequiredSize = oldSize + bytes.size

        if (newSizeRequiredSize > buffer.size) {

            /*
             * Copying an array is expensive. So we want at least expand
             * it with a defined BUFFER_EXPANSION and never with just one byte.
             */

            val newBufferSize =
                max(newSizeRequiredSize, oldSize + BUFFER_EXPANSION)

            // TODO only expand a minimum of BUFFER_EXPANSION

            buffer = buffer.copyOf(newSizeRequiredSize)
        }

        for (i in bytes.indices)
            buffer[oldSize + i] = bytes[i]

        bufferedBytesSize = newSizeRequiredSize
    }

    companion object {

        private const val BUFFER_EXPANSION = 1024
    }
}
