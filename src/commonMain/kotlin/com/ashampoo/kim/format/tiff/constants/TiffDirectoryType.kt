/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff.constants

enum class TiffDirectoryType(
    val isImageDirectory: Boolean,
    val directoryType: Int,
    val directoryName: String
) {

    TIFF_DIRECTORY_IFD0(
        true, TiffConstants.DIRECTORY_TYPE_DIR_0, "IFD0"
    ),
    TIFF_DIRECTORY_IFD1(
        true, TiffConstants.DIRECTORY_TYPE_DIR_1, "IFD1"
    ),
    TIFF_DIRECTORY_IFD2(
        true, TiffConstants.DIRECTORY_TYPE_DIR_2, "IFD2"
    ),
    TIFF_DIRECTORY_IFD3(
        true, TiffConstants.DIRECTORY_TYPE_DIR_3, "IFD3"
    ),
    EXIF_DIRECTORY_INTEROP_IFD(
        false, TiffConstants.DIRECTORY_TYPE_INTEROPERABILITY, "Interop IFD"
    ),
    EXIF_DIRECTORY_MAKER_NOTES(
        false, TiffConstants.DIRECTORY_TYPE_MAKER_NOTES, "Maker Notes"
    ),
    EXIF_DIRECTORY_EXIF_IFD(
        false, TiffConstants.DIRECTORY_TYPE_EXIF, "Exif IFD"
    ),
    EXIF_DIRECTORY_GPS(
        false, TiffConstants.DIRECTORY_TYPE_GPS, "GPS IFD"
    );

    companion object {

        val EXIF_DIRECTORY_IFD0 = TIFF_DIRECTORY_IFD0
        val TIFF_DIRECTORY_ROOT = TIFF_DIRECTORY_IFD0
        val EXIF_DIRECTORY_IFD1 = TIFF_DIRECTORY_IFD1
        val EXIF_DIRECTORY_IFD2 = TIFF_DIRECTORY_IFD2
        val EXIF_DIRECTORY_IFD3 = TIFF_DIRECTORY_IFD3
        val EXIF_DIRECTORY_SUB_IFD = TIFF_DIRECTORY_IFD1
        val EXIF_DIRECTORY_SUB_IFD1 = TIFF_DIRECTORY_IFD2
        val EXIF_DIRECTORY_SUB_IFD2 = TIFF_DIRECTORY_IFD3

        val EXIF_DIRECTORY_UNKNOWN: TiffDirectoryType? = null
    }
}
