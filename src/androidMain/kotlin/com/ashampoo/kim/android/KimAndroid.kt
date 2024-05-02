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
package com.ashampoo.kim.android

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.AndroidInputStreamByteReader
import java.io.File
import java.io.InputStream

/**
 * Extra object to have a nicer API for Java projects
 */
public object KimAndroid {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
        Kim.readMetadata(AndroidInputStreamByteReader(inputStream, length))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): ImageMetadata? =
        readMetadata(File(path))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(file: File): ImageMetadata? {

        check(file.exists()) { "File does not exist: $file" }

        return readMetadata(file.inputStream().buffered(), file.length())
    }
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
    KimAndroid.readMetadata(inputStream, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: String): ImageMetadata? =
    KimAndroid.readMetadata(path)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(file: File): ImageMetadata? =
    KimAndroid.readMetadata(file)
