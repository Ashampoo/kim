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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.ExifTag.EXIF_DIRECTORY_UNKNOWN
import com.ashampoo.kim.format.tiff.constants.GpsTag
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.format.tiff.taginfos.TagInfo
import com.ashampoo.kim.format.tiff.taginfos.TagInfoUnknowns

internal object TiffTags {

    private val ALL_TAGS = ExifTag.ALL_EXIF_TAGS + GpsTag.ALL_GPS_TAGS + TiffTag.ALL_TIFF_TAGS
    private val ALL_TAG_MAP = ALL_TAGS.groupByTo(mutableMapOf()) { it.tag }

    fun getTag(directoryType: Int, tag: Int): TagInfo {

        val possibleMatches = ALL_TAG_MAP[tag]
            ?: return TagInfoUnknowns("Unknown", tag, TagInfo.LENGTH_UNKNOWN, null)

        return getTag(directoryType, possibleMatches)
    }

    /*
     * Note: Keep in sync with ImageMetadata.findTiffField()
     */
    @Suppress("UnnecessaryParentheses")
    private fun getTag(directoryType: Int, possibleMatches: List<TagInfo>): TagInfo {

        val exactMatch = possibleMatches.firstOrNull { tagInfo ->
            tagInfo.directoryType?.directoryType == directoryType &&
                tagInfo.directoryType != EXIF_DIRECTORY_UNKNOWN
        }

        if (exactMatch != null)
            return exactMatch

        val inexactMatch = possibleMatches.firstOrNull { tagInfo ->
            val isImageDirectory = tagInfo.directoryType?.isImageDirectory ?: false
            (directoryType >= 0 && isImageDirectory) || (directoryType < 0 && !isImageDirectory)
        }

        if (inexactMatch != null)
            return inexactMatch

        val wildcardMatch = possibleMatches.firstOrNull { tagInfo ->
            tagInfo.directoryType == EXIF_DIRECTORY_UNKNOWN
        }

        if (wildcardMatch != null)
            return wildcardMatch

        return TiffTag.TIFF_TAG_UNKNOWN
    }
}
