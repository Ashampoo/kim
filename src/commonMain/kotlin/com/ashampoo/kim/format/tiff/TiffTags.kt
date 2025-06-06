/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.format.tiff.constant.CanonTag
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.ExifTag.EXIF_DIRECTORY_UNKNOWN
import com.ashampoo.kim.format.tiff.constant.GeoTiffTag
import com.ashampoo.kim.format.tiff.constant.GpsTag
import com.ashampoo.kim.format.tiff.constant.NikonTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.format.tiff.taginfo.TagInfo

internal object TiffTags {

    /* Note: Ordered to give EXIF tag names priority. */
    private val TIFF_AND_EXIF_TAGS = ExifTag.ALL + TiffTag.ALL + GeoTiffTag.ALL

    private val TIFF_AND_EXIF_TAGS_MAP = TIFF_AND_EXIF_TAGS.groupByTo(mutableMapOf()) { it.tag }
    private val GPS_TAGS_MAP = GpsTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val CANON_TAGS_MAP = CanonTag.ALL.groupByTo(mutableMapOf()) { it.tag }
    private val NIKON_TAGS_MAP = NikonTag.ALL.groupByTo(mutableMapOf()) { it.tag }

    fun getTag(directoryType: Int, tag: Int): TagInfo? {

        /*
         * GPS and Maker Notes should be exact matches.
         */
        @Suppress("UseIfInsteadOfWhen")
        val possibleMatches = when (directoryType) {
            TiffConstants.TIFF_DIRECTORY_GPS -> GPS_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_CANON -> CANON_TAGS_MAP[tag]
            TiffConstants.TIFF_MAKER_NOTE_NIKON -> NIKON_TAGS_MAP[tag]
            else -> TIFF_AND_EXIF_TAGS_MAP[tag]
        } ?: return null

        return getTag(directoryType, possibleMatches)
    }

    /*
     * Note: Keep in sync with ImageMetadata.findTiffField()
     */
    @Suppress("UnnecessaryParentheses")
    private fun getTag(directoryType: Int, possibleMatches: List<TagInfo>): TagInfo? {

        val exactMatch = possibleMatches.firstOrNull { tagInfo ->
            tagInfo.directoryType?.typeId == directoryType &&
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

        return null
    }
}
