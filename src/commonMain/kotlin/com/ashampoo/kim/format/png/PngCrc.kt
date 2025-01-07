/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.png

@Suppress("MagicNumber")
internal object PngCrc {

    private const val CRC_TABLE_SIZE = 256

    private val crcTable = LongArray(CRC_TABLE_SIZE)

    init {

        /*
         * Compute CRC table
         */

        repeat(CRC_TABLE_SIZE) { index ->

            var crc = index.toLong()

            repeat(8) {

                crc = if (crc and 1L != 0L)
                    0xedb88320L xor (crc shr 1)
                else
                    crc shr 1
            }

            crcTable[index] = crc
        }
    }

    private fun updateCrc(crc: Long, buf: ByteArray): Long {

        var index = 0

        var newCrc = crc

        while (index < buf.size) {
            newCrc = crcTable[(newCrc xor buf[index].toLong() and 0xFFL).toInt()] xor (newCrc shr 8)
            index++
        }

        return newCrc
    }

    @kotlin.jvm.JvmStatic
    fun startPartialCrc(buf: ByteArray): Long =
        updateCrc(0xFFFFFFFFL, buf)

    @kotlin.jvm.JvmStatic
    fun continuePartialCrc(oldCrc: Long, buf: ByteArray): Long =
        updateCrc(oldCrc, buf)

    @kotlin.jvm.JvmStatic
    fun finishPartialCrc(oldCrc: Long): Long =
        oldCrc xor 0xFFFFFFFFL
}
