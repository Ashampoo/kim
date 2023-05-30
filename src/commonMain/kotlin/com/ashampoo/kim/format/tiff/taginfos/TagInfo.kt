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
package com.ashampoo.kim.format.tiff.taginfos

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constants.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType

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
        "0x" + tag.toString(16).padStart(4, '0')

    val description: String =
        "$tagFormatted $name"

    constructor(
        name: String,
        tag: Int,
        dataType: FieldType,
        length: Int = LENGTH_UNKNOWN,
        exifDirectory: TiffDirectoryType? = TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN
    ) : this(name, tag, listOf(dataType), length, exifDirectory)

    /**
     * @param entry the TIFF field whose value to return
     * @return the value of the TIFF field
     */
    open fun getValue(entry: TiffField): Any =
        entry.fieldType.getValue(entry)

    open fun encodeValue(fieldType: FieldType, value: Any, byteOrder: ByteOrder): ByteArray =
        fieldType.writeData(value, byteOrder)

    override fun toString(): String = tagFormatted

    open fun isText(): Boolean =
        false

    companion object {
        const val LENGTH_UNKNOWN = -1
    }
}
