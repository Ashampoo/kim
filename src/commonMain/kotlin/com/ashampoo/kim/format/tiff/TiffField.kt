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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.HEX_RADIX
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.RationalNumber
import com.ashampoo.kim.common.head
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.tiff.TiffTags.getTag
import com.ashampoo.kim.format.tiff.fieldtype.FieldType
import com.ashampoo.kim.format.tiff.taginfo.TagInfo

/**
 * A TIFF field in a TIFF directory.
 */
class TiffField(
    /** Offset relative to TIFF header */
    val offset: Int,
    val tag: Int,
    val directoryType: Int,
    val fieldType: FieldType,
    val count: Int,
    /** Set if field has a local value. */
    val localValue: Int?,
    /** Set if field has a offset pointer to its value. */
    val valueOffset: Int?,
    val valueBytes: ByteArray,
    val byteOrder: ByteOrder,
    val sortHint: Int
) {

    /**
     * Returns the offset with padding.
     * Because TIFF files can be as big as 4 GB we need 10 digits to present that.
     */
    val offsetFormatted: String =
        offset.toString().padStart(10, '0')

    /** Return a proper Tag ID like 0x0100 */
    val tagFormatted: String =
        "0x" + tag.toString(HEX_RADIX).padStart(4, '0')

    val tagInfo: TagInfo = getTag(directoryType, tag)

    val bytesLength: Int = count.toInt() * fieldType.size

    val byteArrayValue: ByteArray = valueBytes.head(bytesLength)

    val value: Any = tagInfo.getValue(this)

    val valueDescription: String by lazy {
        try {

            if (value is ByteArray) {

                if (value.size <= MAX_BYTE_ARRAY_DISPLAY_SIZE)
                    return@lazy "[${value.toSingleNumberHexes()}]"

                return@lazy "[${value.size} bytes]"
            }

            if (value is IntArray)
                return@lazy value.contentToString()

            if (value is ShortArray)
                return@lazy value.contentToString()

            if (value is DoubleArray)
                return@lazy value.contentToString()

            if (value is FloatArray)
                return@lazy value.contentToString()

            if (value is Array<*>)
                return@lazy value.contentToString()

            value.toString()

        } catch (ex: ImageReadException) {
            "Invalid value: " + ex.message
        }
    }

    fun toStringValue(): String {

        if (value is List<*>) {

            /*
             * If the field is all NULLs, this wil result in an empty list.
             */
            val firstValue = value.firstOrNull() ?: return ""

            return firstValue.toString()
        }

        if (value !is String)
            throw ImageReadException("Expected String value(" + tagInfo.tagFormatted + "): " + value)

        return value
    }

    fun toIntArray(): IntArray {

        if (value is Number)
            return intArrayOf(value.toInt())

        if (value is IntArray)
            return value.copyOf()

        if (value is Array<*>) {

            val result = IntArray(value.size)

            repeat(result.size) { i ->
                result[i] = (value[i] as Number).toInt()
            }

            return result
        }

        if (value is ShortArray) {

            val result = IntArray(value.size)

            repeat(result.size) { index ->
                result[index] = 0xFFFF and value[index].toInt()
            }

            return result
        }

        throw ImageReadException("Unknown value: $value for ${tagInfo.tagFormatted}")
    }

    fun toInt(): Int = (value as Number).toInt()

    fun toDouble(): Double =
        if (value is RationalNumber)
            value.doubleValue()
        else
            (value as Number).toDouble()

    /*
     * Note that we need to show the local 'tagFormatted', because
     * 'tagInfo' might be an Unknown tag and show a placeholder.
     */
    override fun toString(): String =
        "$offsetFormatted $tagFormatted ${tagInfo.name} = $valueDescription"

    fun createOversizeValueElement(): TiffElement? =
        valueOffset?.let { OversizeValueElement(it.toInt(), valueBytes.size) }

    inner class OversizeValueElement(offset: Int, length: Int) : TiffElement(
        debugDescription = "Value of $tagInfo ($fieldType) @ $offset",
        offset = offset,
        length = length
    ) {

        override fun toString(): String =
            debugDescription
    }

    companion object {

        private const val MAX_BYTE_ARRAY_DISPLAY_SIZE = 10
    }
}
