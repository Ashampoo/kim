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
import com.ashampoo.kim.common.RationalNumbers
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toRationals
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.fieldtype.FieldType.Companion.SRATIONAL

/**
 * Two LONGs: the first represents the numerator of a
 * fraction; the second, the denominator.
 */
object FieldTypeRational : FieldType<RationalNumbers> {

    override val type: Int = TiffConstants.FIELD_TYPE_RATIONAL_INDEX

    override val name: String = "Rational"

    override val size: Int = 8

    override fun getValue(entry: TiffField): RationalNumbers {

        val unsignedType = entry.fieldType !== SRATIONAL

        return entry.byteArrayValue.toRationals(entry.byteOrder, unsignedType)
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        (data as RationalNumbers).toBytes(byteOrder)

//        val rationalNumbers = arrayOfNulls<RationalNumber>(data.size)
//
//        repeat(rationalNumbers.size) { i ->
//            rationalNumbers[i] = valueOf(i.toDouble())
//        }
//
//        return (rationalNumbers as Array<RationalNumber>).toBytes(byteOrder)
}
