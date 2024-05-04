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
package com.ashampoo.kim.format.tiff.taginfo

import com.ashampoo.kim.common.HEX_RADIX
import com.ashampoo.kim.format.tiff.constant.ExifTag.EXIF_DIRECTORY_UNKNOWN
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtype.FieldType

public abstract class TagInfo(
    public val tag: Int,
    public val name: String,
    public val fieldType: FieldType<out Any>,
    public val length: Int = LENGTH_UNKNOWN,
    public val directoryType: TiffDirectoryType? = EXIF_DIRECTORY_UNKNOWN,
    public val isOffset: Boolean = false
) {

    /** Return a proper Tag ID like 0x0100 */
    public val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    public val description: String =
        "$tagFormatted $name"

    override fun toString(): String =
        description

    public open fun isText(): Boolean =
        false

    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is TagInfo) return false

        if (name != other.name) return false
        if (tag != other.tag) return false
        if (fieldType != other.fieldType) return false
        if (length != other.length) return false
        if (directoryType != other.directoryType) return false
        if (isOffset != other.isOffset) return false
        if (tagFormatted != other.tagFormatted) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + tag
        result = 31 * result + fieldType.hashCode()
        result = 31 * result + length
        result = 31 * result + (directoryType?.hashCode() ?: 0)
        result = 31 * result + isOffset.hashCode()
        result = 31 * result + tagFormatted.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    public companion object {
        public const val LENGTH_UNKNOWN: Int = -1
    }
}
