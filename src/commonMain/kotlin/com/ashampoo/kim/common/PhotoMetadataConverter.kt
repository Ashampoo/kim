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

fun ImageMetadata.convertToPhotoMetadata(
    includeThumbnail: Boolean = false,
    ignoreOrientation: Boolean = false
): PhotoMetadata {

    val orientation = if (ignoreOrientation)
        TiffOrientation.STANDARD
    else
        TiffOrientation.of(findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())

    val takenDateMillis = extractTakenDateMillis(this)

    val gpsDirectory = findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)

    val gps = gpsDirectory?.let { GPSInfo.createFrom(it) }

    val latitude = gps?.getLatitudeAsDegreesNorth()
    val longitude = gps?.getLongitudeAsDegreesEast()

    val cameraMake = findStringValue(TiffTag.TIFF_TAG_MAKE)
    val cameraModel = findStringValue(TiffTag.TIFF_TAG_MODEL)

    val lensMake = findStringValue(ExifTag.EXIF_TAG_LENS_MAKE)
    val lensModel = findStringValue(ExifTag.EXIF_TAG_LENS_MODEL)

    /* Look for ISO at the standard place and fall back to test RW2 logic. */
    val iso = findShortValue(ExifTag.EXIF_TAG_ISO)
        ?: findShortValue(ExifTag.EXIF_TAG_ISO_PANASONIC)

    val exposureTime = findDoubleValue(ExifTag.EXIF_TAG_EXPOSURE_TIME)
    val fNumber = findDoubleValue(ExifTag.EXIF_TAG_FNUMBER)
    val focalLength = findDoubleValue(ExifTag.EXIF_TAG_FOCAL_LENGTH)

    val keywords = mutableSetOf<String>()

    val iptcRecords = iptc?.records

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

    val xmpMetadata: PhotoMetadata? = xmp?.let {
        XmpReader.readMetadata(it)
    }

    val thumbnailBytes = if (includeThumbnail)
        getExifThumbnailBytes()
    else
        null

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
        widthPx = imageSize?.width,
        heightPx = imageSize?.height,
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
        flagged = xmpMetadata?.flagged,
        rating = xmpMetadata?.rating,
        keywords = keywords.ifEmpty { xmpMetadata?.keywords ?: emptySet() },
        faces = xmpMetadata?.faces ?: emptyMap(),
        personsInImage = xmpMetadata?.personsInImage ?: emptySet(),
        albums = xmpMetadata?.albums ?: emptySet(),
        thumbnailImageSize = thumbnailImageSize,
        thumbnailBytes = thumbnailBytes
    )
}

private fun extractTakenDateAsIso(metadata: ImageMetadata): String? {

    val takenDateField = metadata.findTiffField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)

    var takenDate = takenDateField?.value as? String

    /*
     * photo_53.jpg of our test data triggers a bug here.
     * This is workaround code.
     */
    if (takenDate == null && takenDateField != null)
        takenDate = takenDateField.toStringValue()

    if (takenDate == null || isExifDateEmpty(takenDate))
        return null

    return convertExifDateToIso8601Date(takenDate)
}

private fun extractTakenDateMillis(metadata: ImageMetadata): Long? {

    val exif = metadata.exif

    if (exif == null)
        return exif

    var takenDate: String? = null

    try {

        takenDate = extractTakenDateAsIso(metadata) ?: return null

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
