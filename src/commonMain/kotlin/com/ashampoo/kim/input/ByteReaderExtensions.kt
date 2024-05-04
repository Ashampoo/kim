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
@file:Suppress("TooManyFunctions")

package com.ashampoo.kim.input

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.quadsToByteArray
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.output.ByteArrayByteWriter

/*
 * For easier implementation of the [ByteReader] in
 * Java these functions are designed as extension functions.
 */

internal fun ByteReader.readByte(fieldName: String): Byte =
    readByte() ?: throw ImageReadException("Couldn't read byte for $fieldName")

internal fun ByteReader.readBytes(fieldName: String, count: Int): ByteArray {

    if (count < 0)
        throw ImageReadException("Couldn't read $fieldName, invalid length: $count")

    val bytes = readBytes(count)

    if (bytes.size != count)
        throw ImageReadException("Couldn't read $count bytes for $fieldName. Got only ${bytes.size}.")

    return bytes
}

internal fun ByteReader.readNullTerminatedString(fieldName: String): String {

    val bytes = mutableListOf<Byte>()

    var byte: Byte?

    while (true) {

        byte = readByte()

        if (byte == null)
            throw ImageReadException("No bytes for $fieldName, never reached terminator byte.")

        if (byte.toInt() == 0)
            break

        bytes.add(byte)
    }

    return bytes.toByteArray().decodeToString()
}

/** Reads one byte as unsigned number, also known as "byte" or "UInt8" */
internal fun ByteReader.readByteAsInt(): Int =
    readByte()?.let { it.toInt() and 0xFF } ?: -1

/** Reads 2 bytes as unsigned number, also known as "short" or "UInt16" */
internal fun ByteReader.read2BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()

    if (byte0 or byte1 < 0)
        throw ImageReadException("Couldn't read two bytes for $fieldName")

    return if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0 shl 8 or byte1
    else
        byte1 shl 8 or byte0
}

/** Reads 4 bytes as unsigned number, also known as "int" or "UInt32" */
internal fun ByteReader.read4BytesAsInt(fieldName: String, byteOrder: ByteOrder): Int {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()
    val byte2 = readByteAsInt()
    val byte3 = readByteAsInt()

    if (byte0 or byte1 or byte2 or byte3 < 0)
        throw ImageReadException("Couldn't read 4 bytes for $fieldName")

    val result: Int = if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0 shl 24 or (byte1 shl 16) or (byte2 shl 8) or (byte3 shl 0)
    else
        byte3 shl 24 or (byte2 shl 16) or (byte1 shl 8) or (byte0 shl 0)

    return result
}

/** Reads 8 bytes as unsigned number, also known as "long" or "UInt64" */
internal fun ByteReader.read8BytesAsLong(fieldName: String, byteOrder: ByteOrder): Long {

    val byte0 = readByteAsInt()
    val byte1 = readByteAsInt()
    val byte2 = readByteAsInt()
    val byte3 = readByteAsInt()
    val byte4 = readByteAsInt()
    val byte5 = readByteAsInt()
    val byte6 = readByteAsInt()
    val byte7 = readByteAsInt()

    if (byte0 or byte1 or byte2 or byte3 or byte4 or byte5 or byte6 or byte7 < 0)
        throw ImageReadException("Couldn't read 8 bytes for $fieldName")

    val result: Long = if (byteOrder == ByteOrder.BIG_ENDIAN)
        byte0.toLong() shl 56 or (byte1.toLong() shl 48) or (byte2.toLong() shl 40) or (byte3.toLong() shl 32) or
            (byte4.toLong() shl 24) or (byte5.toLong() shl 16) or (byte6.toLong() shl 8) or (byte7.toLong() shl 0)
    else
        byte7.toLong() shl 56 or (byte6.toLong() shl 48) or (byte5.toLong() shl 40) or (byte4.toLong() shl 32) or
            (byte3.toLong() shl 24) or (byte2.toLong() shl 16) or (byte1.toLong() shl 8) or (byte0.toLong() shl 0)

    return result
}

internal fun ByteReader.readXBytesAtInt(fieldName: String, count: Int, byteOrder: ByteOrder): Long =
    when (count) {
        1 -> readByteAsInt().toLong()
        2 -> read2BytesAsInt(fieldName, byteOrder).toLong()
        4 -> read4BytesAsInt(fieldName, byteOrder).toLong()
        8 -> read8BytesAsLong(fieldName, byteOrder)
        else -> error("Illegal byteCount specified: $count")
    }

internal fun ByteReader.readAndVerifyBytes(fieldName: String, expectedBytes: ByteArray) {

    for (index in expectedBytes.indices) {

        val byte = readByte()
            ?: throw ImageReadException("Unexpected EOF for $fieldName")

        if (byte != expectedBytes[index])
            throw ImageReadException("Byte $index is different by reading $fieldName: ${byte.toHex()}")
    }
}

internal fun ByteReader.readRemainingBytes(): ByteArray {

    val os = ByteArrayByteWriter()

    while (true) {

        val bytes = readBytes(DEFAULT_BUFFER_SIZE)

        if (bytes.isEmpty())
            break

        os.write(bytes)
    }

    return os.toByteArray()
}

internal fun ByteReader.skipBytes(fieldName: String, count: Int) {

    /* Nothing to do. */
    if (count == 0)
        return

    if (count < 0)
        throw ImageReadException("Couldn't read $fieldName, invalid length: $count")

    var total = 0

    while (count != total) {

        val skippedByteCount = readBytes(count).size

        if (skippedByteCount == 0) {

            val missingBytesCount = count - total

            throw ImageReadException(
                "Skipped $total bytes of $count for $fieldName: " +
                    "Missing $missingBytesCount bytes."
            )
        }

        total += skippedByteCount
    }
}

internal fun ByteReader.skipToQuad(quad: Int): Boolean {

    val needle = quad.quadsToByteArray()

    var position = 0

    while (true) {

        val byte = readByteAsInt()

        if (byte == -1)
            break

        if (needle[position].toInt() == byte) {

            position++

            if (position == needle.size) {
                return true
            }

        } else {

            position = 0
        }
    }

    return false
}
