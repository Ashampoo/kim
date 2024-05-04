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
 * 6.3.1.1 Model Type Codes
 *
 * See http://geotiff.maptools.org/spec/geotiff6.html#6.3.1.1
 */
@Suppress("MagicNumber")
public enum class GeoTiffModelType(
    public val typeCode: Short,
    public val displayName: String
) {

    /** Projection Coordinate System */
    PROJECTED(1, "Projected"),

    /** Geographic latitude-longitude System */
    GEOGRAPHIC(2, "Geographic"),

    /** Geocentric (X,Y,Z) Coordinate System */
    GEOCENTRIC(3, "Geocentric"),

    /** user-defined */
    USER_DEFINED(32767, "User Defined");

    public companion object {

        @JvmStatic
        public fun of(typeCode: Short): GeoTiffModelType? =
            GeoTiffModelType.entries.firstOrNull { it.typeCode == typeCode }
    }
}
