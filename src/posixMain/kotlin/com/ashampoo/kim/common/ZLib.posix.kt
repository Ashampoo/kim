/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.common

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import platform.zlib.Z_DEFAULT_COMPRESSION
import platform.zlib.Z_FINISH
import platform.zlib.Z_NO_FLUSH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.deflate
import platform.zlib.deflateBound
import platform.zlib.deflateEnd
import platform.zlib.deflateInit
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit
import platform.zlib.uByteVar
import platform.zlib.z_stream

private const val OUTPUT_BUFFER_LENGTH = 4096

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
internal actual fun compress(input: String): ByteArray {

    memScoped {

        /* Create a zlib stream structure */
        val stream = alloc<z_stream>()

        /* Initialize the zlib stream */
        deflateInit(stream.ptr, Z_DEFAULT_COMPRESSION)

        val inputBuffer = input.encodeToByteArray()

        val inputBufferLength = inputBuffer.size
        val outputBufferLength = deflateBound(stream.ptr, inputBufferLength.convert())

        val outputBuffer = ByteArray(outputBufferLength.toInt())

        /* Set the input buffer and its length */
        stream.next_in = inputBuffer.refTo(0).getPointer(this) as CPointer<uByteVar>
        stream.avail_in = inputBufferLength.toUInt()

        /* Set the output buffer and its length */
        stream.next_out = outputBuffer.refTo(0).getPointer(this) as CPointer<uByteVar>
        stream.avail_out = outputBufferLength.convert()

        /* Compress the data */
        deflate(stream.ptr, Z_FINISH)

        /* Get the compressed data length */
        val compressedDataLength = outputBufferLength - stream.avail_out

        /* Clean up the zlib stream */
        deflateEnd(stream.ptr)

        /* Return the compressed data as a ByteArray */
        return outputBuffer.copyOf(compressedDataLength.toInt())
    }
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun decompress(byteArray: ByteArray): String {

    memScoped {

        /* Create a zlib stream structure */
        val stream = alloc<z_stream>()

        /* Initialize the zlib stream */
        stream.next_in = byteArray.refTo(0).getPointer(this) as CPointer<uByteVar>
        stream.avail_in = byteArray.size.toUInt()

        /* Specify the decompression mode */
        inflateInit(stream.ptr)

        val outputBuffer = ByteArray(OUTPUT_BUFFER_LENGTH)
        var decompressedData = ""

        try {
            while (true) {

                /* Set the output buffer and its length */
                stream.next_out = outputBuffer.refTo(0).getPointer(this) as CPointer<uByteVar>
                stream.avail_out = OUTPUT_BUFFER_LENGTH.toUInt()

                /* Decompress the data */
                val result = inflate(stream.ptr, Z_NO_FLUSH)

                when (result) {

                    Z_STREAM_END -> {
                        /* The end of the compressed data was reached */
                        val bytesWritten = OUTPUT_BUFFER_LENGTH - stream.avail_out.toInt()
                        decompressedData += outputBuffer.decodeToString(0, bytesWritten)
                        break
                    }

                    Z_OK -> {
                        /* More decompressed data is available */
                        val bytesWritten = OUTPUT_BUFFER_LENGTH - stream.avail_out.toInt()
                        decompressedData += outputBuffer.decodeToString(0, bytesWritten)
                    }

                    else -> {
                        /* An error occurred during decompression */
                        inflateEnd(stream.ptr)
                        throw ImageReadException("Decompression error: $result")
                    }
                }
            }

        } finally {
            /* Clean up the zlib stream */
            inflateEnd(stream.ptr)
        }

        return decompressedData
    }
}
