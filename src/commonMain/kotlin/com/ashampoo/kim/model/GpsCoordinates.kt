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

import kotlin.math.round

private const val MAX_LATITUDE = 90.0
private const val MIN_LATITUDE: Double = -MAX_LATITUDE

private const val MAX_LONGITUDE = 180.0
private const val MIN_LONGITUDE = -MAX_LONGITUDE

/** Around ~100 m accuracy */
private const val THREE_DIGIT_PRECISE: Double = 1_000.0

/** Around ~1 m accuracy */
private const val FIVE_DIGIT_PRECISE: Double = 100_000.0

private const val LAT_LONG_STRING_REGEX_PATTERN =
    """^\s*-?([1-8]?\d(\.\d+)?|90(\.0+)?),\s*-?(180(\.0+)?|((1[0-7]\d)|(\d{1,2}))(\.\d+)?)\s*$"""

private val latLongStringRegex: Regex = LAT_LONG_STRING_REGEX_PATTERN.toRegex()

public data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double
) {

    val latLongString: String = "${roundPrecise(latitude)}, ${roundPrecise(longitude)}"

    public fun toPreciseCoordinates(): GpsCoordinates = GpsCoordinates(
        latitude = roundPrecise(latitude),
        longitude = roundPrecise(longitude)
    )

    public fun toCoarseCoordinates(): GpsCoordinates = GpsCoordinates(
        latitude = roundCoarse(latitude),
        longitude = roundCoarse(longitude)
    )

    public fun isNullIsland(): Boolean =
        latitude == 0.0 && longitude == 0.0

    public fun isValid(): Boolean =
        latitude in MIN_LATITUDE..MAX_LATITUDE &&
            longitude in MIN_LONGITUDE..MAX_LONGITUDE

    public companion object {

        public fun parse(latLongString: String?): GpsCoordinates? {

            if (latLongString.isNullOrBlank())
                return null

            if (!latLongStringRegex.matches(latLongString))
                return null

            val parts = latLongString.split(",")

            return GpsCoordinates(
                latitude = parts[0].toDouble(),
                longitude = parts[1].toDouble()
            )
        }
    }
}

/**
 * Rounds the coordinates to three decimal places,
 * providing approximately 100 meters accuracy.
 */
private fun roundCoarse(value: Double): Double =
    round(value * THREE_DIGIT_PRECISE) / THREE_DIGIT_PRECISE

/**
 * Rounds the coordinates to five decimal places,
 * providing approximately 1 meter accuracy.
 * Suitable for display and precise localization.
 */
private fun roundPrecise(value: Double): Double =
    round(value * FIVE_DIGIT_PRECISE) / FIVE_DIGIT_PRECISE
