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
package com.ashampoo.kim.input

import io.ktor.utils.io.core.endOfInput
import io.ktor.utils.io.core.remaining
import kotlinx.io.Source
import kotlinx.io.readByteArray

/**
 * This class provides a convenient way to parse metadata directly from
 * a Ktor HttpClient Input. It is particularly useful for parsing metadata
 * of files hosted on a cloud service, which is a common use case.
 */
public class KtorInputByteReader(
    private val source: Source
) : ByteReader {

    override val contentLength: Long = source.remaining

    override fun readByte(): Byte? =
        if (source.endOfInput) null else source.readByte()

    override fun readBytes(count: Int): ByteArray =
        source.readByteArray(minOf(count, source.remaining.toInt()))

    override fun close(): Unit =
        source.close()
}
