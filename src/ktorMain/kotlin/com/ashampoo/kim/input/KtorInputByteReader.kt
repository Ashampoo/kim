/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

import kotlinx.io.Source
import kotlinx.io.readByteArray

/**
 * This class provides a convenient way to parse metadata directly from
 * a Ktor HttpClient Input. It is particularly useful for parsing metadata
 * of files hosted on a cloud service, which is a common use case.
 *
 * The source is read into a byte array at construction time because
 * [Source.remaining] returns 0 for unbuffered or streaming sources,
 * which would cause [contentLength] to be 0 and metadata parsing to
 * return null.
 */
public class KtorInputByteReader(
    source: Source
) : ByteReader {

    private val bytes: ByteArray = source.readByteArray()

    override val contentLength: Long = bytes.size.toLong()

    private var position: Int = 0

    override fun readByte(): Byte? {

        if (position >= bytes.size)
            return null

        return bytes[position++]
    }

    override fun readBytes(count: Int): ByteArray {

        val remaining = bytes.size - position
        val bytesToRead = minOf(count, remaining)

        val result = bytes.copyOfRange(position, position + bytesToRead)

        position += bytesToRead

        return result
    }

    override fun close() {
        /* Bytes are already buffered; nothing to close. */
    }
}
