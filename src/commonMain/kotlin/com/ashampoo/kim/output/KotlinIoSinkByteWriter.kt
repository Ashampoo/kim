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
package com.ashampoo.kim.output

import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

class KotlinIoSinkByteWriter(
    val sink: Sink,
) : ByteWriter {

    override fun write(byte: Int) =
        sink.writeByte(byte.toByte())

    override fun write(byteArray: ByteArray) =
        sink.write(byteArray)

    override fun flush() =
        sink.flush()

    override fun close() =
        sink.close()

    companion object {

        @OptIn(ExperimentalStdlibApi::class)
        fun <T> write(path: Path, block: (ByteWriter) -> T): T {

            return SystemFileSystem.sink(path).buffered().use { sink ->
                block(KotlinIoSinkByteWriter(sink))
            }
        }
    }
}
