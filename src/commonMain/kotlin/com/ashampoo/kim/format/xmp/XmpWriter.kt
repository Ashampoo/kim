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
package com.ashampoo.kim.format.xmp

import com.ashampoo.kim.Kim.underUnitTesting
import com.ashampoo.kim.common.GpsUtil
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.xmp.XMPException
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory
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

    fun XMPMeta.applyUpdate(update: MetadataUpdate) {

        when (update) {

            is MetadataUpdate.Orientation ->
                setOrientation(update.tiffOrientation.value)

            is MetadataUpdate.TakenDate -> {

                if (update.takenDate != null) {

                    val timeZone = if (underUnitTesting)
                        TimeZone.of("GMT+02:00")
                    else
                        TimeZone.currentSystemDefault()

                    val isoDate = Instant.fromEpochMilliseconds(update.takenDate)
                        .toLocalDateTime(timeZone)
                        .toString()

                    setDateTimeOriginal(isoDate)

                } else {

                    deleteDateTimeOriginal()
                }
            }

            is MetadataUpdate.GpsCoordinates -> {

                if (update.gpsCoordinates != null)
                    setGpsCoordinates(
                        GpsUtil.decimalLatitudeToDDM(update.gpsCoordinates.latitude),
                        GpsUtil.decimalLongitudeToDDM(update.gpsCoordinates.longitude)
                    )
                else
                    deleteGpsCoordinates()
            }

            is MetadataUpdate.Rating ->
                setRating(update.photoRating.value)

            is MetadataUpdate.Keywords ->
                setKeywords(update.keywords)

//              is MetadataUpdate.Faces -> {
//                  TODO How do write the fields?
//              }

            is MetadataUpdate.Persons ->
                setPersonsInImage(update.personsInImage)
        }
    }

    /**
     * Note: Parameter 'writePackageWrapper' should be "true" for embedded XMP
     */
    @Throws(XMPException::class)
    @Suppress("LoopWithTooManyJumpStatements")
    fun updateXmp(
        xmpMeta: XMPMeta,
        updates: Set<MetadataUpdate>,
        writePackageWrapper: Boolean
    ): String {

        for (update in updates)
            xmpMeta.applyUpdate(update)

        return xmpMeta.serializeToString(writePackageWrapper)
    }

    /**
     * Note: Parameter 'writePackageWrapper' should be "true" for embedded XMP
     */
    @Throws(XMPException::class)
    @Suppress("LoopWithTooManyJumpStatements")
    fun updateXmp(
        xmpMeta: XMPMeta,
        update: MetadataUpdate,
        writePackageWrapper: Boolean
    ): String {

        xmpMeta.applyUpdate(update)

        return xmpMeta.serializeToString(writePackageWrapper)
    }

    private fun XMPMeta.serializeToString(
        writePackageWrapper: Boolean
    ): String {

        /* We clone and modify the clone to prevent concurrency issues. */
        val options =
            xmpSerializeOptions.clone().setOmitPacketWrapper(!writePackageWrapper)

        return XMPMetaFactory.serializeToString(this, options)
    }
}
