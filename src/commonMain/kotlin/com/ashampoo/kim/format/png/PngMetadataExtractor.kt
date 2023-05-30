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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.input.ByteReader

object PngMetadataExtractor {

    const val INT32_BYTE_SIZE: Int = 4

    /* Length of black pixel chunk data. */
    private val fakeImageDataChunkLength: List<Byte> = listOf(
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0C.toByte()
    )

    /* Only black pixels chunk data */
    private val fakeImageDataChunkData: List<Byte> = listOf(
        0x08.toByte(), 0xD7.toByte(), 0x63.toByte(), 0x60.toByte(), 0x60.toByte(), 0x60.toByte(),
        0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x04.toByte(), 0x00.toByte(), 0x01.toByte()
    )

    /* CRC of a black pixels chunk data */
    private val fakeImageDataChunkCrc: List<Byte> = listOf(
        0x27.toByte(), 0x34.toByte(), 0x27.toByte(), 0x0A.toByte()
    )

    /* The end marker chunk has a zero length. */
    private val imageEndChunkLength: List<Byte> = listOf(
        0.toByte(), 0.toByte(), 0.toByte(), 0.toByte()
    )

    private val imageEndChunkCrc: List<Byte> = listOf(
        0xAE.toByte(), 42.toByte(), 60.toByte(), 82.toByte()
    )

    @Suppress("ComplexMethod", "LoopWithTooManyJumpStatements")
    fun extractMetadataBytes(reader: ByteReader): ByteArray {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = reader.readBytes(ImageFormatMagicNumbers.png.size).toList()

        /* Ensure it's actually a PNG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.png) {
            "PNG magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        /*
         * A chunk has this structure:
         *
         * 4 bytes - length of chunk data (unsigned, but always within 31 bytes)
         * 4 bytes - chunk type, ASCII representation like "IHDR" or "IEND"
         * * bytes - chunk data, variable length (see first 4 bytes)
         * 4 bytes - CRC calculated from type and data
         *
         * Even the file start and end markers are chunks.
         */
        while (true) {

            val chunkDataLengthBytes = reader.readBytes(INT32_BYTE_SIZE)

            val chunkDataLength = chunkDataLengthBytes.toInt32()

            /* If the number is negative we have an integer overflow. */
            if (chunkDataLength < 0)
                throw ImageReadException("PNG chunk length exceeds maximum")

            val chunkTypeBytes = reader.readBytes(INT32_BYTE_SIZE)

            val chunkType = PngChunkType.get(chunkTypeBytes)

            /*
             * We replace the first image data chunk with a fake black pixel
             * chunk data and quit. Valid PNGs require one image data block.
             */
            if (chunkType == PngChunkType.IDAT) {

                bytes.addAll(fakeImageDataChunkLength)
                bytes.addAll(PngChunkType.IDAT.bytes.toList())
                bytes.addAll(fakeImageDataChunkData)
                bytes.addAll(fakeImageDataChunkCrc)

                break
            }

            /* Break if we reached the end marker. */
            if (chunkType == PngChunkType.IEND)
                break

            bytes.addAll(chunkDataLengthBytes.toList())
            bytes.addAll(chunkTypeBytes.toList())

            /* Chunk data. */
            reader.readAndAddBytes(bytes, chunkDataLength)

            /* CRC bytes. */
            reader.readAndAddBytes(bytes, INT32_BYTE_SIZE)
        }

        /* Write the end tag. */
        bytes.addAll(imageEndChunkLength)
        bytes.addAll(PngChunkType.IEND.bytes.toList())
        bytes.addAll(imageEndChunkCrc)

        return bytes.toByteArray()
    }

