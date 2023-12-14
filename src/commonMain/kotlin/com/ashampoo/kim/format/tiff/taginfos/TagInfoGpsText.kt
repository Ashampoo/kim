/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff.taginfos

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.decodeIso8859BytesToString
import com.ashampoo.kim.common.isEquals
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constants.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.String
import io.ktor.utils.io.core.toByteArray

/**
 * Used by some GPS tags and the EXIF user comment tag,
 * this badly documented value is meant to contain
 * the text encoding in the first 8 bytes followed by
 * the non-null-terminated text in an unknown byte order.
 */
class TagInfoGpsText(
    name: String,
    tag: Int,
    exifDirectory: TiffDirectoryType?
) : TagInfo(name, tag, FieldType.UNDEFINED, TagInfo.LENGTH_UNKNOWN, exifDirectory) {

    override fun isText(): Boolean =
        true

    private data class TextEncoding(val prefix: ByteArray, val encodingName: String)

    override fun encodeValue(fieldType: FieldType, value: Any, byteOrder: ByteOrder): ByteArray {

        if (value !is String)
            throw ImageWriteException("GPS text value not String: $value")

        val asciiBytes = value.toByteArray(Charsets.ISO_8859_1)

        val result = ByteArray(asciiBytes.size + TEXT_ENCODING_ASCII_BYTES.size)

        TEXT_ENCODING_ASCII_BYTES.copyInto(
            destination = result,
            destinationOffset = 0,
            endIndex = TEXT_ENCODING_ASCII_BYTES.size
        )

        asciiBytes.copyInto(
            destination = result,
            destinationOffset = TEXT_ENCODING_ASCII_BYTES.size,
            endIndex = asciiBytes.size
        )

        return result
    }

    override fun getValue(entry: TiffField): String {

        val fieldType = entry.fieldType

        if (fieldType === FieldType.ASCII)
            return FieldType.ASCII.getValue(entry)

        if (fieldType === FieldType.UNDEFINED) {
            /* TODO Handle */
        } else if (fieldType === FieldType.BYTE) {
            /* TODO Handle */
        } else {
            throw ImageReadException("GPS text field not encoded as bytes.")
        }

        val bytes = entry.byteArrayValue

        /* Try ASCII with NO prefix. */
        if (bytes.size < TEXT_ENCODING_BYTE_LENGTH)
            return bytes.decodeIso8859BytesToString()

        val encodingPrefixBytes = bytes.slice(
            startIndex = 0,
            count = TEXT_ENCODING_BYTE_LENGTH
        )

        val hasEncoding =
            encodingPrefixBytes.contentEquals(TEXT_ENCODING_ASCII_BYTES) ||
                encodingPrefixBytes.contentEquals(TEXT_ENCODING_UNDEFINED_BYTES)

        if (hasEncoding) {

            val decodedString = String(
                bytes = bytes,
                offset = 8,
                length = bytes.size - TEXT_ENCODING_BYTE_LENGTH,
                Charsets.ISO_8859_1
            )

            val reEncodedBytes = decodedString.toByteArray()

            val bytesEqual = bytes.isEquals(
                start = TEXT_ENCODING_BYTE_LENGTH,
                other = reEncodedBytes,
                otherStart = 0,
                length = reEncodedBytes.size
            )

            if (bytesEqual)
                return decodedString
        }

        return bytes.decodeIso8859BytesToString()
    }

    companion object {

        private val TEXT_ENCODING_BYTE_LENGTH = 8

        /**
         * Code for US-ASCII.
         *
         * This is a subset of ISO-8859-1 (Latin), so we can use that.
         */
        private val TEXT_ENCODING_ASCII_BYTES =
            byteArrayOf(0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00)

        /*
         * Undefined
         *
         * Try to interpret an undefined text as ISO-8859-1 (Latin)
         */
        private val TEXT_ENCODING_UNDEFINED_BYTES =
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
    }
}
