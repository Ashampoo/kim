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
import com.ashampoo.kim.common.convertToPhotoMetadata
import kotlinx.datetime.Instant
import kotlinx.datetime.toJSDate
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

/**
 * Extra object to have a nicer API for JavaScript projects
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("Kim")
public object KimJs {

    public fun readMetadata(uint8Array: Uint8Array): JsImageMetadata? {

        val byteArray = uint8Array.toByteArray()

        val imageMetadata = Kim.readMetadata(byteArray) ?: return null

        val photoMetadata = imageMetadata.convertToPhotoMetadata()

        val takenDateJsDate = photoMetadata.takenDate?.let {
            Instant.fromEpochMilliseconds(it).toJSDate()
        }

        val faces = photoMetadata.faces.map {
            Face(
                name = it.key,
                xPos = it.value.xPos,
                yPos = it.value.yPos,
                width = it.value.width,
                height = it.value.height
            )
        }.toTypedArray()

        return JsImageMetadata(
            mimeType = photoMetadata.imageFormat?.mimeType,
            widthPx = photoMetadata.widthPx,
            heightPx = photoMetadata.heightPx,
            orientation = photoMetadata.orientation?.value,
            takenDate = takenDateJsDate,
            gpsLatitude = photoMetadata.gpsCoordinates?.latitude,
            gpsLongitude = photoMetadata.gpsCoordinates?.longitude,
            cameraMake = photoMetadata.cameraMake,
            cameraModel = photoMetadata.cameraModel,
            lensMake = photoMetadata.lensMake,
            lensModel = photoMetadata.lensModel,
            iso = photoMetadata.iso,
            exposureTime = photoMetadata.exposureTime,
            fNumber = photoMetadata.fNumber,
            focalLength = photoMetadata.focalLength,
            flagged = photoMetadata.flagged,
            rating = photoMetadata.rating?.value,
            keywords = photoMetadata.keywords.toTypedArray(),
            faces = faces,
            personsInImage = photoMetadata.personsInImage.toTypedArray(),
            albums = photoMetadata.albums.toTypedArray()
        )
    }
}

private fun Uint8Array.toByteArray(): ByteArray =
    ByteArray(length) { this[it] }
