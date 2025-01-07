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

import com.ashampoo.kim.common.ImageWriteException

internal class BufferByteWriter(
    private val buffer: ByteArray,
    private var index: Int
) : ByteWriter {

    override fun write(byte: Int) {

        if (index >= buffer.size)
            throw ImageWriteException("Buffer overflow.")

        buffer[index++] = byte.toByte()
    }

    override fun write(byteArray: ByteArray) {

        if (index + byteArray.size > buffer.size)
            throw ImageWriteException("Buffer overflow.")

        byteArray.copyInto(
            destination = buffer,
            destinationOffset = index,
            startIndex = 0,
            endIndex = 0 + byteArray.size
        )

        index += byteArray.size
    }

    override fun flush() {
        /* Does nothing. */
    }

    override fun close() {
        /* Does nothing. */
    }
}
