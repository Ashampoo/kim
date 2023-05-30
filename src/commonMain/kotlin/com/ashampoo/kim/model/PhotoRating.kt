/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

const val REJECTED_VALUE: Int = -1
const val UNRATED_VALUE: Int = 0
const val ONE_STAR_VALUE: Int = 1
const val TWO_STARS_VALUE: Int = 2
const val THREE_STARS_VALUE: Int = 3
const val FOUR_STARS_VALUE: Int = 4
const val FIVE_STARS_VALUE: Int = 5

const val REJECTED_STRING: String = "-----"
const val UNRATED_STRING: String = "☆☆☆☆☆"
const val ONE_STAR_STRING: String = "★☆☆☆☆"
const val TWO_STARS_STRING: String = "★★☆☆☆"
const val THREE_STARS_STRING: String = "★★★☆☆"
const val FOUR_STARS_STRING: String = "★★★★☆"
const val FIVE_STARS_STRING: String = "★★★★★"

/**
 * This represents the XMP exif:Rating property,
 * which is a 5-star-system with 0 for unrated
 * and -1 for rejected images.
 */
enum class PhotoRating(
    val value: Int,
    val string: String
) {

    REJECTED(REJECTED_VALUE, REJECTED_STRING),
    UNRATED(UNRATED_VALUE, UNRATED_STRING),
    ONE_STAR(ONE_STAR_VALUE, ONE_STAR_STRING),
    TWO_STARS(TWO_STARS_VALUE, TWO_STARS_STRING),
    THREE_STARS(THREE_STARS_VALUE, THREE_STARS_STRING),
    FOUR_STARS(FOUR_STARS_VALUE, FOUR_STARS_STRING),
    FIVE_STARS(FIVE_STARS_VALUE, FIVE_STARS_STRING);

    companion object {

        val validIntRange: IntRange = REJECTED.value..FIVE_STARS.value

        /* Warning: Swift problems if value is of type Int? */
        fun of(value: Int): PhotoRating? = when (value) {
            REJECTED_VALUE -> REJECTED
            UNRATED_VALUE -> UNRATED
            ONE_STAR_VALUE -> ONE_STAR
            TWO_STARS_VALUE -> TWO_STARS
            THREE_STARS_VALUE -> THREE_STARS
            FOUR_STARS_VALUE -> FOUR_STARS
            FIVE_STARS_VALUE -> FIVE_STARS
            else -> null
        }

        fun of(value: String?): PhotoRating? = when (value) {
            "-1" -> REJECTED
            "0" -> UNRATED
            "1" -> ONE_STAR
            "2" -> TWO_STARS
            "3" -> THREE_STARS
            "4" -> FOUR_STARS
            "5" -> FIVE_STARS
            else -> null
        }
    }
}
