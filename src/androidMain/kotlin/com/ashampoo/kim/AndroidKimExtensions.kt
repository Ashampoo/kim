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
package com.ashampoo.kim

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.AndroidInputStreamByteReader
import java.io.File
import java.io.InputStream

@Throws(ImageReadException::class)
fun Kim.readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
    Kim.readMetadata(AndroidInputStreamByteReader(inputStream, length))

@Throws(ImageReadException::class)
fun Kim.readMetadata(path: String): ImageMetadata? =
    Kim.readMetadata(File(path))

@Throws(ImageReadException::class)
fun Kim.readMetadata(file: File): ImageMetadata? {

    check(file.exists()) { "File does not exist: $file" }

    return Kim.readMetadata(file.inputStream(), file.length())
}
