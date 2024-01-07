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
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.tiff.TiffField

class FieldTypeByte(type: Int, name: String) : FieldType(type, name, 1) {

    override fun getValue(entry: TiffField): Any {

        val bytes = entry.byteArrayValue

        return if (entry.count == 1)
            bytes[0]
        else
            bytes
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data is Byte)
            return byteArrayOf(data)

        if (data is ByteArray)
            return data

        throw ImageWriteException("Invalid data: $data")
    }
}
