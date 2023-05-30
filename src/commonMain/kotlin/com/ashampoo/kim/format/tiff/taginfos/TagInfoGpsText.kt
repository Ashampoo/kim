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
import com.ashampoo.kim.common.isEquals
import com.ashampoo.kim.format.tiff.TiffField
import com.ashampoo.kim.format.tiff.constants.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import io.ktor.utils.io.charsets.Charset
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

        val result = ByteArray(asciiBytes.size + TEXT_ENCODING_ASCII.prefix.size)

        TEXT_ENCODING_ASCII.prefix.copyInto(
            destination = result,
            destinationOffset = 0,
            endIndex = TEXT_ENCODING_ASCII.prefix.size
        )

        asciiBytes.copyInto(
            destination = result,
            destinationOffset = TEXT_ENCODING_ASCII.prefix.size,
            endIndex = asciiBytes.size
        )

        return result
    }

    override fun getValue(entry: TiffField): String {

        val fieldType = entry.fieldType

        if (fieldType === FieldType.ASCII) {

            val value = FieldType.ASCII.getValue(entry)

            if (value is String)
                return value

            /*
             * Use of arrays with the ASCII type should be extremely rare, and use of
             * ASCII type in GPS fields should be forbidden. So assume the 2 never happen
             * together and return incomplete strings if they do.
             */
            if (value is List<*>)
                return value[0] as String

            throw ImageReadException("Unexpected ASCII type decoded")
        }

        if (fieldType === FieldType.UNDEFINED) {
            /* TODO Handle */
        } else if (fieldType === FieldType.BYTE) {
            /* TODO Handle */
        } else
            throw ImageReadException("GPS text field not encoded as bytes.")

        val bytes = entry.byteArrayValue

        /* Try ASCII with NO prefix. */
        if (bytes.size < 8)
            return String(bytes, charset = Charsets.ISO_8859_1)

        for (encoding in TEXT_ENCODINGS) {

            if (bytes.isEquals(0, encoding.prefix, 0, encoding.prefix.size)) {

                if (!Charset.isSupported(encoding.encodingName))
                    throw ImageWriteException("No support for charset ${encoding.encodingName}")

                val charset = Charset.forName(encoding.encodingName)

                val decodedString = String(
                    bytes,
                    encoding.prefix.size,
                    bytes.size - encoding.prefix.size,
                    charset
                )

                val reEncodedBytes = decodedString.toByteArray(charset)

                val bytesEqual = bytes.isEquals(
                    encoding.prefix.size,
                    reEncodedBytes,
                    0,
                    reEncodedBytes.size
                )

                if (bytesEqual)
                    return decodedString
            }
        }

        return String(bytes, charset = Charsets.ISO_8859_1)
    }

    companion object {

        /*
         * This byte sequence is for US-ASCII, but that's not supported
         * in Ktor IO. Therefore we use ISO-8859-1 as a replacement.
         */
        private val TEXT_ENCODING_ASCII = TextEncoding(
            byteArrayOf(0x41, 0x53, 0x43, 0x49, 0x49, 0x00, 0x00, 0x00),
            "ISO-8859-1"
        )

        // Undefined
        // Try to interpret an undefined text as ISO-8859-1 (Latin)
        private val TEXT_ENCODING_UNDEFINED = TextEncoding(
            byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00),
            "ISO-8859-1"
        )

        private val TEXT_ENCODINGS = listOf(TEXT_ENCODING_ASCII, TEXT_ENCODING_UNDEFINED)
    }
}
