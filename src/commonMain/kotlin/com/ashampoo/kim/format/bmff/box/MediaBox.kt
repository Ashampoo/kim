/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package com.ashampoo.kim.format.bmff.box

import com.ashampoo.kim.common.MetadataOffset
import com.ashampoo.kim.common.MetadataType
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BMFFConstants
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes

/**
 * EIC/ISO 14496-12 movie box
 *
 * The Movie Box is a container for several sub boxes.
 */
public class MediaBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.MDIA, offset, size, largeSize, payload), BoxContainer {

    override val boxes: List<Box>

    init {

        val byteReader = ByteArrayByteReader(payload)

        boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false,
            positionOffset = 4,
            offsetShift = offset + 8
        )
    }

    override fun toString(): String =
        "Box '$type' @$offset boxes=${boxes.map { it.type }}"
}
