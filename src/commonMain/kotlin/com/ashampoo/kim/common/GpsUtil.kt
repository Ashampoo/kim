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

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

public object GpsUtil {

    internal const val MINUTES_PER_HOUR: Double = 60.0
    internal const val SECONDS_PER_HOUR: Double = 3600.0
    private const val MAX_DDM_FRACTION_DIGITS: Int = 4

    /**
     * Converts a GPS coordinate in DMS (Degrees, Minutes, Seconds) format
     * or DDM (Degrees, Decimal Minutes) to decimal degrees.
     *
     * This method is designed to be robust and will not throw any errors.
     *
     * @param dms the GPS coordinate in DMS or DDM format.
     * @return the decimal value of the GPS coordinate, or null if the input is null or invalid.
     */
    @Suppress("MagicNumber")
    public fun dmsToDecimal(dms: String?): Double? {

        /* Blank values are illegal. */
        if (dms.isNullOrBlank())
            return null

        val directionLetter = dms.last()

        /* Proper dms ends with a direction letter. */
        if (directionLetter !in setOf('N', 'S', 'E', 'W'))
            return null

        val parts = dms.split(",", "N", "S", "E", "W")

        /* Proper dms requires degrees and minutes. Only seconds are optional. */
        if (parts.size < 2)
            return null

        val degrees = parts[0].toDoubleOrNull() ?: return null
        val minutes = parts[1].toDoubleOrNull() ?: return null
        val seconds = if (parts.size >= 3) parts[2].toDoubleOrNull() ?: 0.0 else 0.0

        val direction = if (directionLetter == 'S' || directionLetter == 'W') -1 else 1

        return direction * (degrees + minutes / MINUTES_PER_HOUR + seconds / 3600)
    }

    /**
     * XMP requires geo data to be in DDM (Degrees, decimal minutes) format.
     */
    public fun decimalLatitudeToDDM(latitude: Double): String {

        val direction = if (latitude >= 0) "N" else "S"

        val latitudeAbs = abs(latitude)

        val degrees = latitudeAbs.toInt()
        val minutes = (latitudeAbs - degrees) * MINUTES_PER_HOUR

        val minutesRounded = minutes.roundTo(MAX_DDM_FRACTION_DIGITS)

        return "$degrees,$minutesRounded$direction"
    }

    /**
     * XMP requires geo data to be in DDM (Degrees, decimal minutes) format.
     */
    public fun decimalLongitudeToDDM(longitude: Double): String {

        val direction = if (longitude >= 0) "E" else "W"

        val longitudeAbs = abs(longitude)

        val degrees = longitudeAbs.toInt()
        val minutes = (longitudeAbs - degrees) * MINUTES_PER_HOUR

        val minutesRounded = minutes.roundTo(MAX_DDM_FRACTION_DIGITS)

        return "$degrees,$minutesRounded$direction"
    }

    private fun Double.roundTo(numFractionDigits: Int): Double {
        val factor = 10.0.pow(numFractionDigits.toDouble())
        return (this * factor).roundToLong() / factor
    }
}
