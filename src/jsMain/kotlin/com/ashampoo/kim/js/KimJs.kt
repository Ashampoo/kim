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
package com.ashampoo.kim.js

import com.ashampoo.kim.Kim
import com.ashampoo.kim.format.ImageMetadata
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.w3c.files.File
import org.w3c.files.FileReader

public const val UNKNOWN_IMAGE_MIME_TYPE: String = "application/octet-stream"

/**
 * Extra object to have a nicer API for JavaScript projects
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("Kim")
public object KimJs {

    public fun readMetadataFromFile(
        file: File,
        onRead: (JsImageMetadata?) -> Unit
    ) {

        val fileReader = FileReader()

        fileReader.onload = { event ->

            val target = event.target as? FileReader

            if (target != null) {

                val arrayBuffer = target.result as? ArrayBuffer

                if (arrayBuffer != null) {

                    val uInt8Bytes = Uint8Array(arrayBuffer)

                    val metadata = readMetadataFromByteArray(uInt8Bytes)

                    onRead(metadata)

                } else {
                    onRead(null)
                }

            } else {
                onRead(null)
            }
        }

        fileReader.readAsArrayBuffer(file)
    }

    public fun readMetadataFromByteArray(uint8Array: Uint8Array): JsImageMetadata? =
        convertImageMetadata(Kim.readMetadata(uint8Array.toByteArray()))
}

private fun Uint8Array.toByteArray(): ByteArray =
    ByteArray(length) { this[it] }

private fun convertImageMetadata(
    imageMetadata: ImageMetadata?
): JsImageMetadata? {

    if (imageMetadata == null)
        return null

    return JsImageMetadata(
        mimeType = imageMetadata.imageFormat.mimeType,
        imageWidth = imageMetadata.imageSize?.width ?: 0,
        imageHeight = imageMetadata.imageSize?.height ?: 0,
        xmp = imageMetadata.xmp ?: ""
    )
}
