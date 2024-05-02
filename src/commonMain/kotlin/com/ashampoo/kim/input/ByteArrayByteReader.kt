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

import kotlin.math.min

/**
 * ByteArray backed ByteReader
 *
 * This is intended to be used for EXIF, because this is at max 64 kb in size.
 *
 * Note that huge files in production shouldn't be loaded into a ByteArray.
 * For unit the purpose of unit tests this is acceptable.
 */
public class ByteArrayByteReader(
    private val bytes: ByteArray
) : RandomAccessByteReader {

    override val contentLength: Long =
        bytes.size.toLong()

    private var currentPosition = 0

    override fun readByte(): Byte? {

        if (currentPosition == bytes.size)
            return null

        return bytes[currentPosition++]
    }

    override fun readBytes(count: Int): ByteArray {

        val targetToIndex = currentPosition + count

        val bytes = bytes.copyOfRange(
            fromIndex = currentPosition,
            toIndex = min(targetToIndex, bytes.size)
        )

        currentPosition += bytes.size

        return bytes
    }

    override fun moveTo(position: Int) {

        require(position <= contentLength) {
            "Can't move to $position in content of $contentLength bytes."
        }

        this.currentPosition = position
    }

    override fun readBytes(offset: Int, length: Int): ByteArray {

        require(offset >= 0) { "Offset must be positive: $offset" }
        require(length > 0) { "Length must be positive: $length" }

        val toIndex = offset + length

        require(offset + length <= contentLength) {
            "Requested to read to index $toIndex where max index is ${contentLength - 1}"
        }

        return bytes.copyOfRange(offset, toIndex)
    }

    override fun close() {
        /* Does nothing. */
    }
}
