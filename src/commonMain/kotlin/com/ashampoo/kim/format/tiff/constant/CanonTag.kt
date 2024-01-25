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

import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Canon MakerNote Tags
 *
 * See https://exiftool.org/TagNames/Canon.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
object CanonTag {

    /*
     * TODO This list is incomplete
     */

    val CANON_TAG_CANON_CAMERA_SETTINGS = TagInfoUndefineds(
        "CanonCameraSettings", 0x0001, TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_CANON_MAKER_NOTES
    )

    val ALL_CANON_TAGS = listOf(
        CANON_TAG_CANON_CAMERA_SETTINGS
    )
}
