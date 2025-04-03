package com.ashampoo.kim.format.gif

import com.ashampoo.kim.common.ByteOrder

public object GifConstants {

    public val GIF_BYTE_ORDER: ByteOrder = ByteOrder.LITTLE_ENDIAN

    public val GIF_SIGNATURE: ByteArray = byteArrayOf(
        'G'.code.toByte(),
        'I'.code.toByte(),
        'F'.code.toByte(),
    )

    public const val GIF_TERMINATOR: Byte = 0x3B.toByte()

    public const val EXTENSION_INTRODUCER: Byte = 0x21.toByte()

    public const val IMAGE_SEPARATOR: Byte = 0x2C.toByte()

    public const val BLOCK_TERMINATOR: Byte = 0x00.toByte()

    public const val GRAPHICS_CONTROL_EXTENSION_LABEL: Byte = 0xF9.toByte()

    public const val PLAIN_TEXT_EXTENSION_LABEL: Byte = 0x01.toByte()

    public const val APPLICATION_EXTENSION_LABEL: Byte = 0xFF.toByte()

    public const val COMMENT_EXTENSION_LABEL: Byte = 0xFE.toByte()

    public const val XMP_APPLICATION_IDENTIFIER: String = "XMP Data"

    public const val XMP_APPLICATION_CODE: String = "XMP"
}
