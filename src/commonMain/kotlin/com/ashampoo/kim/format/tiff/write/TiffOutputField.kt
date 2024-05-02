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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.HEX_RADIX
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import com.ashampoo.kim.format.tiff.fieldtype.FieldType
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.output.BinaryByteWriter

class TiffOutputField(
    val tag: Int,
    val fieldType: FieldType<out Any>,
    val count: Int,
    private var bytes: ByteArray
) : Comparable<TiffOutputField> {

    val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    val isLocalValue: Boolean = bytes.size <= TIFF_ENTRY_MAX_VALUE_LENGTH

    val separateValue: TiffOutputValue? =
        if (isLocalValue) null else TiffOutputValue("Value of $this", bytes)

    var sortHint = -1

    fun writeField(byteWriter: BinaryByteWriter) {

        byteWriter.write2Bytes(tag)
        byteWriter.write2Bytes(fieldType.type)
        byteWriter.write4Bytes(count)

        if (isLocalValue) {

            if (separateValue != null)
                throw ImageWriteException("Unexpected separate value item.")

            if (bytes.size > 4)
                throw ImageWriteException("Local value has invalid length: " + bytes.size)

            byteWriter.write(bytes)

            /* Fill the empty space with zeros */
            repeat(TIFF_ENTRY_MAX_VALUE_LENGTH - bytes.size) {
                byteWriter.write(0)
            }

        } else {

            if (separateValue == null)
                throw ImageWriteException("Missing separate value item.")

            byteWriter.write4Bytes(separateValue.offset.toInt())
        }
    }

    fun bytesAsHex(): String =
        bytes.toHex()

    fun bytesEqual(data: ByteArray): Boolean =
        bytes.contentEquals(data)

    fun setBytes(bytes: ByteArray) {

        if (this.bytes.size != bytes.size)
            throw ImageWriteException("Cannot change size of value.")

        this.bytes = bytes

        separateValue?.updateValue(bytes)
    }

    override fun toString(): String =
        "TiffOutputField $tagFormatted"

    override fun compareTo(other: TiffOutputField): Int {

        if (tag != other.tag)
            return tag - other.tag

        return sortHint - other.sortHint
    }

    companion object {

        fun createOffsetField(tagInfo: TagInfo, byteOrder: ByteOrder): TiffOutputField =
            TiffOutputField(tagInfo.tag, FieldTypeLong, 1, FieldTypeLong.writeData(0, byteOrder))
    }
}
