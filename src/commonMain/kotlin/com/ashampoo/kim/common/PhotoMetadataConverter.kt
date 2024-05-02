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
package com.ashampoo.kim.common

import com.ashampoo.kim.Kim.underUnitTesting
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.jpeg.JpegImageParser
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
import com.ashampoo.kim.format.tiff.GPSInfo
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.format.xmp.XmpReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.PhotoMetadata
import com.ashampoo.kim.model.TiffOrientation
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/*
 * This is a dedicated object with @JvmStatic methods
 * to provide a better API to pure Java projects.
 */
object PhotoMetadataConverter {

    @JvmStatic
    @JvmOverloads
    @Suppress("LongMethod")
    fun convertToPhotoMetadata(
        imageMetadata: ImageMetadata,
        ignoreOrientation: Boolean = false
    ): PhotoMetadata {

        val orientation = if (ignoreOrientation)
            TiffOrientation.STANDARD
        else
            TiffOrientation.of(imageMetadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())

        val takenDateMillis = extractTakenDateMillis(imageMetadata)

        val gpsDirectory = imageMetadata.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)

        val gps = gpsDirectory?.let(GPSInfo::createFrom)

        val latitude = gps?.getLatitudeAsDegreesNorth()
        val longitude = gps?.getLongitudeAsDegreesEast()

        val cameraMake = imageMetadata.findStringValue(TiffTag.TIFF_TAG_MAKE)
        val cameraModel = imageMetadata.findStringValue(TiffTag.TIFF_TAG_MODEL)

        val lensMake = imageMetadata.findStringValue(ExifTag.EXIF_TAG_LENS_MAKE)
        val lensModel = imageMetadata.findStringValue(ExifTag.EXIF_TAG_LENS_MODEL)

        /* Look for ISO at the standard place and fall back to test RW2 logic. */
        val iso = imageMetadata.findShortValue(ExifTag.EXIF_TAG_ISO)
            ?: imageMetadata.findShortValue(ExifTag.EXIF_TAG_ISO_PANASONIC)

        val exposureTime = imageMetadata.findDoubleValue(ExifTag.EXIF_TAG_EXPOSURE_TIME)
        val fNumber = imageMetadata.findDoubleValue(ExifTag.EXIF_TAG_FNUMBER)
        val focalLength = imageMetadata.findDoubleValue(ExifTag.EXIF_TAG_FOCAL_LENGTH)

        val keywords = mutableSetOf<String>()

        val iptcRecords = imageMetadata.iptc?.records

        iptcRecords?.forEach {

            if (it.iptcType == IptcTypes.KEYWORDS)
                keywords.add(it.value)
        }

        val gpsCoordinates =
            if (latitude != null && longitude != null)
                GpsCoordinates(
                    latitude = latitude,
                    longitude = longitude
                )
            else
                null

        val xmpMetadata: PhotoMetadata? = imageMetadata.xmp?.let {
            XmpReader.readMetadata(it)
        }

        val thumbnailBytes = imageMetadata.getExifThumbnailBytes()

        val thumbnailImageSize = thumbnailBytes?.let {
            JpegImageParser.getImageSize(
                ByteArrayByteReader(thumbnailBytes)
            )
        }

        /*
         * Embedded XMP metadata has higher priority than EXIF or IPTC
         * for certain fields because it's the newer format. Some fields
         * like rating, faces and persons in image are exclusive to XMP.
         *
         * Resolution, orientation and capture parameters (camera make,
         * iso, exposure time, etc.) are always taken from EXIF.
         */
        return PhotoMetadata(
            imageFormat = imageMetadata.imageFormat,
            widthPx = imageMetadata.imageSize?.width,
            heightPx = imageMetadata.imageSize?.height,
            orientation = orientation,
            takenDate = xmpMetadata?.takenDate ?: takenDateMillis,
            gpsCoordinates = xmpMetadata?.gpsCoordinates ?: gpsCoordinates,
            location = xmpMetadata?.location,
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            lensMake = lensMake,
            lensModel = lensModel,
            iso = iso?.toInt(),
            exposureTime = exposureTime,
            fNumber = fNumber,
            focalLength = focalLength,
            flagged = xmpMetadata?.flagged ?: false,
            rating = xmpMetadata?.rating,
            keywords = keywords.ifEmpty { xmpMetadata?.keywords ?: emptySet() },
            faces = xmpMetadata?.faces ?: emptyMap(),
            personsInImage = xmpMetadata?.personsInImage ?: emptySet(),
            albums = xmpMetadata?.albums ?: emptySet(),
            thumbnailImageSize = thumbnailImageSize,
            thumbnailBytes = thumbnailBytes
        )
    }

    @JvmStatic
    fun extractTakenDateAsIsoString(metadata: ImageMetadata): String? {

        val takenDateField = metadata.findTiffField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            ?: return null

        var takenDate = takenDateField.value as? String

        /*
         * Workaround in case that it's a String array.
         */
        if (takenDate == null)
            takenDate = takenDateField.toStringValue()

        if (isExifDateEmpty(takenDate))
            return null

        return convertExifDateToIso8601Date(takenDate)
    }

    @JvmStatic
    fun extractTakenDateMillis(metadata: ImageMetadata): Long? {

        var takenDate: String? = null

        try {

            takenDate = extractTakenDateAsIsoString(metadata) ?: return null

            val takenDateSubSecond = metadata
                .findStringValue(ExifTag.EXIF_TAG_SUB_SEC_TIME_ORIGINAL)
                ?.toIntOrNull()
                ?: 0

            /*
             * If the date string itself contains a sub second like "2020-08-30T18:43:00.500"
             * this should be used. We append it, if the string does not have a dot yet.
             */
            val takenDatePlusSubSecond = if (!takenDate.contains('.'))
                "$takenDate.$takenDateSubSecond"
            else
                takenDate

            val timeZone = if (underUnitTesting)
                TimeZone.of("GMT+02:00")
            else
                TimeZone.currentSystemDefault()

            return LocalDateTime.parse(takenDatePlusSubSecond)
                .toInstant(timeZone)
                .toEpochMilliseconds()

        } catch (ignore: Exception) {

            /*
             * Many photos contain wrong values here. We ignore this problem and hope
             * that another taken date source like embedded XMP has a valid date instead.
             */
            println("Ignore invalid EXIF DateTimeOriginal: '$takenDate'")

            return null
        }
    }

}

fun ImageMetadata.convertToPhotoMetadata(
    ignoreOrientation: Boolean = false
): PhotoMetadata =
    PhotoMetadataConverter.convertToPhotoMetadata(
        imageMetadata = this,
        ignoreOrientation = ignoreOrientation
    )

