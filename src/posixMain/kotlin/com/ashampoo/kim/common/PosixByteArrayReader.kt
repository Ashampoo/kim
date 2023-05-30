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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import platform.posix.FILE
import platform.posix.SEEK_END
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.posix.perror
import platform.posix.rewind

@OptIn(UnsafeNumber::class)
fun readFileAsByteArray(filePath: String): ByteArray? = memScoped {

    /* Note: Mode "rb" is for reading binary files. */
    val file: CPointer<FILE>? = fopen(filePath, "rb")

    if (file == null) {
        perror("Failed to open file: $filePath")
        return null
    }

    /* Move the cursor to the end of the file. */
    fseek(file, 0, SEEK_END)
    val fileSize = ftell(file)
    rewind(file)

    val buffer = ByteArray(fileSize.toInt())

    val bytesReadCount: ULong = fread(
        buffer.refTo(0),
        1.toULong(), // Number of items
        fileSize.toULong(), // Size to read
        file
    )

    fclose(file)

    if (bytesReadCount != fileSize.toULong()) {
        perror("Did not read file completely: $bytesReadCount != $fileSize")
        return null
    }

    return buffer
}
