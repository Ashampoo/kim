/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package com.ashampoo.kim.output

import com.ashampoo.kim.input.Closeable
import com.ashampoo.kim.input.DEFAULT_BUFFER_SIZE

public class ByteArrayByteWriter : ByteWriter, Closeable {

    private var bytes: ByteArray = ByteArray(DEFAULT_BUFFER_SIZE)

    private var position: Int = 0

    override fun write(byte: Int) {
        ensureCapacity(1)
        bytes[position++] = byte.toByte()
    }

    override fun write(byteArray: ByteArray) {
        ensureCapacity(byteArray.size)
        byteArray.copyInto(bytes, position)
        position += byteArray.size
    }

    override fun close() {
        /* Nothing to do */
    }

    override fun flush() {
        /* Nothing to do */
    }

    public fun toByteArray(): ByteArray = bytes.copyOfRange(0, position)

    private fun ensureCapacity(requiredCapacity: Int) {

        val currentCapacity = bytes.size

        if (position + requiredCapacity > currentCapacity) {

            val newCapacity = (currentCapacity + requiredCapacity) * 2
            val newBytes = ByteArray(newCapacity)

            bytes.copyInto(newBytes)
            bytes = newBytes
        }
    }
}
