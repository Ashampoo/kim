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
package com.ashampoo.kim.format.bmff.boxes

import com.ashampoo.kim.common.toFourCCTypeString
import com.ashampoo.kim.format.bmff.BMFFConstants
import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.input.ByteArrayByteReader

/**
 * EIC/ISO 14496-12 ftyp box
 */
class FileTypeBox(
    offset: Long,
    length: Long,
    payload: ByteArray
) : Box(offset, BoxType.FTYP, length, payload) {

    val majorBrand: String

    val minorBrand: String

    val compatibleBrands: List<String>

    init {

        val byteReader = ByteArrayByteReader(payload)

        majorBrand = byteReader
            .read4BytesAsInt("majorBrand", BMFF_BYTE_ORDER)
            .toFourCCTypeString()

        minorBrand = byteReader
            .read4BytesAsInt("minorBrand", BMFF_BYTE_ORDER)
            .toFourCCTypeString()

        val brandCount: Int = (length.toInt() - 8 - 8) / 4

        val brands = mutableListOf<String>()

        repeat(brandCount) {
            brands.add(
                byteReader
                    .read4BytesAsInt("brand $it", BMFFConstants.BMFF_BYTE_ORDER)
                    .toFourCCTypeString()
            )
        }

        compatibleBrands = brands
    }

    override fun toString(): String =
        "$type major=$majorBrand minor=$minorBrand compatible=$compatibleBrands"

    companion object {
        const val JXL_BRAND = "jxl "
    }
}
