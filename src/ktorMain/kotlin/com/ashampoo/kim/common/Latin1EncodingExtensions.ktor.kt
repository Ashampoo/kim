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
package com.ashampoo.kim.common

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.charsets.decode
import io.ktor.utils.io.charsets.encodeToByteArray
import kotlinx.io.Buffer

private val decoder = Charsets.ISO_8859_1.newDecoder()
private val encoder = Charsets.ISO_8859_1.newEncoder()

internal actual fun ByteArray.decodeLatin1BytesToString(): String {

    val buffer = Buffer()
    buffer.write(this)

    return decoder.decode(buffer)
}

internal actual fun String.encodeToLatin1Bytes(): ByteArray =
    encoder.encodeToByteArray(this)
