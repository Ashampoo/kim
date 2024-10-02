/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package com.ashampoo.kim.format.cr3.box

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.bmff.BMFFConstants.BMFF_BYTE_ORDER
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.BoxType
import com.ashampoo.kim.format.bmff.box.Box
import com.ashampoo.kim.format.bmff.box.ItemInfoEntryBox
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.read8BytesAsLong
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.readRemainingBytes
import com.ashampoo.kim.input.skipBytes

/**
 * EIC/ISO 14496-12 mdat box
 *
 * The Media Data Box contains all the actual data.
 * This includes the EXIF bytes.
 */
public class CanonTrakOffsetsBox(
    offset: Long,
    size: Long,
    largeSize: Long?,
    payload: ByteArray
) : Box(BoxType.CTBO, offset, size, largeSize, payload) {

    public val entryCount: Int

    public val previewOffset: Int

    public val previewLength: Int

    init {

        val byteReader = ByteArrayByteReader(payload)

        entryCount = byteReader.read4BytesAsInt("count", ByteOrder.BIG_ENDIAN)

        if (entryCount < 4)
            throw ImageReadException("Unexpected entry count: $entryCount")

        /* Not interesting to us. */
        byteReader.skipBytes("xpacket", 20)

        val index =
            byteReader.read4BytesAsInt("index", ByteOrder.BIG_ENDIAN)

        if (index != 2)
            throw ImageReadException("Index should have been 2.")

        previewOffset = byteReader.read8BytesAsLong("previewOffset", ByteOrder.BIG_ENDIAN).toInt()

        previewLength = byteReader.read8BytesAsLong("previewLength", ByteOrder.BIG_ENDIAN).toInt()
    }

    override fun toString(): String =
        "Box '$type' @$offset " +
            "entryCount=$entryCount " +
            "previewOffset=$previewOffset " +
            "previewLength=$previewLength " +
            "($actualLength bytes)"
}
