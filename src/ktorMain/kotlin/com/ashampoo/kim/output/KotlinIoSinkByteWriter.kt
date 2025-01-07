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
package com.ashampoo.kim.output

import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

public class KotlinIoSinkByteWriter(
    private val sink: Sink,
) : ByteWriter {

    override fun write(byte: Int): Unit =
        sink.writeByte(byte.toByte())

    override fun write(byteArray: ByteArray): Unit =
        sink.write(byteArray)

    override fun flush(): Unit =
        sink.flush()

    override fun close(): Unit =
        sink.close()

    public companion object {

        @OptIn(ExperimentalStdlibApi::class)
        public fun <T> write(path: Path, block: (ByteWriter) -> T): T {

            return SystemFileSystem.sink(path).buffered().use { sink ->
                block(KotlinIoSinkByteWriter(sink))
            }
        }
    }
}
