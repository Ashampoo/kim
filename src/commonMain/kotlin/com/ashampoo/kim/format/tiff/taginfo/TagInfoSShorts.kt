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
package com.ashampoo.kim.format.tiff.taginfo

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.common.toShorts
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtype.FieldType

class TagInfoSShorts(name: String, tag: Int, length: Int, directoryType: TiffDirectoryType?) :
    TagInfo(name, tag, FieldType.SSHORT, length, directoryType) {

    fun getValue(byteOrder: ByteOrder, bytes: ByteArray): ShortArray =
        bytes.toShorts(byteOrder)

    fun encodeValue(byteOrder: ByteOrder, value: ShortArray): ByteArray =
        value.toBytes(byteOrder)
}
