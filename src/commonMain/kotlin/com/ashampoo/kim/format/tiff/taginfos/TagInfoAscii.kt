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
import com.ashampoo.kim.format.tiff.constants.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.String

class TagInfoAscii(
    name: String,
    tag: Int,
    length: Int,
    directoryType: TiffDirectoryType?
) : TagInfo(name, tag, FieldType.ASCII, length, directoryType) {

    fun getValue(bytes: ByteArray): List<String> {

        val strings = mutableListOf<String>()

        var nextStringPos = 0

        /*
         * TIFF ASCII fields are specified as UTF-8 strings
         *
         * Most fields are only one String, but some fields like
         * User Comment (0x9286) are often having multiple strings.
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

        /*
         * If the string wasn't null terminated (which means a broken file),
         * we still try to handle this here.
         */
        if (nextStringPos < bytes.size) {

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

        return strings
    }

    fun encodeValue(byteOrder: ByteOrder, values: List<String>): ByteArray =
        FieldType.ASCII.writeData(values, byteOrder)
}
