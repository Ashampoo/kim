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
package com.ashampoo.kim.format.jpeg.segment

import com.ashampoo.kim.common.ByteOrder

abstract class Segment(val marker: Int, val length: Int) {

    /* Big endian is the most common byte order. */
    var byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN
        protected set

    abstract fun getDescription(): String

    override fun toString(): String =
        "[JPEG Segment " + getDescription() + "]"
}
