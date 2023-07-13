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
@file:Suppress("MagicNumber", "TooManyFunctions")

package com.ashampoo.kim.common

private const val FF = 0xFF

const val HEX_RADIX = 16

fun Byte.toHex(): String =
    this.toInt().and(FF).toString(HEX_RADIX).padStart(2, '0')

fun convertHexStringToByteArray(string: String): ByteArray =
    string
        .chunked(2)
        .map { it.toInt(HEX_RADIX).toByte() }
        .toByteArray()

@Suppress("MagicNumber")
fun ByteArray.toHex(): String =
    joinToString("") { it.toHex() }

@Suppress("MagicNumber")
fun ByteArray.toSingleNumberHexes(): String =
    joinToString(", ") { "0x" + it.toHex() }

@Suppress("MagicNumber")
fun ByteArray.toAsciiString(): String =
    this.decodeToString()

fun ByteArray.indexOfNullTerminator(): Int =
    indexOfNullTerminator(0)

/**
 * NUL is often used in image formats to terminate a string.
 */
fun ByteArray.indexOfNullTerminator(start: Int): Int {

    for (i in start until size)
        if (this[i].toInt() == 0)
            return i

    return -1
}

fun ByteArray.startsWith(bytes: ByteArray): Boolean {

    if (bytes.size > size)
        return false

    for (index in bytes.indices)
        if (this[index] != bytes[index])
            return false

    return true
}

fun ByteArray.startsWith(bytes: List<Byte>): Boolean {

    if (bytes.size > size)
        return false

    for (index in bytes.indices)
        if (this[index] != bytes[index])
            return false

    return true
}

fun ByteArray.startsWithNullable(bytes: List<Byte?>): Boolean {

    if (bytes.size > size)
        return false

    for (index in bytes.indices)
        if (bytes[index] != null && this[index] != bytes[index])
            return false

    return true
}

fun ByteArray.getRemainingBytes(startIndex: Int): ByteArray {
    val actualStartIndex = startIndex.coerceIn(indices)
    return sliceArray(actualStartIndex until size)
}

fun ByteArray.slice(startIndex: Int, count: Int): ByteArray {
    val endIndex = (startIndex + count).coerceAtMost(size)
    return sliceArray(startIndex until endIndex)
}

fun ByteArray.head(endIndex: Int): ByteArray {
    val actualEndIndex = endIndex.coerceAtMost(size)
    return sliceArray(0 until actualEndIndex)
}

fun ByteArray.isEquals(start: Int, other: ByteArray, otherStart: Int, length: Int): Boolean {

    if (size < start + length)
        return false

    if (other.size < otherStart + length)
        return false

    for (index in 0 until length)
        if (this[start + index] != other[otherStart + index])
            return false

    return true
}
