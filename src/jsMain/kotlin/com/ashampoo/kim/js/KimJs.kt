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
import com.ashampoo.kim.format.xmp.XmpReader
import com.ashampoo.kim.model.PhotoMetadata
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

        return photoMetadata.convertToJsImageMetadata()
    }

    public fun readXmp(xmp: String): JsImageMetadata =
        XmpReader.readMetadata(xmp).convertToJsImageMetadata()
}

private fun Uint8Array.toByteArray(): ByteArray =
    ByteArray(length) { this[it] }

private fun PhotoMetadata.convertToJsImageMetadata(): JsImageMetadata {

    val takenDateJsDate = this.takenDate?.let {
        Instant.fromEpochMilliseconds(it).toJSDate()
    }

    val faces = this.faces.map {
        Face(
            name = it.key,
            xPos = it.value.xPos,
            yPos = it.value.yPos,
            width = it.value.width,
            height = it.value.height
        )
    }.toTypedArray()

    return JsImageMetadata(
        mimeType = imageFormat?.mimeType,
        widthPx = widthPx,
        heightPx = heightPx,
        orientation = orientation?.value,
        takenDate = takenDateJsDate,
        gpsLatitude = gpsCoordinates?.latitude,
        gpsLongitude = gpsCoordinates?.longitude,
        cameraMake = cameraMake,
        cameraModel = cameraModel,
        lensMake = lensMake,
        lensModel = lensModel,
        iso = iso,
        exposureTime = exposureTime,
        fNumber = fNumber,
        focalLength = focalLength,
        flagged = flagged,
        rating = rating?.value,
        keywords = keywords.toTypedArray(),
        faces = faces,
        personsInImage = personsInImage.toTypedArray(),
        albums = albums.toTypedArray()
    )
}
