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

import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoGpsText
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShort

@Suppress("MagicNumber")
public object GpsTag {

    public val GPS_TAG_GPS_VERSION_ID: TagInfoBytes = TagInfoBytes(
        0x0000, "GPSVersionID", 4,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_VERSION: ByteArray =
        byteArrayOf(2.toByte(), 3.toByte(), 0.toByte(), 0.toByte())

    public val GPS_TAG_GPS_LATITUDE_REF: TagInfoAscii = TagInfoAscii(
        0x0001, "GPSLatitudeRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_LATITUDE_REF_VALUE_NORTH = "N"
    public const val GPS_TAG_GPS_LATITUDE_REF_VALUE_SOUTH = "S"

    public val GPS_TAG_GPS_LATITUDE: TagInfoRationals = TagInfoRationals(
        0x0002, "GPSLatitude", 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_LONGITUDE_REF: TagInfoAscii = TagInfoAscii(
        0x0003, "GPSLongitudeRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_LONGITUDE_REF_VALUE_EAST: String = "E"
    public const val GPS_TAG_GPS_LONGITUDE_REF_VALUE_WEST: String = "W"

    public val GPS_TAG_GPS_LONGITUDE: TagInfoRationals = TagInfoRationals(
        0x0004, "GPSLongitude", 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_ALTITUDE_REF: TagInfoByte = TagInfoByte(
        0x0005, "GPSAltitudeRef",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_ALTITUDE_REF_VALUE_ABOVE_SEA_LEVEL = 0
    public const val GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL = 1

    public val GPS_TAG_GPS_ALTITUDE: TagInfoRational = TagInfoRational(
        0x0006, "GPSAltitude",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_TIME_STAMP: TagInfoRationals = TagInfoRationals(
        0x0007, "GPSTimeStamp", 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_SATELLITES: TagInfoAscii = TagInfoAscii(
        0x0008, "GPSSatellites", -1,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_STATUS: TagInfoAscii = TagInfoAscii(
        0x0009, "GPSStatus", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_STATUS_VALUE_MEASUREMENT_IN_PROGRESS = "A"
    public const val GPS_TAG_GPS_STATUS_VALUE_MEASUREMENT_INTEROPERABILITY = "V"

    public val GPS_TAG_GPS_MEASURE_MODE: TagInfoAscii = TagInfoAscii(
        0x000a, "GPSMeasureMode", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_MEASURE_MODE_VALUE_2_DIMENSIONAL_MEASUREMENT = 2
    public const val GPS_TAG_GPS_MEASURE_MODE_VALUE_3_DIMENSIONAL_MEASUREMENT = 3

    public val GPS_TAG_GPS_DOP: TagInfoRational = TagInfoRational(
        0x000b, "GPSDOP",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_SPEED_REF: TagInfoAscii = TagInfoAscii(
        0x000c, "GPSSpeedRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_SPEED_REF_VALUE_KMPH = "K"
    public const val GPS_TAG_GPS_SPEED_REF_VALUE_MPH = "M"
    public const val GPS_TAG_GPS_SPEED_REF_VALUE_KNOTS = "N"

    public val GPS_TAG_GPS_SPEED: TagInfoRational = TagInfoRational(
        0x000d, "GPSSpeed",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_TRACK_REF: TagInfoAscii = TagInfoAscii(
        0x000e, "GPSTrackRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_TRACK_REF_VALUE_MAGNETIC_NORTH = "M"
    public const val GPS_TAG_GPS_TRACK_REF_VALUE_TRUE_NORTH = "T"

    public val GPS_TAG_GPS_TRACK: TagInfoRational = TagInfoRational(
        0x000f, "GPSTrack",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_IMG_DIRECTION_REF: TagInfoAscii = TagInfoAscii(
        0x0010, "GPSImgDirectionRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_MAGNETIC_NORTH = "M"
    public const val GPS_TAG_GPS_IMG_DIRECTION_REF_VALUE_TRUE_NORTH = "T"

    public val GPS_TAG_GPS_IMG_DIRECTION: TagInfoRational = TagInfoRational(
        0x0011, "GPSImgDirection",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_MAP_DATUM: TagInfoAscii = TagInfoAscii(
        0x0012, "GPSMapDatum", -1,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DEST_LATITUDE_REF: TagInfoAscii = TagInfoAscii(
        0x0013, "GPSDestLatitudeRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_DEST_LATITUDE_REF_VALUE_NORTH = "N"
    public const val GPS_TAG_GPS_DEST_LATITUDE_REF_VALUE_SOUTH = "S"

    public val GPS_TAG_GPS_DEST_LATITUDE: TagInfoRationals = TagInfoRationals(
        0x0014, "GPSDestLatitude", 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DEST_LONGITUDE_REF: TagInfoAscii = TagInfoAscii(
        0x0015, "GPSDestLongitudeRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_DEST_LONGITUDE_REF_VALUE_EAST = "E"
    public const val GPS_TAG_GPS_DEST_LONGITUDE_REF_VALUE_WEST = "W"

    public val GPS_TAG_GPS_DEST_LONGITUDE: TagInfoRationals = TagInfoRationals(
        0x0016, "GPSDestLongitude", 3,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DEST_BEARING_REF: TagInfoAscii = TagInfoAscii(
        0x0017, "GPSDestBearingRef", 2,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_DEST_BEARING_REF_VALUE_MAGNETIC_NORTH = "M"
    public const val GPS_TAG_GPS_DEST_BEARING_REF_VALUE_TRUE_NORTH = "T"

    public val GPS_TAG_GPS_DEST_BEARING: TagInfoRational = TagInfoRational(
        0x0018, "GPSDestBearing",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DEST_DISTANCE: TagInfoRational = TagInfoRational(
        0x001a, "GPSDestDistance",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_PROCESSING_METHOD: TagInfoGpsText = TagInfoGpsText(
        0x001b, "GPSProcessingMethod",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_AREA_INFORMATION: TagInfoGpsText = TagInfoGpsText(
        0x001c, "GPSAreaInformation",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DATE_STAMP: TagInfoAscii = TagInfoAscii(
        0x001d, "GPSDateStamp", 11,
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public val GPS_TAG_GPS_DIFFERENTIAL: TagInfoShort = TagInfoShort(
        0x001e, "GPSDifferential",
        TiffDirectoryType.EXIF_DIRECTORY_GPS
    )

    public const val GPS_TAG_GPS_DIFFERENTIAL_VALUE_NO_CORRECTION = 0
    public const val GPS_TAG_GPS_DIFFERENTIAL_VALUE_DIFFERENTIAL_CORRECTED = 1

    public val ALL: List<TagInfo> = listOf(
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
