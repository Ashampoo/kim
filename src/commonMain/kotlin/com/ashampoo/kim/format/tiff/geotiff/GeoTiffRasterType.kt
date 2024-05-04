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

import kotlin.jvm.JvmStatic

/**
 * 6.3.1.2 Raster Type Codes
 *
 * See http://geotiff.maptools.org/spec/geotiff6.html#6.3.1.2
 */
@Suppress("MagicNumber")
public enum class GeoTiffRasterType(
    public val typeCode: Short,
    public val displayName: String
) {

    /** Projection Coordinate System */
    PIXEL_IS_AREA(1, "Pixel Is Area"),

    /** Geographic latitude-longitude System */
    PIXEL_IS_POINT(2, "Pixel Is Point"),

    /** user-defined */
    USER_DEFINED(32767, "User Defined");

    public companion object {

        @JvmStatic
        public fun of(typeCode: Short): GeoTiffRasterType? =
            GeoTiffRasterType.entries.firstOrNull { it.typeCode == typeCode }
    }
}
