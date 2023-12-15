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
package com.ashampoo.kim.common

import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.khronos.webgl.set

actual fun compress(input: String): ByteArray =
    Pako.deflate(input).toByteArray()

actual fun decompress(byteArray: ByteArray): String =
    Pako.inflate(byteArray.toUint8Array(), toStringOptions)

private val toStringOptions: JsAny = js("({to: 'string'})")

private fun Uint8Array.toByteArray(): ByteArray =
    ByteArray(length) { this[it] }

private fun ByteArray.toUint8Array(): Uint8Array {
    val result = Uint8Array(size)
    forEachIndexed { index, byte ->
        result[index] = byte
    }
    return result
}

@JsModule("pako")
private external object Pako {
    fun deflate(data: String): Uint8Array
    fun inflate(data: Uint8Array, options: JsAny): String
}
