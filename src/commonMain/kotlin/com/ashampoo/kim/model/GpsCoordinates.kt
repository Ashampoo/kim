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
package com.ashampoo.kim.model

import kotlin.math.round

private const val MAX_LATITUDE = 90.0
private const val MIN_LATITUDE: Double = -MAX_LATITUDE

private const val MAX_LONGITUDE = 180.0
private const val MIN_LONGITUDE = -MAX_LONGITUDE

private const val THREE_DIGIT_PRECISE: Double = 1_000.0
private const val FIVE_DIGIT_PRECISE: Double = 100_000.0

public data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double
) {

    val displayString: String = "GPS: ${roundForDisplay(latitude)}, ${roundForDisplay(longitude)}"

    public fun toRoundedForCaching(): GpsCoordinates = GpsCoordinates(
        latitude = roundForCaching(latitude),
        longitude = roundForCaching(longitude)
    )

    public fun isNullIsland(): Boolean = latitude == 0.0 && longitude == 0.0

    public fun isValid(): Boolean =
        latitude in MIN_LATITUDE..MAX_LATITUDE && longitude in MIN_LONGITUDE..MAX_LONGITUDE
}

private fun roundForCaching(value: Double): Double =
    round(value * THREE_DIGIT_PRECISE) / THREE_DIGIT_PRECISE

private fun roundForDisplay(value: Double): Double =
    round(value * FIVE_DIGIT_PRECISE) / FIVE_DIGIT_PRECISE
