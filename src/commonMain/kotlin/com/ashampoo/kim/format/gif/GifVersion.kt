package com.ashampoo.kim.format.gif

public enum class GifVersion(public val bytes: ByteArray) {
    GIF87A(
        byteArrayOf(
            '8'.code.toByte(),
            '7'.code.toByte(),
            'a'.code.toByte(),
        )
    ),
    GIF89A(
        byteArrayOf(
            '8'.code.toByte(),
            '9'.code.toByte(),
            'a'.code.toByte(),
        )
    );

    public fun matches(bytes: ByteArray): Boolean = this.bytes.contentEquals(bytes)
}
