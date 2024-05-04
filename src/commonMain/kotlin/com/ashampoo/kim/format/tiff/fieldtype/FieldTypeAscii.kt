/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff.fieldtype

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.indexOfNullTerminator
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.format.tiff.constant.TiffConstants

/**
 * 8-bit byte that contains a 7-bit ASCII code;
 * the last byte must be NUL (binary zero).
 */
public data object FieldTypeAscii : FieldType<String> {

    override val type: Int = TiffConstants.FIELD_TYPE_ASCII_INDEX

    override val name: String = "ASCII"

    override val size: Int = 1

    override fun getValue(bytes: ByteArray, byteOrder: ByteOrder): String {

        /*
         * According to EXIF specification:
         * "2 = ASCII An 8-bit byte containing one 7-bit ASCII code. The final byte is terminated with NULL."
         *
         * Most fields are only one String, but some fields like
         * User Comment (0x9286) are sometimes having multiple strings.
         * We read it all as one String for simplicity.
         */

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
        val string = bytes.slice(
            startIndex = 0,
            count = length
        ).decodeToString()

        return string
    }

    override fun writeData(data: Any, byteOrder: ByteOrder): ByteArray {

        if (data !is String)
            throw ImageWriteException("ASCII Data must be String")

        val bytes = data.encodeToByteArray()

        val result = bytes.copyOf(bytes.size + 1)

        result[result.lastIndex] = 0

        return result
    }
}
