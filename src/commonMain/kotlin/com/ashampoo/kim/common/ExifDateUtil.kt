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

/**
 * This class contains extensions needed to
 * interpret bytes in photo files.
 */

private val emptyExifDates = setOf(
    "0000:00:00 00:00:00",
    "                   "
)

/**
 * "yyyy:MM:dd" is 10 chars
 */
private const val LENGTH_ONLY_DATE = 10

/**
 * "yyyy:MM:dd HH:mm:ss" is 19 chars
 */
private const val LENGTH_DATE_WITH_TIME = 19

private const val YEAR_AND_MONTH_SEPARATOR_INDEX = 4
private const val MONTH_AND_DAY_SEPARATOR_INDEX = 7
private const val TIME_SEPARATOR_INDEX = 10

private const val FIRST_SECOND_INDEX = 17
private const val SECOND_SECOND_INDEX = 18

fun isExifDateEmpty(exifDate: String?): Boolean =
    exifDate.isNullOrEmpty() || emptyExifDates.contains(exifDate)

/**
 * EXIF dates are in the format of "yyyy:MM:dd HH:mm:ss" (19 chars),
 * which should be transformed to ISO like "yyyy-MM-ddTHH:mm:ss".
 */
fun convertExifDateToIso8601Date(exifDate: String): String {

    require(!isExifDateEmpty(exifDate)) { "Given date was empty: $exifDate" }

    /*
     * Different vendors may have chosen to write a variant of the date
     * instead of the specified format. To encounter this we do replacements
     * that always should result in an ISO 8601 date.
     *
     * The shortest date we should ever encounter is "yyyy:MM:dd" (10 chars)
     */
    require(exifDate.length >= LENGTH_ONLY_DATE) { "Invalid date: $exifDate" }

    val charArray = exifDate.toCharArray()

    charArray[YEAR_AND_MONTH_SEPARATOR_INDEX] = '-'
    charArray[MONTH_AND_DAY_SEPARATOR_INDEX] = '-'

    if (charArray.size > LENGTH_ONLY_DATE)
        charArray[TIME_SEPARATOR_INDEX] = 'T'

    /**
     * We saw some files where some buggy software turned
     * "2023:05:12 18:04:00" into "2023:05:12 18:04:  ".
     * We don't want to lose the whole date, just because
     * some buggy software discarded the seconds.
     */
    if (charArray.size >= LENGTH_DATE_WITH_TIME) {

        if (charArray[FIRST_SECOND_INDEX] == ' ')
            charArray[FIRST_SECOND_INDEX] = '0'

        if (charArray[SECOND_SECOND_INDEX] == ' ')
            charArray[SECOND_SECOND_INDEX] = '0'
    }

    return charArray.concatToString()
}
