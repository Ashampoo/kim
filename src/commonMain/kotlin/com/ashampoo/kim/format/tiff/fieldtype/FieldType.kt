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
package com.ashampoo.kim.format.tiff.fieldtype

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_ASCII_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_BYTE_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_DOUBLE_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_FLOAT_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_IFD_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_LONG_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_RATIONAL_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SBYTE_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SHORT_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SLONG_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SRATIONAL_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SSHORT_INDEX
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_UNDEFINED_INDEX

interface FieldType<T> {

    val type: Int

    val name: String

    val size: Int

    fun getValue(bytes: ByteArray, byteOrder: ByteOrder): T

    fun writeData(data: Any, byteOrder: ByteOrder): ByteArray

    companion object {

        @kotlin.jvm.JvmStatic
        fun getFieldType(type: Int): FieldType<out Any> =
            when (type) {
                FIELD_TYPE_BYTE_INDEX -> FieldTypeByte
                FIELD_TYPE_ASCII_INDEX -> FieldTypeAscii
                FIELD_TYPE_SHORT_INDEX -> FieldTypeShort
                FIELD_TYPE_LONG_INDEX -> FieldTypeLong
                FIELD_TYPE_RATIONAL_INDEX -> FieldTypeRational
                FIELD_TYPE_SBYTE_INDEX -> FieldTypeSByte
                FIELD_TYPE_UNDEFINED_INDEX -> FieldTypeUndefined
                FIELD_TYPE_SSHORT_INDEX -> FieldTypeSShort
                FIELD_TYPE_SLONG_INDEX -> FieldTypeSLong
                FIELD_TYPE_SRATIONAL_INDEX -> FieldTypeSRational
                FIELD_TYPE_FLOAT_INDEX -> FieldTypeFloat
                FIELD_TYPE_DOUBLE_INDEX -> FieldTypeDouble
                FIELD_TYPE_IFD_INDEX -> FieldTypeIfd
                else -> throw ImageReadException("Unknown field type $type")
            }
    }
}
