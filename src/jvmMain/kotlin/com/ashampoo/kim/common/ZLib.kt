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

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

private const val ZLIB_BUFFER_SIZE: Int = 1024

actual fun compress(input: String): ByteArray {

    val deflater = Deflater()
    val inputBytes = input.toByteArray()

    deflater.setInput(inputBytes)
    deflater.finish()

    val outputStream = ByteArrayOutputStream(inputBytes.size)

    val buffer = ByteArray(ZLIB_BUFFER_SIZE)

    while (!deflater.finished()) {

        val count = deflater.deflate(buffer)

        outputStream.write(buffer, 0, count)
    }

    deflater.end()

    return outputStream.toByteArray()
}

actual fun decompress(byteArray: ByteArray): String {

    val inflater = Inflater()
    val outputStream = ByteArrayOutputStream()

    return outputStream.use {

        val buffer = ByteArray(ZLIB_BUFFER_SIZE)

        inflater.setInput(byteArray)

        var count = -1

        while (count != 0) {

            count = inflater.inflate(buffer)

            outputStream.write(buffer, 0, count)
        }

        inflater.end()

        String(outputStream.toByteArray())
    }
}
