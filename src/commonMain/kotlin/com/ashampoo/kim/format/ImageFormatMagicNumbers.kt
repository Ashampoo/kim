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
package com.ashampoo.kim.format

/**
 * See https://en.wikipedia.org/wiki/List_of_file_signatures
 *
 * Note that not all formats can be detected by a magic number.
 * For example HEIC has a broad number of different headers bytes.
 */
@Suppress("MagicNumber")
object ImageFormatMagicNumbers {

    val jpeg: List<Byte> = byteListOf(
        0xFF, 0xD8, 0xFF
    )

    val jpegShort: List<Byte> = byteListOf(
        0xFF, 0xD8
    )

    val gif87a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x37, 0x61
    )

    val gif89a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61
    )

    val png: List<Byte> = byteListOf(
        0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    val tiffLittleEndian: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00
    )

    val tiffBigEndian: List<Byte> = byteListOf(
        0x4D, 0x4D, 0x00, 0x2A
    )

    val cr2: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52
    )

    val raf: List<Byte> = byteListOf(
        0x46, 0x55, 0x4A, 0x49, 0x46, 0x49, 0x4C, 0x4D, 0x43, 0x43, 0x44, 0x2D, 0x52, 0x41, 0x57
    )

    val webP: List<Byte?> = byteListOf(
        0x52, 0x49, 0x46, 0x46, null, null, null, null, 0x57, 0x45, 0x42, 0x50
    )

    private fun byteListOf(vararg ints: Int?): List<Byte?> =
        ints.map { it?.toByte() }

    private fun byteListOf(vararg ints: Int): List<Byte> =
        ints.map { it.toByte() }
}
