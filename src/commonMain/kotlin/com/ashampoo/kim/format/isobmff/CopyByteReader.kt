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
package com.ashampoo.kim.format.isobmff

import com.ashampoo.kim.input.PositionTrackingByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter

internal class CopyByteReader(
    val byteReader: PositionTrackingByteReader
) : PositionTrackingByteReader {

    private val byteWriter = ByteArrayByteWriter()

    override val contentLength: Long =
        byteReader.contentLength

    override val position: Int
        get() = byteReader.position

    override val available: Long
        get() = byteReader.available

    fun getBytes(): ByteArray =
        byteWriter.toByteArray()

    override fun readByte(): Byte? {

        val byte = byteReader.readByte() ?: return null

        byteWriter.write(byteArrayOf(byte))

        return byte
    }

    override fun readBytes(count: Int): ByteArray {

        val bytes = byteReader.readBytes(count)

        byteWriter.write(bytes)

        return bytes
    }

    override fun close() =
        byteReader.close()
}
