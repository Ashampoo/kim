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

/**
 * Prepends the given list of [Byte]s and continues to
 * read from [ByteReader] after that. Can be used to
 * prepend the magic number again.
 */
class PrePendingByteReader(
    private val delegate: ByteReader,
    prependedBytes: List<Byte>
) : ByteReader {

    override val contentLength: Long = delegate.contentLength

    private val prependedBytesBuffer = ArrayDeque(prependedBytes)

    override fun readByte(): Byte? {

        if (prependedBytesBuffer.isNotEmpty())
            return prependedBytesBuffer.removeFirst()

        return delegate.readByte()
    }

    override fun readBytes(count: Int): ByteArray {

        if (prependedBytesBuffer.isEmpty())
            return delegate.readBytes(count)

        val bytes = ByteArray(count)
        var bytesRead = 0

        /* Read prepended bytes first */
        while (bytesRead < count && prependedBytesBuffer.isNotEmpty()) {

            bytes[bytesRead] = prependedBytesBuffer.removeFirst()

            bytesRead++
        }

        /* Read remaining bytes from the delegate */
        val delegateBytes = delegate.readBytes(count - bytesRead)

        delegateBytes.copyInto(bytes, bytesRead)

        return bytes
    }

    override fun close() {
        delegate.close()
    }
}
