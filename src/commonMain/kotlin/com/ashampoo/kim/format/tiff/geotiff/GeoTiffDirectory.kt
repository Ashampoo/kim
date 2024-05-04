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
package com.ashampoo.kim.format.tiff.geotiff

import com.ashampoo.kim.common.ImageReadException

/**
 * See http://geotiff.maptools.org/spec/geotiff2.4.html
 */
public data class GeoTiffDirectory(

    /**
     * "KeyDirectoryVersion" indicates the current version of Key
     * implementation, and will only change if this Tag's Key
     * structure is changed. (Similar to the TIFFVersion (42)).
     * The current DirectoryVersion number is 1. This value will
     * most likely never change, and may be used to ensure that
     * this is a valid Key-implementation.
     */
    val keyDirectoryVersion: Short,

    /**
     * "KeyRevision" indicates what revision of Key-Sets are used.
     * "MinorRevision" indicates what set of Key-codes are used.
     * The complete revision number is denoted <KeyRevision>.<MinorRevision>
     */
    val keyRevision: Short,

    val minorRevision: Short,

    val modelType: GeoTiffModelType?,

    val rasterType: GeoTiffRasterType?,

    val geographicType: GeoTiffGeographicType?

) {

    val geoTiffVersionString: String =
        "$keyDirectoryVersion.$keyRevision.$minorRevision"

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- GeoTiff ----")
        sb.appendLine("Version         : $geoTiffVersionString")
        sb.appendLine("Model type      : ${modelType?.displayName ?: "-/-"}")
        sb.appendLine("Raster type     : ${rasterType?.displayName ?: "-/-"}")
        sb.appendLine("Geographic type : ${geographicType?.displayName ?: "-/-"}")

        return sb.toString()
    }

    public companion object {

        @Suppress("MagicNumber")
        public fun parseFrom(shorts: ShortArray): GeoTiffDirectory {

            require(shorts.size >= 4) {
                "GeoTiffDirectory should be at least 4 bytes, but was ${shorts.size}."
            }

            val keyDirectoryVersion = shorts[0]

            if (keyDirectoryVersion != 1.toShort())
                throw ImageReadException("Illegal KeyDirectoryVersion: $keyDirectoryVersion")

            val keyRevision = shorts[1]
            val minorRevision = shorts[2]

            /*
             * "NumberOfKeys" indicates how many Keys
             * are defined by the rest of this Tag.
             */
            val numberOfKeys = shorts[3]

            var geoTiffModelType: GeoTiffModelType? = null
            var geoTiffRasterType: GeoTiffRasterType? = null
            var geoTiffGeographicType: GeoTiffGeographicType? = null

            repeat(numberOfKeys.toInt()) { index ->

                /*
                 * "KeyID" gives the key-ID value of the Key (identical in function
                 * to TIFF tag ID, but completely independent of TIFF tag-space)
                 */
                val keyId = shorts[3 + index * 4 + 1]

                /*
                 * We only want to handle keys we know.
                 */
                val geoKey = GeoKey.of(keyId) ?: return@repeat

//                /*
//                 * "TIFFTagLocation" indicates which TIFF tag contains the value(s)
//                 * of the Key: if TIFFTagLocation is 0, then the value is SHORT,
//                 * and is contained in the "Value_Offset" entry. Otherwise, the type
//                 * (format) of the value is implied by the TIFF-Type of the tag
//                 * containing the value.
//                 */
//                val tiffTagLocation = shorts[3 + index * 4 + 2]
//
//                /* "Count" indicates the number of values in this key. */
//                val count = shorts[3 + index * 4 + 3]

                /*
                 * "Value_Offset" Value_Offset indicates the index-offset *into* the TagArray
                 * indicated by TIFFTagLocation, if it is nonzero.
                 * If TIFFTagLocation=0, then Value_Offset contains the actual (SHORT) value
                 * of the Key, and Count=1 is implied. Note that the offset is not a byte-offset,
                 * but rather an index based on the natural data type of the specified tag array.
                 */
                val valueOrOffset = shorts[3 + index * 4 + 4]

                /*
                 * Handle specific interesting values.
                 */
                when (geoKey) {

                    GeoKey.GTModelTypeGeoKey ->
                        geoTiffModelType = GeoTiffModelType.of(valueOrOffset)

                    GeoKey.GTRasterTypeGeoKey ->
                        geoTiffRasterType = GeoTiffRasterType.of(valueOrOffset)

                    GeoKey.GeographicTypeGeoKey ->
                        geoTiffGeographicType = GeoTiffGeographicType.of(valueOrOffset)

                    else -> return@repeat
                }
            }

            return GeoTiffDirectory(
                keyDirectoryVersion,
                keyRevision,
                minorRevision,
                geoTiffModelType,
                geoTiffRasterType,
                geoTiffGeographicType
            )
        }
    }
}
