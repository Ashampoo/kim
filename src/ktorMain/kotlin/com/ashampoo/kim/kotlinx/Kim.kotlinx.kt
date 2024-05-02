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
package com.ashampoo.kim.kotlinx

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.KotlinIoSourceByteReader
import kotlinx.io.files.Path
import kotlin.jvm.JvmStatic

/**
 * Extra object to have a nicer API for Java projects
 */
public object KimKotlinx {

    @JvmStatic
    @OptIn(ExperimentalStdlibApi::class)
    @Throws(ImageReadException::class)
    public fun readMetadata(path: Path): ImageMetadata? = tryWithImageReadException {

        KotlinIoSourceByteReader.read(path) { byteReader ->
            byteReader?.let { Kim.readMetadata(it) }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: Path): ImageMetadata? =
    KimKotlinx.readMetadata(path)
