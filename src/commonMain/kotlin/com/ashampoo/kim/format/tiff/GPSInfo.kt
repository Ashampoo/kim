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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.common.GpsUtil.MINUTES_PER_HOUR
import com.ashampoo.kim.common.GpsUtil.SECONDS_PER_HOUR
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.RationalNumbers
import com.ashampoo.kim.format.tiff.constant.GpsTag

internal data class GPSInfo private constructor(
    private val latitudeRef: String,
    private val longitudeRef: String,
    private val latitudeDegrees: Double,
    private val latitudeMinutes: Double,
    private val latitudeSeconds: Double,
    private val longitudeDegrees: Double,
    private val longitudeMinutes: Double,
    private val longitudeSeconds: Double
) {

    fun getLongitudeAsDegreesEast(): Double {

        val result =
            longitudeDegrees + longitudeMinutes / MINUTES_PER_HOUR + longitudeSeconds / SECONDS_PER_HOUR

        if (longitudeRef.trim().equals("e", ignoreCase = true))
            return result

        if (longitudeRef.trim().equals("w", ignoreCase = true))
            return -result

        throw ImageReadException("Unknown longitude ref: \"$longitudeRef\"")
    }

    fun getLatitudeAsDegreesNorth(): Double {

        val result =
            latitudeDegrees + latitudeMinutes / MINUTES_PER_HOUR + latitudeSeconds / SECONDS_PER_HOUR

        if (latitudeRef.trim().equals("n", ignoreCase = true))
            return result

        if (latitudeRef.trim().equals("s", ignoreCase = true))
            return -result

        throw ImageReadException("Unknown latitude ref: \"$latitudeRef\"")
    }

    companion object {

        fun createFrom(gpsDirectory: TiffDirectory): GPSInfo? {

            val latitudeRef = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LATITUDE_REF)?.toStringValue()
                ?: return null

            val longitudeRef = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LONGITUDE_REF)?.toStringValue()
                ?: return null

            /*
             * The popular Android App "Aves Gallery" nullifies all GPS fields on export.
             */
            if (latitudeRef == "" || longitudeRef == "")
                return null

            val latitudeField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LATITUDE)
                ?: return null

            val longitudeField = gpsDirectory.findField(GpsTag.GPS_TAG_GPS_LONGITUDE)
                ?: return null

            // all of these values are strings.
            val latitude = latitudeField.value as RationalNumbers
            val longitude = longitudeField.value as RationalNumbers

            if (latitude.values.size != 3 || longitude.values.size != 3)
                throw ImageReadException("Expected three values for latitude and longitude.")

            return GPSInfo(
                latitudeRef = latitudeRef,
                longitudeRef = longitudeRef,
                latitudeDegrees = latitude.values[0].doubleValue(),
                latitudeMinutes = latitude.values[1].doubleValue(),
                latitudeSeconds = latitude.values[2].doubleValue(),
                longitudeDegrees = longitude.values[0].doubleValue(),
                longitudeMinutes = longitude.values[1].doubleValue(),
                longitudeSeconds = longitude.values[2].doubleValue()
            )
        }
    }
}
