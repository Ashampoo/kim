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
package com.ashampoo.kim.format.tiff.fieldtypes

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.format.tiff.TiffField
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.String
import io.ktor.utils.io.core.toByteArray

class FieldTypeAscii(type: Int, name: String) : FieldType(type, name, 1) {

    override fun getValue(entry: TiffField): Any {

        /*
         * According to EXIF specification:
         * "2 = ASCII An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL."
         *
         * Most fields are only one String, but some fields like
         * User Comment (0x9286) are often having multiple strings.
         */

        val bytes = entry.byteArrayValue

        val strings = mutableListOf<String>()

        var nextStringPos = 0

        /*
         * According to the Exiftool FAQ, http://www.metadataworkinggroup.org
         * specifies that the TIFF ASCII fields are actually UTF-8.
         * Exiftool however allows you to configure the charset used.
         */
        for (index in bytes.indices) {

            if (bytes[index].toInt() == 0) {

                val string = String(
                    bytes = bytes,
                    offset = nextStringPos,
                    length = index - nextStringPos,
                    charset = Charsets.UTF_8
                )

                /* Ignore blank strings and rewrite files without them. */
                if (string.isNotBlank())
                    strings.add(string)

                nextStringPos = index + 1
            }
        }

        if (nextStringPos < bytes.size) {

            /* Handle buggy files where the String is not terminated. */
            val string = String(
                bytes = bytes,
                offset = nextStringPos,
                length = bytes.size - nextStringPos,
                charset = Charsets.UTF_8
            )

            /* Ignore blank strings and rewrite files without them. */
            if (string.isNotBlank())
                strings.add(string)
        }

        return if (strings.size == 1)
            strings.first()
        else
            strings
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data is ByteArray) {
            val result = data.copyOf(data.size + 1)
            result[result.lastIndex] = 0
            return result
        }

        if (data is String) {
            val bytes = data.toByteArray()
            val result = bytes.copyOf(bytes.size + 1)
            result[result.lastIndex] = 0
            return result
        }

        val strings: List<String> = data as List<String>

        var totalLength = 0

        for (string in strings)
            totalLength += string.toByteArray().size + 1

        val result = ByteArray(totalLength)

        var position = 0

        for (string in strings) {

            val bytes = string.toByteArray()

            bytes.copyInto(
                destination = result,
                destinationOffset = position,
                endIndex = bytes.size
            )

            position += bytes.size + 1
        }

        return result
    }
}
