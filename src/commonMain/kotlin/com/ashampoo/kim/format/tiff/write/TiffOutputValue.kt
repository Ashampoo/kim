/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.output.BinaryByteWriter

public class TiffOutputValue internal constructor(
    private val description: String,
    private val bytes: ByteArray
) : TiffOutputItem {

    override var offset: Int =
        TiffOutputItem.UNDEFINED_VALUE

    override fun getItemLength(): Int =
        bytes.size

    public fun updateValue(bytes: ByteArray) {

        if (this.bytes.size != bytes.size)
            throw ImageWriteException("Updated data size mismatch: ${this.bytes.size} != ${bytes.size}")

        bytes.copyInto(this.bytes)
    }

    override fun writeItem(binaryByteWriter: BinaryByteWriter): Unit =
        binaryByteWriter.write(bytes)

    override fun toString(): String =
        description + " = " + bytes.toHex()
}
