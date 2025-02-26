/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.model

import com.ashampoo.kim.common.PhotoValueFormatter
import com.ashampoo.xmp.XMPRegionArea

/**
 * Represents a high-level summary of image metadata extracted from raw ImageMetadata.
 * This summary is used by Ashampoo Photo Organizer.
 */
public data class PhotoMetadata(

    val imageFormat: ImageFormat? = null,

    /* Image resolution */
    val widthPx: Int? = null,
    val heightPx: Int? = null,

    /** tiff:Orientation */
    val orientation: TiffOrientation? = null,

    /* Capture parameters */
    val takenDate: Long? = null,
    val gpsCoordinates: GpsCoordinates? = null,
    val locationShown: LocationShown? = null,
    val cameraMake: String? = null,
    val cameraModel: String? = null,
    val lensMake: String? = null,
    val lensModel: String? = null,
    val iso: Int? = null,
    val exposureTime: Double? = null,
    val fNumber: Double? = null,
    val focalLength: Double? = null,

    /* Title & Description */
    val title: String? = null,
    val description: String? = null,

    /* Ratings & Tags */
    val flagged: Boolean = false,
    val rating: PhotoRating? = null,
    val keywords: Set<String> = emptySet(),

    /* Persons */
    val faces: Map<String, XMPRegionArea> = emptyMap(),
    val personsInImage: Set<String> = emptySet(),

    /* Albums */
    val albums: Set<String> = emptySet(),

    /* EXIF Thumbnail (IFD1) */
    val thumbnailImageSize: ImageSize? = null,
    val thumbnailBytes: ByteArray? = null

) {

    val megaPixelCount: Int
        get() = if (widthPx == null || heightPx == null)
            0
        else
            (widthPx * heightPx).div(PhotoValueFormatter.MEGA_PIXEL_COUNT)

    val locationDisplay: String?
        get() = locationShown?.displayString
            ?: gpsCoordinates?.let { "GPS: " + gpsCoordinates.latLongString }

    val cameraName: String?
        get() = PhotoValueFormatter.createCameraOrLensName(cameraMake, cameraModel)

    val lensName: String?
        get() = PhotoValueFormatter.createModifiedLensName(
            cameraName = cameraName,
            lensName = PhotoValueFormatter.createCameraOrLensName(lensMake, lensModel)
        )

    val cameraAndLensName: String?
        get() = PhotoValueFormatter.createCameraAndLensName(
            cameraName = cameraName,
            lensName = lensName
        )

    val orientedSize: ImageSize?
        get() = when {
            widthPx == null || heightPx == null -> null
            orientation?.hasFlippedDimensions() == true -> ImageSize(heightPx, widthPx)
            else -> ImageSize(widthPx, heightPx)
        }

    @Suppress("DataClassContainsFunctions")
    public fun isEmpty(): Boolean =
        this == emptyPhotoMetadata

    /**
     * Combine the current metadata with the given one,
     * but only replace fields that are NULL.
     *
     * We read metadata in a certain order where XMP
     * is more important than EXIF, and so on.
     *
     * If the other metadata is NULL the same object is returned.
     * This API is provided to chain it.
     */
    public fun merge(other: PhotoMetadata?): PhotoMetadata {

        if (other == null)
            return this

        return this.copy(

            imageFormat = imageFormat ?: other.imageFormat,

            /* Image resolution */
            widthPx = widthPx ?: other.widthPx,
            heightPx = heightPx ?: other.heightPx,

            /** tiff:Orientation */
            orientation = orientation ?: other.orientation,

            /* Capture parameters */
            takenDate = takenDate ?: other.takenDate,
            gpsCoordinates = gpsCoordinates ?: other.gpsCoordinates,
            locationShown = locationShown ?: other.locationShown,
            cameraMake = cameraMake ?: other.cameraMake,
            cameraModel = cameraModel ?: other.cameraModel,
            lensMake = lensMake ?: other.lensMake,
            lensModel = lensModel ?: other.lensModel,
            iso = iso ?: other.iso,
            exposureTime = exposureTime ?: other.exposureTime,
            fNumber = fNumber ?: other.fNumber,
            focalLength = focalLength ?: other.focalLength,

            /* Title & Description */
            title = title ?: other.title,
            description = description ?: other.description,

            /* Ratings & Tags */
            flagged = flagged || other.flagged,
            rating = rating ?: other.rating,
            keywords = keywords.ifEmpty { other.keywords },

            /* Persons */
            faces = faces.ifEmpty { other.faces },
            personsInImage = personsInImage.ifEmpty { other.personsInImage },

            /* Albums */
            albums = albums.ifEmpty { other.albums },

            /* EXIF Thumbnail (IFD1) */
            thumbnailImageSize = thumbnailImageSize ?: other.thumbnailImageSize,
            thumbnailBytes = thumbnailBytes ?: other.thumbnailBytes
        )
    }

    public companion object {

        public val emptyPhotoMetadata: PhotoMetadata = PhotoMetadata()
    }
}
