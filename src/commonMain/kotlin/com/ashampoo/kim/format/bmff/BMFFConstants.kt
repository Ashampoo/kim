/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.bmff

import com.ashampoo.kim.common.ByteOrder

internal object BMFFConstants {

    val BMFF_BYTE_ORDER = ByteOrder.BIG_ENDIAN

    /** BoxType must be always 4 bytes */
    const val TPYE_LENGTH = 4

    /** The size is presented as unsinged integer */
    const val SIZE_LENGTH = 4

    /** 4 size bytes + 4 type bytes */
    const val BOX_HEADER_LENGTH = TPYE_LENGTH + SIZE_LENGTH

    const val TIFF_HEADER_OFFSET_BYTE_COUNT = 4

    const val ITEM_TYPE_EXIF = 1_165_519_206
    const val ITEM_TYPE_MIME = 1_835_625_829
    const val ITEM_TYPE_JPEG = 1_785_750_887
}
