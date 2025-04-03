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

import java.io.InputStream


private const val MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8

/**
 * Provides way to read from Android ContentReolver that
 * should work on all versions.
 */
public open class AndroidInputStreamByteReader(
    private val inputStream: InputStream,
    override val contentLength: Long
) : ByteReader {

    override fun readByte(): Byte? {

        val nextByte = inputStream.read()

        if (nextByte == -1)
            return null

        return nextByte.toByte()
    }

    override fun readBytes(count: Int): ByteArray =
        inputStream.readBytes(count)

    override fun close(): Unit =
        inputStream.close()
}

@Suppress("NestedBlockDepth")
// Code copied from readNBytes (Android 35)
private fun InputStream.readBytes(len: Int): ByteArray {
    require(len >= 0)

    var bufs: MutableList<ByteArray>? = null
    var result: ByteArray? = null
    var total = 0
    var remaining = len
    var n: Int

    do {
        val buf = ByteArray(minOf(remaining, DEFAULT_BUFFER_SIZE))
        var nread = 0

        while (read(buf, nread, minOf(buf.size - nread, remaining)).also { n = it } > 0) {
            nread += n
            remaining -= n
        }

        if (nread > 0) {
            if (MAX_BUFFER_SIZE - total < nread) {
                throw OutOfMemoryError("Required array size too large")
            }
            val bufToStore = if (nread < buf.size) buf.copyOf(nread) else buf
            total += nread

            if (result == null) {
                result = bufToStore
            } else {
                if (bufs == null) {
                    bufs = mutableListOf()
                    bufs.add(result)
                }
                bufs.add(bufToStore)
            }
        }
    } while (n >= 0 && remaining > 0)

    if (bufs == null) {
        return result ?: ByteArray(0)
    }

    result = ByteArray(total)
    var offset = 0
    remaining = total
    for (b in bufs) {
        val count = minOf(b.size, remaining)
        System.arraycopy(b, 0, result, offset, count)
        offset += count
        remaining -= count
    }

    return result
}
