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
package com.ashampoo.kim

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.readFileAsByteArray
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.ByteArrayByteReader
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.posix.memcpy

/*
 * Extension functions for integration with Swift.
 */

@Throws(ImageReadException::class)
fun Kim.readMetadata(data: NSData): ImageMetadata? =
    Kim.readMetadata(ByteArrayByteReader(convertDataToByteArray(data)))

@Throws(ImageReadException::class)
fun Kim.readMetadata(path: String): ImageMetadata? {

    val fileBytes = readFileAsByteArray(path) ?: return null

    return Kim.readMetadata(ByteArrayByteReader(fileBytes))
}

@OptIn(ExperimentalForeignApi::class)
private fun convertDataToByteArray(data: NSData): ByteArray {

    return ByteArray(data.length.toInt()).apply {
        usePinned {
            memcpy(
                __dst = it.addressOf(0),
                __src = data.bytes,
                __n = data.length
            )
        }
    }
}
