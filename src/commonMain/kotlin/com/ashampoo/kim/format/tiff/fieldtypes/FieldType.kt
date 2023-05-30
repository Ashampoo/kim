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
package com.ashampoo.kim.format.tiff.fieldtypes

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.tiff.TiffField

abstract class FieldType protected constructor(
    val type: Int,
    val name: String,
    val size: Int
) {

    abstract fun getValue(entry: TiffField): Any

    abstract fun writeData(data: Any, byteOrder: ByteOrder): ByteArray

    companion object {

        val BYTE = FieldTypeByte(1, "Byte")
        val ASCII = FieldTypeAscii(2, "ASCII")
        val SHORT = FieldTypeShort(3, "Short")
        val LONG = FieldTypeLong(4, "Long")
        val RATIONAL = FieldTypeRational(5, "Rational")
        val SBYTE = FieldTypeByte(6, "SByte")
        val UNDEFINED = FieldTypeByte(7, "Undefined")
        val SSHORT = FieldTypeShort(8, "SShort")
        val SLONG = FieldTypeLong(9, "SLong")
        val SRATIONAL = FieldTypeRational(10, "SRational")
        val FLOAT = FieldTypeFloat(11, "Float")
        val DOUBLE = FieldTypeDouble(12, "Double")
        val IFD = FieldTypeLong(13, "IFD")

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
        fun getFieldType(type: Int): FieldType {

            for (fieldType in ANY)
                if (fieldType.type == type)
                    return fieldType

            throw ImageReadException("Field type $type is unsupported")
        }
    }
}
