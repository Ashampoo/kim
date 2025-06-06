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

import com.ashampoo.kim.common.exists
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlin.math.min

public class KotlinIoSourceByteReader(
    private val source: Source,
    override val contentLength: Long
) : ByteReader {

    private var position = 0

    private val remainingByteCount: Int
        get() = (contentLength - position).toInt()

    override fun readByte(): Byte? {

        if (source.exhausted())
            return null

        position++

        return source.readByte()
    }

    override fun readBytes(count: Int): ByteArray {

        val bytes = source.readByteArray(min(count, remainingByteCount))

        position += bytes.size

        return bytes
    }

    override fun close(): Unit =
        source.close()

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun <T> read(path: Path, block: (ByteReader?) -> T): T {

            if (!path.exists())
                return block(null)

            val metadata = SystemFileSystem.metadataOrNull(path)

            if (metadata == null || !metadata.isRegularFile)
                return block(null)

            return SystemFileSystem.source(path).buffered().use { source ->
                block(KotlinIoSourceByteReader(source, metadata.size))
            }
        }
    }
}
