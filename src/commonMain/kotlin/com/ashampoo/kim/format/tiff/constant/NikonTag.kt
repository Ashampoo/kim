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

import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Nikon MakerNote Tags
 *
 * https://exiftool.org/TagNames/Nikon.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
object NikonTag {

    /*
     * TODO This list is incomplete
     */

    val MAKER_NOTE_VERSION = TagInfoUndefineds(
        "MakerNoteVersion", 0x0001, 4,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    val SHUTTER_COUNT = TagInfoLong(
        "ShutterCount", 0x00a7,
        TiffDirectoryType.EXIF_DIRECTORY_MAKER_NOTE_NIKON
    )

    val ALL = listOf(
        MAKER_NOTE_VERSION,
        SHUTTER_COUNT
    )
}
