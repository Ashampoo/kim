/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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
package com.ashampoo.kim.format.tiff.constant

import com.ashampoo.kim.format.tiff.constant.ExifTag.EXIF_DIRECTORY_UNKNOWN
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoDoubles
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShorts

/**
 * See https://exiftool.org/TagNames/GeoTiff.html
 */
@Suppress("MagicNumber")
object GeoTiffTag {

    val EXIF_TAG_MODEL_PIXEL_SCALE_TAG: TagInfoDoubles = TagInfoDoubles(
        0x830e, "ModelPixelScaleTag", 3,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_INTERGRAPH_MATRIX_TAG: TagInfoDoubles = TagInfoDoubles(
        0x8480, "IntergraphMatrixTag", -1,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_MODEL_TIEPOINT_TAG: TagInfoDoubles = TagInfoDoubles(
        0x8482, "ModelTiepointTag", -1,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_MODEL_TRANSFORMATION_TAG: TagInfoDoubles = TagInfoDoubles(
        0x85d8, "ModelTransformationTag", 16,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_GEO_KEY_DIRECTORY_TAG: TagInfoShorts = TagInfoShorts(
        0x87af, "GeoKeyDirectoryTag", -1,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_GEO_DOUBLE_PARAMS_TAG: TagInfoDoubles = TagInfoDoubles(
        0x87b0, "GeoDoubleParamsTag", -1,
        EXIF_DIRECTORY_UNKNOWN
    )

    val EXIF_TAG_GEO_ASCII_PARAMS_TAG: TagInfoAscii = TagInfoAscii(
        0x87b1, "GeoAsciiParamsTag", -1,
        EXIF_DIRECTORY_UNKNOWN
    )

    val ALL = listOf(
        EXIF_TAG_MODEL_PIXEL_SCALE_TAG,
        EXIF_TAG_INTERGRAPH_MATRIX_TAG,
        EXIF_TAG_MODEL_TIEPOINT_TAG,
        EXIF_TAG_MODEL_TRANSFORMATION_TAG,
        EXIF_TAG_GEO_KEY_DIRECTORY_TAG,
        EXIF_TAG_GEO_DOUBLE_PARAMS_TAG,
        EXIF_TAG_GEO_ASCII_PARAMS_TAG
    )
}
