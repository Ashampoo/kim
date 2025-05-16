package com.ashampoo.kim.output

import java.io.OutputStream

public open class AndroidOutputStreamByteWriter(
    private val outputStream: OutputStream
) : ByteWriter {

    override fun write(byte: Int) {
        outputStream.write(byte)
    }

    override fun write(byteArray: ByteArray) {
        outputStream.write(byteArray)
    }

    override fun flush() {
        outputStream.flush()
    }

    override fun close() {
        outputStream.close()
    }
}
