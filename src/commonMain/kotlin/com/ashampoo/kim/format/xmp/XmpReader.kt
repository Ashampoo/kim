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
package com.ashampoo.kim.format.xmp

import com.ashampoo.kim.Kim.underUnitTesting
import com.ashampoo.kim.common.GpsUtil
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.PhotoMetadata
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.TiffOrientation
import com.ashampoo.xmp.XMPException
import com.ashampoo.xmp.XMPMetaFactory
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.jvm.JvmStatic

/**
 * We only read metadata that the user is likely to change/correct
 * like orientation, keywords and GPS.
 *
 * We ignore capture parameters like iso, focal length and so
 * on because we prefer to get that from EXIF.
 */
public object XmpReader {

    @Suppress("LoopWithTooManyJumpStatements")
    @Throws(XMPException::class)
    @JvmStatic
    public fun readMetadata(xmp: String): PhotoMetadata {

        val xmpMeta = XMPMetaFactory.parseFromString(xmp)

        /*
         * Read taken date
         */

        val takenDateIsoString = xmpMeta.getDateTimeOriginal()

        val timeZone = if (underUnitTesting)
            TimeZone.of("GMT+02:00")
        else
            TimeZone.currentSystemDefault()

        val takenDateIsoStringWithoutTimezone =
            takenDateIsoString
                ?.substringBefore('+')
                ?.substringBefore('Z')

        val takenDate = takenDateIsoStringWithoutTimezone?.let {
            try {
                LocalDateTime.parse(it)
                    .toInstant(timeZone)
                    .toEpochMilliseconds()
            } catch (ignore: Exception) {
                /* We ignore invalid XMP DateTimeOriginal values. */
                null
            }
        }

        /*
         * Read location
         */

        val latitude = GpsUtil.dmsToDecimal(xmpMeta.getGpsLatitude())
        val longitude = GpsUtil.dmsToDecimal(xmpMeta.getGpsLongitude())

        val gpsCoordinates = if (latitude != null && longitude != null)
            GpsCoordinates(latitude, longitude)
        else
            null

        /*
         * Compile into PhotoMetadata object
         */

        return PhotoMetadata(
            orientation = TiffOrientation.of(xmpMeta.getOrientation()),
            takenDate = takenDate,
            gpsCoordinates = gpsCoordinates,
            location = null, // TODO Read location information to avoid GPS resolving!
            flagged = xmpMeta.isFlagged(),
            rating = xmpMeta.getRating()?.let { PhotoRating.of(it) },
            keywords = xmpMeta.getKeywords().ifEmpty {
                xmpMeta.getAcdSeeKeywords()
            },
            faces = xmpMeta.getFaces(),
            personsInImage = xmpMeta.getPersonsInImage(),
            albums = xmpMeta.getAlbums()
        )
    }
}
