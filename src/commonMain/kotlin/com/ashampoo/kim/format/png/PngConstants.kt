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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.common.ByteOrder

public object PngConstants {

    public val PNG_BYTE_ORDER: ByteOrder = ByteOrder.BIG_ENDIAN

    /* ChunkType is a FourCC, so it's 4 bytes. */
    public const val TPYE_LENGTH: Int = 4

    public const val COMPRESSION_DEFLATE_INFLATE: Int = 0

    @Suppress("MagicNumber")
    public val PNG_SIGNATURE: ByteArray = byteArrayOf(
        0x89.toByte(),
        'P'.code.toByte(),
        'N'.code.toByte(),
        'G'.code.toByte(),
        '\r'.code.toByte(),
        '\n'.code.toByte(),
        0x1A,
        '\n'.code.toByte()
    )

    public const val XMP_KEYWORD: String = "XML:com.adobe.xmp"

    public const val EXIF_KEYWORD: String = "Raw profile type exif"

    public const val IPTC_KEYWORD: String = "Raw profile type iptc"

    /* Size in raw profiles is always 8 chars long and padded with spaces. */
    public const val TXT_SIZE_LENGTH: Int = 8
    public const val TXT_SIZE_PAD: Char = ' '

}
