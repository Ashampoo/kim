/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.common.toFourCCTypeString
import com.ashampoo.kim.format.heic.BoxType
import com.ashampoo.kim.format.heic.HeicConstants
import com.ashampoo.kim.input.ByteArrayByteReader

class FileTypeBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.FTYP, length, payload) {

    val majorBrand: String

    val minorBrand: String

    val compatibleBrands: List<String>

    override fun toString(): String =
        "FTYP major=$majorBrand minor=$minorBrand compatible=$compatibleBrands"

    init {

        val byteReader = ByteArrayByteReader(payload)

        majorBrand = byteReader
            .read4BytesAsInt("majorBrand", HeicConstants.HEIC_BYTE_ORDER)
            .toFourCCTypeString()

        minorBrand = byteReader
            .read4BytesAsInt("minorBrand", HeicConstants.HEIC_BYTE_ORDER)
            .toFourCCTypeString()

        val brandCount: Int = (length.toInt() - 8 - 8) / 4

        val brands = mutableListOf<String>()

        repeat(brandCount) {
            brands.add(
                byteReader
                    .read4BytesAsInt("brand $it", HeicConstants.HEIC_BYTE_ORDER)
                    .toFourCCTypeString()
            )
        }

        compatibleBrands = brands
    }
}
