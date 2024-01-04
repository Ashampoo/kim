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
 * This is a Decorator to track the current position in reading the file.
 *
 * The ByteReader interface has many complex implementations, where some
 * of them aren't even aware of the current position or the position tracking
 * is an implementation detail that should not be exposed.
 * Therefore it's better to track the position on a higher level.
 */
class PositionTrackingByteReader(
    val byteReader: ByteReader
) : ByteReader {

    override val contentLength: Long =
        byteReader.contentLength

    private var currentPosition: Int = 0

    val position: Int
        get() = currentPosition

    val available: Long
        get() = byteReader.contentLength - position

    override fun readByte(): Byte? {

        val byte = byteReader.readByte()

        if (byte != null)
            currentPosition++

        return byte
    }

    override fun readBytes(count: Int): ByteArray {

        val bytes = byteReader.readBytes(count)

        currentPosition += bytes.size

        return bytes
    }

    override fun close() {
        /* Do nothing */
    }
}
