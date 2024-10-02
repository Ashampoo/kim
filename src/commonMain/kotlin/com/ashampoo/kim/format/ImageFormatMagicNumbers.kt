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
package com.ashampoo.kim.format

/**
 * See https://en.wikipedia.org/wiki/List_of_file_signatures
 *
 * Note that not all formats can be detected by a magic number.
 * For example HEIC has a broad number of different headers bytes.
 */
@Suppress("MagicNumber")
public object ImageFormatMagicNumbers {

    public val jpeg: List<Byte> = byteListOf(
        0xFF, 0xD8
    )

    public val gif87a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x37, 0x61
    )

    public val gif89a: List<Byte> = byteListOf(
        0x47, 0x49, 0x46, 0x38, 0x39, 0x61
    )

    public val png: List<Byte> = byteListOf(
        0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    )

    public val tiffLittleEndian: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00
    )

    /*
     * Note that NEF, ARW & DNG also starts this way.
     * We can't distinguish a normal TIFF and a raw file based on them.
     */
    public val tiffBigEndian: List<Byte> = byteListOf(
        0x4D, 0x4D, 0x00, 0x2A
    )

    public val cr2: List<Byte> = byteListOf(
        0x49, 0x49, 0x2A, 0x00, 0x10, 0x00, 0x00, 0x00, 0x43, 0x52
    )

    public val raf: List<Byte> = "FUJIFILMCCD-RAW ".encodeToByteArray().toList()

    public val rw2: List<Byte> = byteListOf(
        0x49, 0x49, 0x55, 0x00
    )

    public val orf_iiro: List<Byte> = byteListOf(
        0x49, 0x49, 0x52, 0x4F, 0x08, 0x00
    )

    public val orf_mmor: List<Byte> = byteListOf(
        0x4D, 0x4D, 0x4F, 0x52, 0x00, 0x00
    )

    public val orf_iirs: List<Byte> = byteListOf(
        0x49, 0x49, 0x52, 0x53, 0x08, 0x00
    )

    public val webP: List<Byte?> = byteListOf(
        0x52, 0x49, 0x46, 0x46, null, null, null, null, 0x57, 0x45, 0x42, 0x50
    )

    /* 4 bytes + "ftypheic" */
    public val heic: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypheic".encodeToByteArray().toList())

    /* A HEIC brand */
    public val mif1: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypmif1".encodeToByteArray().toList())

    /* A HEIC brand */
    public val msf1: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypmsf1".encodeToByteArray().toList())

    /* A HEIC brand */
    public val heix: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypheix".encodeToByteArray().toList())

    /* A HEIC brand */
    public val hevc: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftyphevc".encodeToByteArray().toList())

    /* A HEIC brand */
    public val hevx: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftyphevx".encodeToByteArray().toList())

    /* 4 bytes + "ftypavif" */
    public val avif: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypavif".encodeToByteArray().toList())

    /* 4 bytes + "ftypcrx" */
    public val cr3: List<Byte?> = byteListOf(
        null, null, null, null
    ).plus("ftypcrx".encodeToByteArray().toList())

    /* The regular ISOBMFF-based JPEG XL */
    public val jxl: List<Byte> = byteListOf(
        0x00, 0x00, 0x00, 0x0C, 0x4A, 0x58, 0x4C, 0x20, 0x0D, 0x0A, 0x87, 0x0A
    )

    /* The 'naked' code stream without metadata. */
    public val jxlCodeStream: List<Byte> = byteListOf(
        0xFF, 0x0A
    )

    private fun byteListOf(vararg ints: Int?): List<Byte?> =
        ints.map { it?.toByte() }

    private fun byteListOf(vararg ints: Int): List<Byte> =
        ints.map { it.toByte() }
}
