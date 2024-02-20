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

enum class TiffDirectoryType(
    val typeId: Int,
    val displayName: String,
    val isImageDirectory: Boolean
) {

    TIFF_DIRECTORY_IFD0(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD0, "IFD0", true
    ),
    TIFF_DIRECTORY_IFD1(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD1, "IFD1", true
    ),
    TIFF_DIRECTORY_IFD2(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD2, "IFD2", true
    ),
    TIFF_DIRECTORY_IFD3(
        TiffConstants.TIFF_DIRECTORY_TYPE_IFD3, "IFD3", true
    ),
    EXIF_DIRECTORY_INTEROP_IFD(
        TiffConstants.TIFF_DIRECTORY_INTEROP, "InteropIFD", false
    ),
    EXIF_DIRECTORY_EXIF_IFD(
        TiffConstants.TIFF_DIRECTORY_EXIF, "ExifIFD", false
    ),
    EXIF_DIRECTORY_GPS(
        TiffConstants.TIFF_DIRECTORY_GPS, "GPS", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_CANON(
        TiffConstants.TIFF_MAKER_NOTE_CANON, "MakerNoteCanon", false
    ),
    EXIF_DIRECTORY_MAKER_NOTE_NIKON(
        TiffConstants.TIFF_MAKER_NOTE_NIKON, "MakerNoteNikon", false
    );

    override fun toString(): String =
        displayName
}
