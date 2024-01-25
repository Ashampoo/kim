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
import com.ashampoo.kim.common.toShorts
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constant.TiffConstants

/**
 * 16-bit (2-byte) unsigned integer.
 *
 * FIXME Should be an IntArray
 */
data object FieldTypeShort : FieldType<ShortArray> {

    override val type: Int = TiffConstants.FIELD_TYPE_SHORT_INDEX

    override val name: String = "Short"

    override val size: Int = 2

    override fun getValue(entry: TiffField): ShortArray =
        entry.byteArrayValue.toShorts(entry.byteOrder)

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray =
        when (data) {
            is Short -> data.toBytes(byteOrder)
            is ShortArray -> data.toBytes(byteOrder)
            else -> throw ImageWriteException("Unsupported type: $data")
        }
}
