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
package com.ashampoo.kim.format.tiff.constants

enum class TiffDirectoryType(
    val isImageDirectory: Boolean,
    val directoryType: Int,
    val directoryName: String
) {

    TIFF_DIRECTORY_IFD0(
        true, TiffConstants.TIFF_IFD0, "IFD0"
    ),
    TIFF_DIRECTORY_IFD1(
        true, TiffConstants.TIFF_IFD1, "IFD1"
    ),
    TIFF_DIRECTORY_IFD2(
        true, TiffConstants.TIFF_IFD2, "IFD2"
    ),
    TIFF_DIRECTORY_IFD3(
        true, TiffConstants.TIFF_IFD3, "IFD3"
    ),
    EXIF_DIRECTORY_INTEROP_IFD(
        false, TiffConstants.TIFF_INTEROP_IFD, "InteropIFD"
    ),
    EXIF_DIRECTORY_MAKER_NOTES(
        false, TiffConstants.TIFF_MAKER_NOTES, "MakerNotes"
    ),
    EXIF_DIRECTORY_EXIF_IFD(
        false, TiffConstants.TIFF_EXIF_IFD, "ExifIFD"
    ),
    EXIF_DIRECTORY_GPS(
        false, TiffConstants.TIFF_GPS, "GPS"
    );

    override fun toString(): String =
        directoryName
}
