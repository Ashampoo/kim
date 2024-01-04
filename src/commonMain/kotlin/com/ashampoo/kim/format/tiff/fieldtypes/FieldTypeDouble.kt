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
package com.ashampoo.kim.format.tiff.fieldtypes

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toDouble
import com.ashampoo.kim.common.toDoubles
import com.ashampoo.kim.format.tiff.TiffField

class FieldTypeDouble(type: Int, name: String) : FieldType(type, name, 8) {

    override fun getValue(entry: TiffField): Any {

        val bytes = entry.byteArrayValue

        return if (entry.count == 1)
            bytes.toDouble(entry.byteOrder)
        else
            bytes.toDoubles(entry.byteOrder)
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data is Double)
            return data.toBytes(byteOrder)

        if (data is DoubleArray)
            return data.toBytes(byteOrder)

        val values = DoubleArray((data as Array<Double>).size)

        repeat(values.size) { i ->
            values[i] = data[i]
        }

        return values.toBytes(byteOrder)
    }
}
