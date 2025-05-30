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

import android.os.Build
import com.ashampoo.kim.common.slice
import java.io.InputStream

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

    override fun readBytes(count: Int): ByteArray {

        /*
         * On Android 13 and later use the more efficient API.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return inputStream.readNBytes(count)

        /*
         * Fall back to old API that works on all versions.
         */

        val buffer = ByteArray(count)

        val bytes = inputStream.read(buffer)

        return if (bytes == count)
            buffer
        else
            buffer.slice(startIndex = 0, count = count)
    }

    override fun close(): Unit =
        inputStream.close()
}
