/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
 * Copyright 2002-2019 Drew Noakes and contributors
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
package com.ashampoo.kim.format.heic.boxes

import com.ashampoo.kim.format.heic.BoxReader
import com.ashampoo.kim.format.heic.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

class ItemPropertyContainerBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.IPCO, length, payload) {

    val boxes: List<Box>

    override fun toString(): String =
        "IPCO boxes=${boxes.map { it.type }}"

    init {

        val byteReader = ByteArrayByteReader(payload)

        boxes = BoxReader.readBoxes(byteReader)
    }
}
