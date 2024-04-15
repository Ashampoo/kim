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

import com.ashampoo.kim.common.convertToPhotoMetadata
import com.ashampoo.kim.common.readFileAsByteArray
import platform.posix.perror

fun main(args: Array<String>) {

    if (args.size != 1) {
        println("USAGE: Must be called with one argument.")
        return
    }

    val filePath = args.first()

    println("--- Ashampoo Kim ---")

    println("File path   : $filePath")

    val bytes = readFileAsByteArray(filePath)

    if (bytes == null) {
        perror("File could not be read: $filePath")
        return
    }

    println("File length : ${bytes.size}")

    val metadata = Kim.readMetadata(bytes)

    if (metadata == null) {
        perror("File could not be parsed.")
        return
    }

    println(metadata)

    /*
     * Show what the parsing result looks like
     */

    println("--- Ashampoo Photo Organizer Metadata ---")

    val photoMetadata = metadata.convertToPhotoMetadata()

    println(photoMetadata)
}
