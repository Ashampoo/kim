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
import com.ashampoo.kim.model.LocationShown
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
public object PhotoMetadataConverter {

    @JvmStatic
    @JvmOverloads
    @Suppress("LongMethod")
    public fun convertToPhotoMetadata(
        imageMetadata: ImageMetadata,
        ignoreOrientation: Boolean = false
    ): PhotoMetadata {

        val xmpMetadata: PhotoMetadata? = imageMetadata.xmp?.let {
            XmpReader.readMetadata(it)
        }

        val orientation = if (ignoreOrientation)
            TiffOrientation.STANDARD
        else
            TiffOrientation.of(imageMetadata.findShortValue(TiffTag.TIFF_TAG_ORIENTATION)?.toInt())

        val takenDateMillis: Long? = xmpMetadata?.takenDate
            ?: extractTakenDateMillisFromExif(imageMetadata)

        val gpsCoordinates: GpsCoordinates? = xmpMetadata?.gpsCoordinates
            ?: extractGpsCoordinatesFromExif(imageMetadata)

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

        val keywords = xmpMetadata?.keywords?.ifEmpty {
            extractKeywordsFromIptc(imageMetadata)
        } ?: extractKeywordsFromIptc(imageMetadata)

        val iptcRecords = imageMetadata.iptc?.records

        val title = xmpMetadata?.title ?: iptcRecords
            ?.find { it.iptcType == IptcTypes.OBJECT_NAME }
            ?.value

        val description = xmpMetadata?.description ?: iptcRecords
            ?.find { it.iptcType == IptcTypes.CAPTION_ABSTRACT }
            ?.value

        val location = xmpMetadata?.locationShown
            ?: extractLocationFromIptc(imageMetadata)

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
            takenDate = takenDateMillis,
            gpsCoordinates = gpsCoordinates,
            locationShown = location,
            cameraMake = cameraMake,
            cameraModel = cameraModel,
            lensMake = lensMake,
            lensModel = lensModel,
            iso = iso?.toInt(),
            exposureTime = exposureTime,
            fNumber = fNumber,
            focalLength = focalLength,
            title = title,
            description = description,
            flagged = xmpMetadata?.flagged ?: false,
            rating = xmpMetadata?.rating,
            keywords = keywords,
            faces = xmpMetadata?.faces ?: emptyMap(),
            personsInImage = xmpMetadata?.personsInImage ?: emptySet(),
            albums = xmpMetadata?.albums ?: emptySet(),
            thumbnailImageSize = thumbnailImageSize,
            thumbnailBytes = thumbnailBytes
        )
    }

    @JvmStatic
    private fun extractTakenDateAsIsoString(metadata: ImageMetadata): String? {

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
    private fun extractTakenDateMillisFromExif(
        metadata: ImageMetadata
    ): Long? {

        try {

            val takenDate = extractTakenDateAsIsoString(metadata) ?: return null

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

            return null
        }
    }

    @JvmStatic
    private fun extractGpsCoordinatesFromExif(
        metadata: ImageMetadata
    ): GpsCoordinates? {

        val gpsDirectory = metadata.findTiffDirectory(TiffConstants.TIFF_DIRECTORY_GPS)

        val gps = gpsDirectory?.let(GPSInfo::createFrom)

        val latitude = gps?.getLatitudeAsDegreesNorth()
        val longitude = gps?.getLongitudeAsDegreesEast()

        if (latitude == null || longitude == null)
            return null

        return GpsCoordinates(
            latitude = latitude,
            longitude = longitude
        )
    }

    @JvmStatic
    private fun extractKeywordsFromIptc(
        metadata: ImageMetadata
    ): Set<String> {

        return metadata.iptc?.records
            ?.filter { it.iptcType == IptcTypes.KEYWORDS }
            ?.map { it.value }
            ?.toSet()
            ?: emptySet()
    }

    @JvmStatic
    private fun extractLocationFromIptc(
        metadata: ImageMetadata
    ): LocationShown? {

        val iptcRecords = metadata.iptc?.records
            ?: return null

        val iptcCity = iptcRecords
            .find { it.iptcType == IptcTypes.CITY }
            ?.value

        val iptcState = iptcRecords
            .find { it.iptcType == IptcTypes.PROVINCE_STATE }
            ?.value

        val iptcCountry = iptcRecords
            .find { it.iptcType == IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME }
            ?.value

        /* Don't create an object if everything is NULL */
        if (iptcCity.isNullOrBlank() && iptcState.isNullOrBlank() && iptcCountry.isNullOrBlank())
            return null

        return LocationShown(
            name = null,
            location = null,
            city = iptcCity,
            state = iptcState,
            country = iptcCountry
        )
    }
}

public fun ImageMetadata.convertToPhotoMetadata(
    ignoreOrientation: Boolean = false
): PhotoMetadata =
    PhotoMetadataConverter.convertToPhotoMetadata(
        imageMetadata = this,
        ignoreOrientation = ignoreOrientation
    )

