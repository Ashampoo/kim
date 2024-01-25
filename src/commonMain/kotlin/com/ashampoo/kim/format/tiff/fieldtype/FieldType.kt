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
import com.ashampoo.kim.format.tiff.TiffField

interface FieldType<T> {

    val type: Int

    val name: String

    val size: Int

    fun getValue(entry: TiffField): T

    // FIXME Try to replace "Any" with "T"
    fun writeData(data: Any, byteOrder: ByteOrder): ByteArray

    companion object {

        val BYTE = FieldTypeByte
        val ASCII = FieldTypeAscii
        val SHORT = FieldTypeShort
        val LONG = FieldTypeLong
        val RATIONAL = FieldTypeRational
        val SBYTE = FieldTypeSByte
        val UNDEFINED = FieldTypeUndefined
        val SSHORT = FieldTypeSShort
        val SLONG = FieldTypeSLong
        val SRATIONAL = FieldTypeSRational
        val FLOAT = FieldTypeFloat
        val DOUBLE = FieldTypeDouble
        val IFD = FieldTypeIfd

        val ANY = listOf(
            BYTE, ASCII, SHORT, LONG, RATIONAL, SBYTE, UNDEFINED,
            SSHORT, SLONG, SRATIONAL, FLOAT, DOUBLE, IFD
        )

        val SHORT_OR_LONG = listOf(SHORT, LONG)
        val SHORT_OR_RATIONAL = listOf(SHORT, RATIONAL)
        val SHORT_OR_LONG_OR_RATIONAL = listOf(SHORT, LONG, RATIONAL)
        val LONG_OR_SHORT = listOf(SHORT, LONG)
        val BYTE_OR_SHORT = listOf(SHORT, BYTE)
        val LONG_OR_IFD = listOf(LONG, IFD)
        val ASCII_OR_RATIONAL = listOf(ASCII, RATIONAL)
        val ASCII_OR_BYTE = listOf(ASCII, BYTE)

        @kotlin.jvm.JvmStatic
        fun getFieldType(type: Int): FieldType<out Any> {

            for (fieldType in ANY)
                if (fieldType.type == type)
                    return fieldType

            throw ImageReadException("Field type $type is unsupported")
        }
    }
}
