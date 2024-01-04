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

import com.ashampoo.kim.format.heic.BoxType

open class Box(
    val offset: Long,
    val type: BoxType,
    val length: Long,
    /* Payload bytes, not including type & length bytes */
    val payload: ByteArray
) {

    override fun toString(): String =
        "Box $type @ $offset of length $length"
}
