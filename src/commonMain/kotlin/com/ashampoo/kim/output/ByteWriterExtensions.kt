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
package com.ashampoo.kim.output

import com.ashampoo.kim.common.ByteOrder

/*
 * For easier implementation of the [ByteWriter] in
 * Java these functions are designed as extension functions.
 */

@Suppress("MagicNumber")
internal fun ByteWriter.writeInt(
    value: Int,
    byteOrder: ByteOrder
) {

    if (byteOrder == ByteOrder.BIG_ENDIAN) {

        write(0xFF and (value shr 24))
        write(0xFF and (value shr 16))
        write(0xFF and (value shr 8))
        write(0xFF and (value shr 0))

    } else {

        write(0xFF and (value shr 0))
        write(0xFF and (value shr 8))
        write(0xFF and (value shr 16))
        write(0xFF and (value shr 24))
    }
}

@Suppress("MagicNumber")
internal fun ByteWriter.writeLong(
    value: Long,
    byteOrder: ByteOrder
) {
    if (byteOrder == ByteOrder.BIG_ENDIAN) {

        write(0xFF and (value shr 56).toInt())
        write(0xFF and (value shr 48).toInt())
        write(0xFF and (value shr 40).toInt())
        write(0xFF and (value shr 32).toInt())
        write(0xFF and (value shr 24).toInt())
        write(0xFF and (value shr 16).toInt())
        write(0xFF and (value shr 8).toInt())
        write(0xFF and value.toInt())

    } else {

        write(0xFF and value.toInt())
        write(0xFF and (value shr 8).toInt())
        write(0xFF and (value shr 16).toInt())
        write(0xFF and (value shr 24).toInt())
        write(0xFF and (value shr 32).toInt())
        write(0xFF and (value shr 40).toInt())
        write(0xFF and (value shr 48).toInt())
        write(0xFF and (value shr 56).toInt())
    }
}
