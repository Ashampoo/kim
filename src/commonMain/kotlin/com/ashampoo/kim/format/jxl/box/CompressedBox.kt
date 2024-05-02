/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jxl.box

import com.ashampoo.kim.format.bmff.BMFFConstants
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.box.Box

/**
 * JPEG XL brob box for brotli compressed Exif or XMP
 */
public class CompressedBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.BROB, offset, size, largeSize, payload) {

    val actualType: BoxType

    init {

        actualType = BoxType.of(payload.take(BMFFConstants.TPYE_LENGTH).toByteArray())
    }
}
