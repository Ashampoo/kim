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

val emptyExifDates = setOf(
    "0000:00:00 00:00:00",
    "                   "
)

fun isExifDateEmpty(exifDate: String?): Boolean =
    exifDate.isNullOrEmpty() || emptyExifDates.contains(exifDate)

/**
 * EXIF dates are in the format of yyyy:MM:dd HH:mm:ss (19 chars),
 * which should be transformed to ISO like yyyy-MM-ddTHH:mm:ss.
 */
fun convertExifDateToIso8601Date(exifDate: String): String {

    require(!isExifDateEmpty(exifDate)) { "Given date was empty: $exifDate" }

    /*
     * Different vendors may have chosen to write a variant of the date
     * instead of the specified format. To encounter this we do replacements
     * that always should result in an ISO 8601 date.
     *
     * The shortest date we should ever encounter is yyyy:MM:dd (10 chars)
     */
    require(exifDate.length >= 10) { "Invalid date: $exifDate" }

    val charArray = exifDate.toCharArray()

    charArray[4] = '-'
    charArray[7] = '-'

    if (charArray.size > 10)
        charArray[10] = 'T'

    return charArray.concatToString()
}
