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

import com.ashampoo.kim.format.bmff.BMFFConstants.BOX_HEADER_LENGTH
import com.ashampoo.kim.format.bmff.BoxType

public open class Box(
    public val type: BoxType,
    public val offset: Long,
    public val size: Long,
    public val largeSize: Long?,
    /** Payload bytes, not including type & length bytes */
    public val payload: ByteArray
) {

    /*
     * "size" is an integer that specifies the number of bytes in this box,
     * including all its fields and contained boxes; if size is 1 then the
     * actual size is in the field largesize; if size is 0, then this
     * box is the last one in the file, and its contents extend to the
     * end of the file (normally only used for a Media Data Box)
     */
    public val actualLength: Long =
        when (size) {
            0L -> BOX_HEADER_LENGTH.toLong() + payload.size
            1L -> largeSize!!
            else -> size
        }

    override fun toString(): String =
        "Box '$type' @ $offset ($actualLength bytes)"
}
