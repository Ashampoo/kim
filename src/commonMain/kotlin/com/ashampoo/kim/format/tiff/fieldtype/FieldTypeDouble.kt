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
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toDoubles
import com.ashampoo.kim.format.tiff.constant.TiffConstants

/**
 * Double precision (8-byte) IEEE format.
 */
object FieldTypeDouble : FieldType<DoubleArray> {

    override val type: Int = TiffConstants.FIELD_TYPE_DOUBLE_INDEX

    override val name: String = "Double"

    override val size: Int = 8

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): DoubleArray =
        bytes.toDoubles(byteOrder)

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Double -> data.toBytes(byteOrder)
            is DoubleArray -> data.toBytes(byteOrder)
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
