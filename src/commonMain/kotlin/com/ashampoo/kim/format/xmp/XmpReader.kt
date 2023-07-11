/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.ashampoo.kim.format.xmp

import com.ashampoo.kim.common.GpsUtil
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.model.PhotoMetadata
import com.ashampoo.kim.model.PhotoRating
import com.ashampoo.kim.model.RegionArea
import com.ashampoo.kim.model.TiffOrientation
import com.ashampoo.xmp.XMPConst
import com.ashampoo.xmp.XMPMetaFactory
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

/**
 * We only read metadata that the user is likely to change/correct
 * like orientation, keywords and GPS.
 *
 * We ignore capture parameters like iso, focal length and so
 * on because we prefer to get that from EXIF.
 */
object XmpReader {

    private const val XMP_DC_SUBJECT = "subject"
    private const val XMP_IPTCEXT_PERSON_IN_IMAGE = "PersonInImage"
    private const val XMP_ACDSEE_KEYWORDS = "keywords"

    @Suppress("LoopWithTooManyJumpStatements")
    fun readMetadata(xmp: String, underUnitTesting: Boolean = false): PhotoMetadata {

        val xmpMeta = XMPMetaFactory.parseFromString(xmp)

        /*
         * Read taken date
         */

        val takenDateIsoString = xmpMeta.getPropertyString(XMPConst.NS_EXIF, "DateTimeOriginal")

        val timeZone = if (underUnitTesting)
            TimeZone.of("GMT+02:00")
        else
            TimeZone.currentSystemDefault()

        val takenDateIsoStringWithoutTimezone =
            takenDateIsoString
                ?.substringBefore('+')
                ?.substringBefore('Z')

        val takenDate = if (takenDateIsoStringWithoutTimezone != null)
            try {
                LocalDateTime.parse(takenDateIsoStringWithoutTimezone)
                    .toInstant(timeZone)
                    .toEpochMilliseconds()
            } catch (ignore: Exception) {
                /* We ignore invalid XMP DateTimeOriginal values. */
                null
            }
        else
            null

        val orientation =
            TiffOrientation.of(xmpMeta.getPropertyInteger(XMPConst.NS_TIFF, "Orientation"))

        val rating = PhotoRating.of(xmpMeta.getPropertyString(XMPConst.NS_XMP, "Rating"))

        /*
         * Read location
         */

        val latitude =
            GpsUtil.dmsToDecimal(xmpMeta.getPropertyString(XMPConst.NS_EXIF, "GPSLatitude"))

        val longitude =
            GpsUtil.dmsToDecimal(xmpMeta.getPropertyString(XMPConst.NS_EXIF, "GPSLongitude"))

        val gpsCoordinates = if (latitude != null && longitude != null)
            GpsCoordinates(latitude, longitude)
        else
            null

        /*
         * Read dc:subject for keywords
         */

        val keywords = mutableSetOf<String>()

        val subjectCount = xmpMeta.countArrayItems(XMPConst.NS_DC, XMP_DC_SUBJECT)

        for (index in 1..subjectCount) {

            val keyword = xmpMeta.getPropertyString(
                XMPConst.NS_DC,
                "$XMP_DC_SUBJECT[$index]"
            ) ?: continue

            keywords.add(keyword)
        }

        /*
         * Try to read acdsee:keywords instead, so that ACDSee users see their tags.
         */

        if (keywords.isEmpty()) {

            val acdseeKeywordsExist = xmpMeta.doesPropertyExist(XMPConst.NS_ACDSEE, XMP_ACDSEE_KEYWORDS)

            if (acdseeKeywordsExist) {

                val acdseeKeywordCount = xmpMeta.countArrayItems(XMPConst.NS_ACDSEE, XMP_ACDSEE_KEYWORDS)

                for (index in 1..acdseeKeywordCount) {

                    val keyword = xmpMeta.getPropertyString(
                        XMPConst.NS_ACDSEE,
                        "$XMP_ACDSEE_KEYWORDS[$index]"
                    ) ?: continue

                    keywords.add(keyword)
                }
            }
        }

        /*
         * Read mwg-rs:Regions for faces
         */

        val faces = mutableMapOf<String, RegionArea>()

        val regionListExists = xmpMeta.doesPropertyExist(XMPConst.NS_MWG_RS, "Regions/mwg-rs:RegionList")

        if (regionListExists) {

            val regionCount = xmpMeta.countArrayItems(XMPConst.NS_MWG_RS, "Regions/mwg-rs:RegionList")

            for (index in 1..regionCount) {

                val prefix = "Regions/mwg-rs:RegionList[$index]/mwg-rs"

                val regionType = xmpMeta.getPropertyString(XMPConst.NS_MWG_RS, "$prefix:Type")

                /* We only want faces. */
                if (regionType != "Face")
                    continue

                val name = xmpMeta.getPropertyString(XMPConst.NS_MWG_RS, "$prefix:Name")
                val xPos = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:x")
                val yPos = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:y")
                val width = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:w")
                val height = xmpMeta.getPropertyDouble(XMPConst.NS_MWG_RS, "$prefix:Area/stArea:h")

                /* Skip regions with missing data. */
                @Suppress("ComplexCondition")
                if (name == null || xPos == null || yPos == null || width == null || height == null)
                    continue

                faces[name] = RegionArea(xPos, yPos, width, height)
            }
        }

        /*
         * Read Iptc4xmpExt:PersonInImage for persons
         */

        val personsInImage = mutableSetOf<String>()

        val personsInImageCount =
            xmpMeta.countArrayItems(XMPConst.NS_IPTC_EXT, XMP_IPTCEXT_PERSON_IN_IMAGE)

        for (index in 1..personsInImageCount) {

            val personName =
                xmpMeta.getPropertyString(
                    XMPConst.NS_IPTC_EXT,
                    "$XMP_IPTCEXT_PERSON_IN_IMAGE[$index]"
                ) ?: continue

            personsInImage.add(personName)
        }

        /*
         * Compile into PhotoMetadata object
         */

        return PhotoMetadata(
            widthPx = null,
            heightPx = null,
            orientation = orientation,
            takenDate = takenDate,
            gpsCoordinates = gpsCoordinates,
            location = null, // TODO Read location information to avoid GPS resolving!
            rating = rating,
            keywords = keywords,
            faces = faces,
            personsInImage = personsInImage
        )
    }

    fun determineXmpPath(path: String): String {

        val xmpPath = path.replaceAfterLast(".", "xmp")

        /* If the path is not different, there was no suffix. */
        if (xmpPath != path)
            return xmpPath

        return path + ".xmp"
    }
}
