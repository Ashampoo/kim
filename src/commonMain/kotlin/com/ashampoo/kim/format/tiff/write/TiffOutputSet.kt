/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.GpsUtil.MINUTES_PER_HOUR
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.RationalNumber.Companion.valueOf
import com.ashampoo.kim.common.RationalNumbers
import com.ashampoo.kim.common.toExifDateString
import com.ashampoo.kim.format.tiff.JpegImageDataElement
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.GpsTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffConstants.DEFAULT_TIFF_BYTE_ORDER
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.MetadataUpdate
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs

@Suppress("TooManyFunctions")
class TiffOutputSet(
    val byteOrder: ByteOrder = DEFAULT_TIFF_BYTE_ORDER
) {

    private val directories = mutableListOf<TiffOutputDirectory>()

    fun getOutputItems(outputSummary: TiffOffsetItems): List<TiffOutputItem> {

        val outputItems = mutableListOf<TiffOutputItem>()

        for (directory in directories)
            outputItems.addAll(directory.getOutputItems(outputSummary))

        return outputItems
    }

    fun addDirectory(directory: TiffOutputDirectory): TiffOutputDirectory {

        if (findDirectory(directory.type) != null)
            throw ImageWriteException("Output set already contains a directory of that type.")

        directories.add(directory)

        return directory
    }

    fun getDirectories(): List<TiffOutputDirectory> = directories

    fun getOrCreateRootDirectory(): TiffOutputDirectory =
        findDirectory(TiffConstants.DIRECTORY_TYPE_ROOT) ?: addRootDirectory()

    fun getOrCreateExifDirectory(): TiffOutputDirectory {

        /* The EXIF directory requires root directory. */
        getOrCreateRootDirectory()

        return findDirectory(TiffConstants.TIFF_EXIF_IFD) ?: addExifDirectory()
    }

    fun getOrCreateThumbnailDirectory(): TiffOutputDirectory {

        /* The Thumbnail directory requires root directory. */
        getOrCreateRootDirectory()

        return findDirectory(TiffConstants.TIFF_IFD1) ?: addThumbnailDirectory()
    }

    fun getOrCreateGPSDirectory(): TiffOutputDirectory {

        /* The GPS directory requires EXIF directory */
        getOrCreateExifDirectory()

        return findDirectory(TiffConstants.TIFF_GPS) ?: addGPSDirectory()
    }

    fun findDirectory(directoryType: Int): TiffOutputDirectory? =
        directories.find { it.type == directoryType }

    fun applyUpdate(update: MetadataUpdate) {

        val rootDirectory = getOrCreateRootDirectory()
        val exifDirectory = getOrCreateExifDirectory()

        when (update) {

            is MetadataUpdate.Orientation -> {

                rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
                rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, update.tiffOrientation.value.toShort())
            }

            is MetadataUpdate.TakenDate -> {

                exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
                exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED)

                if (update.takenDate != null) {

                    val timeZone = if (Kim.underUnitTesting)
                        TimeZone.of("GMT+02:00")
                    else
                        TimeZone.currentSystemDefault()

                    val exifDateString = Instant.fromEpochMilliseconds(update.takenDate)
                        .toLocalDateTime(timeZone)
                        .toExifDateString()

                    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, exifDateString)
                    exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED, exifDateString)
                }
            }

            is MetadataUpdate.GpsCoordinates -> {

                setGpsCoordinates(update.gpsCoordinates)
            }

            else -> throw ImageWriteException("Can't perform update $update.")
        }
    }

    /**
     * Sets the provided thumbnail bytes to the thumbnail directory (IFD1)
     */
    fun setThumbnailBytes(thumbnailBytes: ByteArray) {

        val thumbnailDirectory = getOrCreateThumbnailDirectory()

        thumbnailDirectory.setJpegImageData(
            JpegImageDataElement(
                /* Offset will be calculated, but the block should come early in the file. */
                offset = -1,
                length = thumbnailBytes.size,
                bytes = thumbnailBytes
            )
        )
    }

    /**
     * A convenience method to update GPS values in EXIF metadata.
     */
    fun setGpsCoordinates(gpsCoordinates: GpsCoordinates?) {

        val gpsDirectory = getOrCreateGPSDirectory()

        /* First delete everything. */
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_VERSION_ID)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LATITUDE_REF)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LONGITUDE)
        gpsDirectory.removeField(GpsTag.GPS_TAG_GPS_LATITUDE)

        if (gpsCoordinates == null)
            return

        /* Add the data back. */

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_VERSION_ID, GpsTag.GPS_VERSION)

        val longitudeRef = if (gpsCoordinates.longitude < 0) "W" else "E"
        val latitudeRef = if (gpsCoordinates.latitude < 0) "S" else "N"

        gpsDirectory.add(GpsTag.GPS_TAG_GPS_LONGITUDE_REF, longitudeRef)
        gpsDirectory.add(GpsTag.GPS_TAG_GPS_LATITUDE_REF, latitudeRef)

        run {

            var value = abs(gpsCoordinates.longitude)

            val longitudeDegrees = value.toLong().toDouble()
            value %= 1.0
            value *= MINUTES_PER_HOUR

            val longitudeMinutes = value.toLong().toDouble()
            value %= 1.0
            value *= MINUTES_PER_HOUR

            val longitudeSeconds = value

            gpsDirectory.add(
                GpsTag.GPS_TAG_GPS_LONGITUDE,
                RationalNumbers(
                    arrayOf(
                        valueOf(longitudeDegrees),
                        valueOf(longitudeMinutes),
                        valueOf(longitudeSeconds)
                    )
                )
            )
        }

        run {

            var value = abs(gpsCoordinates.latitude)

            val latitudeDegrees = value.toLong().toDouble()

            value %= 1.0
            value *= MINUTES_PER_HOUR

            val latitudeMinutes = value.toLong().toDouble()

            value %= 1.0
            value *= MINUTES_PER_HOUR

            val latitudeSeconds = value

            gpsDirectory.add(
                GpsTag.GPS_TAG_GPS_LATITUDE,
                RationalNumbers(
                    arrayOf(
                        valueOf(latitudeDegrees),
                        valueOf(latitudeMinutes),
                        valueOf(latitudeSeconds)
                    )
                )
            )
        }
    }

    fun findField(tag: Int): TiffOutputField? =
        directories
            .mapNotNull { directory -> directory.findField(tag) }
            .firstOrNull()

    fun addRootDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.DIRECTORY_TYPE_ROOT, byteOrder))

    fun addExifDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_EXIF_IFD, byteOrder))

    fun addThumbnailDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_IFD1, byteOrder))

    fun addGPSDirectory(): TiffOutputDirectory =
        addDirectory(TiffOutputDirectory(TiffConstants.TIFF_GPS, byteOrder))
}
