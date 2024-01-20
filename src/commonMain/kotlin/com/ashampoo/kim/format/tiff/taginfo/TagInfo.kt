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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.HEX_RADIX
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constant.ExifTag.EXIF_DIRECTORY_UNKNOWN
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtype.FieldType

open class TagInfo(
    val name: String,
    val tag: Int,
    val dataTypes: List<FieldType>,
    val length: Int,
    val directoryType: TiffDirectoryType?,
    val isOffset: Boolean = false
) {

    /** Return a proper Tag ID like 0x0100 */
    val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    val description: String =
        "$tagFormatted $name"

    constructor(
        name: String,
        tag: Int,
        dataType: FieldType,
        length: Int = LENGTH_UNKNOWN,
        exifDirectory: TiffDirectoryType? = EXIF_DIRECTORY_UNKNOWN
    ) : this(name, tag, listOf(dataType), length, exifDirectory)

    /**
     * @param entry the TIFF field whose value to return
     * @return the value of the TIFF field
     *
     * Implementation detail: This indirection exists because
     * [TagInfoGpsText] has some special logic to interpret the value.
     */
    open fun getValue(entry: TiffField): Any =
        entry.fieldType.getValue(entry)

    open fun encodeValue(fieldType: FieldType, value: Any, byteOrder: ByteOrder): ByteArray =
        fieldType.writeData(value, byteOrder)

    override fun toString(): String =
        description

    open fun isText(): Boolean =
        false

    override fun equals(other: Any?): Boolean {

        if (this === other) return true
        if (other !is TagInfo) return false

        if (name != other.name) return false
        if (tag != other.tag) return false
        if (dataTypes != other.dataTypes) return false
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
        result = 31 * result + dataTypes.hashCode()
        result = 31 * result + length
        result = 31 * result + (directoryType?.hashCode() ?: 0)
        result = 31 * result + isOffset.hashCode()
        result = 31 * result + tagFormatted.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }

    companion object {
        const val LENGTH_UNKNOWN = -1
    }
}
