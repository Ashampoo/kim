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
package com.ashampoo.kim.format.heic

import com.ashampoo.kim.common.ByteOrder

object HeicConstants {

    val HEIC_BYTE_ORDER = ByteOrder.BIG_ENDIAN

    /* BoxType must be always 4 bytes */
    const val TPYE_LENGTH = 4

    /* 4 length bytes + 4 type bytes */
    const val BOX_HEADER_LENGTH = 8

}