    fun extractExifBytes(reader: ByteReader): ByteArray? {

        val bytes = mutableListOf<Byte>()

        val magicNumberBytes = reader.readBytes(ImageFormatMagicNumbers.png.size).toList()

        /* Ensure it's actually a PNG. */
        require(magicNumberBytes == ImageFormatMagicNumbers.png) {
            "PNG magic number mismatch: ${magicNumberBytes.toByteArray().toSingleNumberHexes()}"
        }

        bytes.addAll(magicNumberBytes)

        /*
         * A chunk has this structure:
         *
         * 4 bytes - length of chunk data (unsigned, but always within 31 bytes)
         * 4 bytes - chunk type, ASCII representation like "IHDR" or "IEND"
         * * bytes - chunk data, variable length (see first 4 bytes)
         * 4 bytes - CRC calculated from type and data
         *
         * Even the file start and end markers are chunks.
         */
        while (true) {

            val chunkDataLength = reader.readBytes(INT32_BYTE_SIZE).toInt32()

            /* If the number is negative we have an integer overflow. */
            if (chunkDataLength < 0)
                throw ImageReadException("PNG chunk length exceeds maximum")

            val chunkTypeBytes = reader.readBytes(INT32_BYTE_SIZE)

            val chunkType = PngChunkType.get(chunkTypeBytes)

            /* Break if we reached the image data or end marker. */
            if (chunkType == PngChunkType.IDAT || chunkType == PngChunkType.IEND)
                return null

            val chunkBytes = reader.readBytes(chunkDataLength)

            if (chunkType == PngChunkType.EXIF)
                return chunkBytes

            reader.readBytes(INT32_BYTE_SIZE)
        }
    }

    private fun ByteReader.readAndAddBytes(
        byteList: MutableList<Byte>,
        count: Int
    ): ByteArray {
        val bytes = readBytes(count)
        byteList.addAll(bytes.toList())
        return bytes
    }

    @Suppress("EnumNaming", "MagicNumber")
    private enum class PngChunkType(
        val bytes: ByteArray
    ) {

        IHDR(byteArrayOf(0x49, 0x48, 0x44, 0x52)),
        PLTE(byteArrayOf(0x50, 0x4c, 0x54, 0x45)),
        IDAT(byteArrayOf(0x49, 0x44, 0x41, 0x54)),
        IEND(byteArrayOf(0x49, 0x45, 0x4e, 0x44)),
        CHRM(byteArrayOf(0x63, 0x48, 0x52, 0x4d)),
        GAMA(byteArrayOf(0x67, 0x41, 0x4d, 0x41)),
        ICCP(byteArrayOf(0x69, 0x43, 0x43, 0x50)),
        SBIT(byteArrayOf(0x73, 0x42, 0x49, 0x54)),
        SRGB(byteArrayOf(0x73, 0x52, 0x47, 0x42)),
        BKGD(byteArrayOf(0x62, 0x4b, 0x47, 0x44)),
        HIST(byteArrayOf(0x68, 0x49, 0x53, 0x54)),
        TRNS(byteArrayOf(0x74, 0x52, 0x4e, 0x53)),
        PHYS(byteArrayOf(0x70, 0x48, 0x59, 0x73)),
        SPLT(byteArrayOf(0x73, 0x50, 0x4c, 0x54)),
        TIME(byteArrayOf(0x74, 0x49, 0x4d, 0x45)),
        ITXT(byteArrayOf(0x69, 0x54, 0x58, 0x74)),
        TEXT(byteArrayOf(0x74, 0x45, 0x58, 0x74)),
        ZTXT(byteArrayOf(0x7a, 0x54, 0x58, 0x74)),
        EXIF(byteArrayOf(0x65, 0x58, 0x49, 0x66));

        companion object {

            fun get(bytes: ByteArray): PngChunkType? {

                for (type in PngChunkType.values())
                    if (bytes.contentEquals(type.bytes))
                        return type

                return null
            }
        }
    }

    private fun byteArrayOf(vararg ints: Int): ByteArray =
        ints.map { it.toByte() }.toByteArray()
}

@Suppress("MagicNumber")
fun ByteArray.toInt32(): Int =
    this[0].toInt() shl 24 and -0x1000000 or
        (this[1].toInt() shl 16 and 0xFF0000) or
        (this[2].toInt() shl 8 and 0xFF00) or
        (this[3].toInt() and 0xFF)
