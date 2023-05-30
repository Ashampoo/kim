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

class ByteArrayByteReader(
    private val bytes: ByteArray
) : RandomAccessByteReader {

    private var position = 0

    override fun readByte(): Byte? {

        if (position == bytes.size)
            return null

        return bytes[position++]
    }

    override fun readBytes(count: Int): ByteArray {

        val bytes = bytes.copyOfRange(position, min(position + count, bytes.size))

        position += bytes.size

        return bytes
    }

    override fun reset() {
        this.position = 0
    }

    override fun skipTo(position: Int) {
        this.position = position
    }

    override fun readBytes(start: Int, length: Int): ByteArray =
        bytes.copyOfRange(start, start + length)

    override fun getLength(): Long = bytes.size.toLong()

    override fun close() {
        /* Does nothing. */
    }
}
