/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeLong

internal class TiffOffsetItems(val byteOrder: ByteOrder) {

    private val offsetItems = mutableSetOf<TiffOffsetItem>()

    fun addOffsetItem(offsetItem: TiffOffsetItem) =
        offsetItems.add(offsetItem)

    fun writeOffsetsToOutputFields() =
        offsetItems.forEach {
            it.outputField.setBytes(
                FieldTypeLong.writeData(
                    data = it.outputItem.offset,
                    byteOrder = byteOrder
                )
            )
        }
}
