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
package com.ashampoo.kim.format.tiff.constant

import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Nikon MakerNote Tags
 *
 * https://exiftool.org/TagNames/Nikon.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object NikonTag {

    /*
     * TODO This list is incomplete
     */

    public val MAKER_NOTE_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x0001, "MakerNoteVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val SHUTTER_COUNT: TagInfoLong = TagInfoLong(
        0x00a7, "ShutterCount",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val AUXILIARY_LENS: TagInfoAscii = TagInfoAscii(
        0x0082, "AuxiliaryLens", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val LENS_TYPE: TagInfoByte = TagInfoByte(
        0x0083, "LensType",
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val LENS: TagInfoRationals = TagInfoRationals(
        0x0084, "Lens", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val LENS_F_STOPS: TagInfoUndefineds = TagInfoUndefineds(
        0x008b, "LensFStops", 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    public val ALL = listOf(
        MAKER_NOTE_VERSION, SHUTTER_COUNT,
        AUXILIARY_LENS, LENS_TYPE, LENS, LENS_F_STOPS
    )
}
