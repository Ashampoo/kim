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
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.indexOfNullTerminator
import com.ashampoo.kim.format.tiff.TiffField
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.String
import io.ktor.utils.io.core.toByteArray

class FieldTypeAscii(type: Int, name: String) : FieldType(type, name, 1) {

    override fun getValue(entry: TiffField): String {

        /*
         * According to EXIF specification:
         * "2 = ASCII An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL."
         *
         * Most fields are only one String, but some fields like
         * User Comment (0x9286) are sometimes having multiple strings.
         * We read it all as one String for simplicity.
         */

        val bytes = entry.byteArrayValue

        val nullTerminatorIndex = bytes.indexOfNullTerminator()

        val length = if (nullTerminatorIndex > -1)
            nullTerminatorIndex
        else
            bytes.size

        if (length == 0)
            return ""

        /*
         * According to the Exiftool FAQ, http://www.metadataworkinggroup.org
         * specifies that the TIFF ASCII fields are actually UTF-8.
         * Exiftool however allows you to configure the charset used.
         */
        val string = String(
            bytes = bytes,
            offset = 0,
            length = length,
            charset = Charsets.UTF_8
        )

        return string
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data is ByteArray) {
            val result = data.copyOf(data.size + 1)
            result[result.lastIndex] = 0
            return result
        }

        if (data is String) {
            val bytes = data.encodeToByteArray()
            val result = bytes.copyOf(bytes.size + 1)
            result[result.lastIndex] = 0
            return result
        }

        throw ImageWriteException("Data must be ByteArray or String")
    }
}
