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
package com.ashampoo.kim.format.tiff.taginfos

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toDouble
import com.ashampoo.kim.format.tiff.constants.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType

class TagInfoDouble(
    name: String,
    tag: Int,
    directoryType: TiffDirectoryType?
) : TagInfo(name, tag, FieldType.DOUBLE, 1, directoryType) {

    fun getValue(byteOrder: ByteOrder, bytes: ByteArray): Double =
        bytes.toDouble(byteOrder)

    fun encodeValue(byteOrder: ByteOrder, value: Double): ByteArray =
        value.toBytes(byteOrder)
}
