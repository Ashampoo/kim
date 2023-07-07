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
package com.ashampoo.kim.xmp

import com.ashampoo.kim.common.GpsUtil
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.xmp.XMPConst
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory
import com.ashampoo.xmp.options.PropertyOptions
import com.ashampoo.xmp.options.SerializeOptions
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object XmpWriter {

    private val xmpSerializeOptions =
        SerializeOptions()
            .setOmitXmpMetaElement(false)
            .setOmitPacketWrapper(false)
            .setUseCompactFormat(true)
            .setUseCanonicalFormat(false)
            .setSort(true)

    private val arrayOptions = PropertyOptions().setArray(true)

    /** GPSVersionID as written by default by ExifTool. */
    private const val DEFAULT_GPS_VERSION_ID = "2.3.0.0"

    private const val XMP_DC_SUBJECT = "subject"
    private const val XMP_IPTCEXT_PERSON_IN_IMAGE = "PersonInImage"

    /**
     * @param writePackageWrapper Should be "true" for embedded XMP
     */
    @Suppress("LoopWithTooManyJumpStatements")
    fun updateXmp(
        xmpMeta: XMPMeta,
        updates: Set<MetadataUpdate>,
        writePackageWrapper: Boolean,
        underUnitTesting: Boolean = false
    ): String {

        for (update in updates) {

            when (update) {

                is MetadataUpdate.Orientation ->
                    xmpMeta.setPropertyInteger(
                        XMPConst.NS_TIFF,
                        "Orientation",
                        update.tiffOrientation.value
                    )

                is MetadataUpdate.TakenDate -> {

                    if (update.takenDate != null) {

                        val timeZone = if (underUnitTesting)
                            TimeZone.of("GMT+02:00")
                        else
                            TimeZone.currentSystemDefault()

                        val isoDate = Instant.fromEpochMilliseconds(update.takenDate)
                            .toLocalDateTime(timeZone)
                            .toString()

                        xmpMeta.setProperty(XMPConst.NS_EXIF, "DateTimeOriginal", isoDate)

                    } else {

                        xmpMeta.deleteProperty(XMPConst.NS_EXIF, "DateTimeOriginal")
                    }
                }

                is MetadataUpdate.GpsCoordinates -> {

                    if (update.gpsCoordinates != null) {

                        /* This was a mandatory flag in the past, so we write it. */
                        xmpMeta.setProperty(
                            XMPConst.NS_EXIF,
                            "GPSVersionID",
                            DEFAULT_GPS_VERSION_ID
                        )

                        xmpMeta.setProperty(
                            XMPConst.NS_EXIF, "GPSLatitude",
                            GpsUtil.decimalLatitudeToDDM(update.gpsCoordinates.latitude)
                        )

                        xmpMeta.setProperty(
                            XMPConst.NS_EXIF, "GPSLongitude",
                            GpsUtil.decimalLongitudeToDDM(update.gpsCoordinates.longitude)
                        )

                    } else {

                        xmpMeta.deleteProperty(XMPConst.NS_EXIF, "GPSVersionID")
                        xmpMeta.deleteProperty(XMPConst.NS_EXIF, "GPSLatitude")
                        xmpMeta.deleteProperty(XMPConst.NS_EXIF, "GPSLongitude")
                    }
                }

                is MetadataUpdate.Rating ->
                    xmpMeta.setPropertyInteger(
                        XMPConst.NS_XMP,
                        "Rating",
                        update.photoRating.value
                    )

                is MetadataUpdate.Keywords -> {

                    /* Delete existing entries, if any */
                    xmpMeta.deleteProperty(XMPConst.NS_DC, XMP_DC_SUBJECT)

                    if (update.keywords.isNotEmpty()) {

                        /* Create a new array property. */
                        xmpMeta.setProperty(
                            XMPConst.NS_DC,
                            XMP_DC_SUBJECT,
                            null,
                            arrayOptions
                        )

                        /* Fill the new array with keywords. */
                        for (keyword in update.keywords.sorted())
                            xmpMeta.appendArrayItem(
                                schemaNS = XMPConst.NS_DC,
                                arrayName = XMP_DC_SUBJECT,
                                itemValue = keyword
                            )
                    }
                }

                is MetadataUpdate.Faces -> {

                    error("Writing of faces is not supported right now.")

//                    // TODO Write faces
//                    if (update.faces.isNotEmpty()) {
//
//                        /* Delete existing entries, if any */
//                        xmpMeta.deleteProperty(NS_MWG_RS, "Regions")
//
//                        xmpMeta.setStructField(
//                            NS_MWG_RS, "Regions/mwg-rs:AppliedToDimensions",
//                            XMPConst.TYPE_DIMENSIONS, "w",
//                            xmpMeta.widthPx.toString()
//                        )
//
//                        xmpMeta.setStructField(
//                            NS_MWG_RS, "Regions/mwg-rs:AppliedToDimensions",
//                            XMPConst.TYPE_DIMENSIONS, "h",
//                            xmpMeta.heightPx.toString()
//                        )
//
//                        xmpMeta.setStructField(
//                            NS_MWG_RS, "Regions/mwg-rs:AppliedToDimensions",
//                            XMPConst.TYPE_DIMENSIONS, "unit", "pixel"
//                        )
//
//                        xmpMeta.setStructField(
//                            NS_MWG_RS, "Regions", NS_MWG_RS, "RegionList", null,
//                            orderedArrayOptions
//                        )
//
//                        // How to proceed further?
//                    }
                }

                is MetadataUpdate.Persons -> {

                    /* Delete existing entries, if any */
                    xmpMeta.deleteProperty(XMPConst.NS_IPTCEXT, XMP_IPTCEXT_PERSON_IN_IMAGE)

                    if (update.personsInImage.isNotEmpty()) {

                        /* Create a new array property. */
                        xmpMeta.setProperty(
                            XMPConst.NS_IPTCEXT,
                            XMP_IPTCEXT_PERSON_IN_IMAGE,
                            null,
                            arrayOptions
                        )

                        /* Fill the new array with persons. */
                        for (person in update.personsInImage.sorted())
                            xmpMeta.appendArrayItem(
                                schemaNS = XMPConst.NS_IPTCEXT,
                                arrayName = XMP_IPTCEXT_PERSON_IN_IMAGE,
                                itemValue = person
                            )
                    }
                }
            }
        }

        /* We clone and modify the clone to prevent concurrency issues. */
        val options =
            xmpSerializeOptions.clone().setOmitPacketWrapper(!writePackageWrapper)

        return XMPMetaFactory.serializeToString(xmpMeta, options)
    }
}
