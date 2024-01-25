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
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.RationalNumber
import com.ashampoo.kim.common.RationalNumber.Companion.valueOf
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toRational
import com.ashampoo.kim.common.toRationals
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constant.TiffConstants.FIELD_TYPE_SRATIONAL_INDEX

/**
 * Two SLONG’s: the first represents the numerator of a
 * fraction, the second the denominator.
 */
object FieldTypeSRational :
    FieldType(FIELD_TYPE_SRATIONAL_INDEX, "SRational", 8) {

    override fun getValue(entry: TiffField): Any {

        val bytes = entry.byteArrayValue

        val unsignedType = entry.fieldType !== SRATIONAL

        return if (entry.count == 1)
            bytes.toRational(entry.byteOrder, unsignedType)
        else
            bytes.toRationals(entry.byteOrder, unsignedType)
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data is RationalNumber)
            return data.toBytes(byteOrder)

        if (data is Number)
            return valueOf(data.toDouble()).toBytes(byteOrder)

        if (data is Array<*>)
            return (data as Array<RationalNumber>).toBytes(byteOrder)

        // FIXME Can this happen?
//        if (o is Array<*> && o.isArrayOf<Number>()) {
//
//            val rationalNumbers = arrayOfNulls<RationalNumber>(o.size)
//
//            repeat(rationalNumbers.size) { i ->
//                rationalNumbers[i] = valueOf(i.toDouble())
//            }
//
//            return toBytes(rationalNumbers as Array<RationalNumber>, byteOrder)
//        }

        if (data !is DoubleArray)
            throw ImageWriteException("Invalid data: $data")

        val rationalNumbers = arrayOfNulls<RationalNumber>(data.size)

        repeat(rationalNumbers.size) { i ->
            rationalNumbers[i] = valueOf(i.toDouble())
        }

        return (rationalNumbers as Array<RationalNumber>).toBytes(byteOrder)
    }
}
