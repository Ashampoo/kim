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
package com.ashampoo.kim.format.tiff.constant

import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoGpsText
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShort

@Suppress("MagicNumber")
object GpsTag {

    val GPS_TAG_GPS_VERSION_ID = TagInfoBytes(
        "GPSVersionID", 0x0000, 4,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_VERSION = byteArrayOf(2.toByte(), 3.toByte(), 0.toByte(), 0.toByte())

    val GPS_TAG_GPS_LATITUDE_REF = TagInfoAscii(
        "GPSLatitudeRef", 0x0001, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_LATITUDE_REF_VALUE_NORTH = "N"
    const val GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH = "S"

    val GPS_TAG_GPS_LATITUDE = TagInfoRationals(
        "GPSLatitude", 0x0002, 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_LONGITUDE_REF = TagInfoAscii(
        "GPSLongitudeRef", 0x0003, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_LONGITUDE_REF_VALUE_EAST = "E"
    const val GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST = "W"

    val GPS_TAG_GPS_LONGITUDE = TagInfoRationals(
        "GPSLongitude", 0x0004, 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_ALTITUDE_REF = TagInfoByte(
        "GPSAltitudeRef", 0x0005,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_ALTITUDE_REF_VALUE_ABOVE_SEA_LEVEL = 0
    const val GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL = 1

    val GPS_TAG_GPS_ALTITUDE = TagInfoRational(
        "GPSAltitude", 0x0006,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_TIME_STAMP = TagInfoRationals(
        "GPSTimeStamp", 0x0007, 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_SATELLITES = TagInfoAscii(
        "GPSSatellites", 0x0008, -1,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_STATUS = TagInfoAscii(
        "GPSStatus", 0x0009, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_STATUS_VALUE_MEASUREMENT_IN_PROGRESS = "A"
    const val GPS_TAG_GPS_STATUS_VALUE_MEASUREMENT_INTEROPERABILITY = "V"

    val GPS_TAG_GPS_MEASURE_MODE = TagInfoAscii(
        "GPSMeasureMode", 0x000a, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_MEASURE_MODE_VALUE_2_DIMENSIONAL_MEASUREMENT = 2
    const val GPS_TAG_GPS_MEASURE_MODE_VALUE_3_DIMENSIONAL_MEASUREMENT = 3

    val GPS_TAG_GPS_DOP = TagInfoRational(
        "GPSDOP", 0x000b,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_SPEED_REF = TagInfoAscii(
        "GPSSpeedRef", 0x000c, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_SPEED_REF_VALUE_KMPH = "K"
    const val GPS_TAG_GPS_SPEED_REF_VALUE_MPH = "M"
    const val GPS_TAG_GPS_SPEED_REF_VALUE_KNOTS = "N"

    val GPS_TAG_GPS_SPEED = TagInfoRational(
        "GPSSpeed", 0x000d,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_TRACK_REF = TagInfoAscii(
        "GPSTrackRef", 0x000e, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_TRACK_REF_VALUE_MAGNETIC_NORTH = "M"
    const val GPS_TAG_GPS_TRACK_REF_VALUE_TRUE_NORTH = "T"

    val GPS_TAG_GPS_TRACK = TagInfoRational(
        "GPSTrack", 0x000f,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_IMG_DIRECTION_REF = TagInfoAscii(
        "GPSImgDirectionRef", 0x0010, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_MAGNETIC_NORTH = "M"
    const val GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH = "T"

    val GPS_TAG_GPS_IMG_DIRECTION = TagInfoRational(
        "GPSImgDirection", 0x0011,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_MAP_DATUM = TagInfoAscii(
        "GPSMapDatum", 0x0012, -1,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DEST_LATITUDE_REF = TagInfoAscii(
        "GPSDestLatitudeRef", 0x0013, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_DEST_LATITUDE_REF_VALUE_NORTH = "N"
    const val GPS_TAG_GPS_DEST_LATITUDE_REF_VALUE_SOUTH = "S"

    val GPS_TAG_GPS_DEST_LATITUDE = TagInfoRationals(
        "GPSDestLatitude", 0x0014, 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DEST_LONGITUDE_REF = TagInfoAscii(
        "GPSDestLongitudeRef", 0x0015, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_DEST_LONGITUDE_REF_VALUE_EAST = "E"
    const val GPS_TAG_GPS_DEST_LONGITUDE_REF_VALUE_WEST = "W"

    val GPS_TAG_GPS_DEST_LONGITUDE = TagInfoRationals(
        "GPSDestLongitude", 0x0016, 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DEST_BEARING_REF = TagInfoAscii(
        "GPSDestBearingRef", 0x0017, 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_DEST_BEARING_REF_VALUE_MAGNETIC_NORTH = "M"
    const val GPS_TAG_GPS_DEST_BEARING_REF_VALUE_TRUE_NORTH = "T"

    val GPS_TAG_GPS_DEST_BEARING = TagInfoRational(
        "GPSDestBearing", 0x0018,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DEST_DISTANCE = TagInfoRational(
        "GPSDestDistance", 0x001a,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_PROCESSING_METHOD = TagInfoGpsText(
        "GPSProcessingMethod", 0x001b,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_AREA_INFORMATION = TagInfoGpsText(
        "GPSAreaInformation", 0x001c,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DATE_STAMP = TagInfoAscii(
        "GPSDateStamp", 0x001d, 11,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    val GPS_TAG_GPS_DIFFERENTIAL = TagInfoShort(
        "GPSDifferential", 0x001e,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    const val GPS_TAG_GPS_DIFFERENTIAL_VALUE_NO_CORRECTION = 0
    const val GPS_TAG_GPS_DIFFERENTIAL_VALUE_DIFFERENTIAL_CORRECTED = 1

    val ALL_GPS_TAGS = listOf(
        GPS_TAG_GPS_VERSION_ID, GPS_TAG_GPS_LATITUDE_REF,
        GPS_TAG_GPS_LATITUDE, GPS_TAG_GPS_LONGITUDE_REF,
        GPS_TAG_GPS_LONGITUDE, GPS_TAG_GPS_ALTITUDE_REF,
        GPS_TAG_GPS_ALTITUDE, GPS_TAG_GPS_TIME_STAMP,
        GPS_TAG_GPS_SATELLITES, GPS_TAG_GPS_STATUS,
        GPS_TAG_GPS_MEASURE_MODE, GPS_TAG_GPS_DOP, GPS_TAG_GPS_SPEED_REF,
        GPS_TAG_GPS_SPEED, GPS_TAG_GPS_TRACK_REF, GPS_TAG_GPS_TRACK,
        GPS_TAG_GPS_IMG_DIRECTION_REF, GPS_TAG_GPS_IMG_DIRECTION,
        GPS_TAG_GPS_MAP_DATUM, GPS_TAG_GPS_DEST_LATITUDE_REF,
        GPS_TAG_GPS_DEST_LATITUDE, GPS_TAG_GPS_DEST_LONGITUDE_REF,
        GPS_TAG_GPS_DEST_LONGITUDE, GPS_TAG_GPS_DEST_BEARING_REF,
        GPS_TAG_GPS_DEST_BEARING,
        GPS_TAG_GPS_DEST_DISTANCE, GPS_TAG_GPS_PROCESSING_METHOD,
        GPS_TAG_GPS_AREA_INFORMATION, GPS_TAG_GPS_DATE_STAMP,
        GPS_TAG_GPS_DIFFERENTIAL
    )
}
