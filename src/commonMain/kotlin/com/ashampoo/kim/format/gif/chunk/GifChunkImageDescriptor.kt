/*
 * Copyright 2025 Ramon Bouckaert
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

package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.gif.GifChunkType
import com.ashampoo.kim.format.gif.GifConstants
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.model.ImageSize
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.write2BytesAsInt
import kotlin.jvm.JvmStatic

public class GifChunkImageDescriptor(
    bytes: ByteArray
) : GifChunk(
    GifChunkType.IMAGE_DESCRIPTOR,
    bytes
) {

    public val leftPosition: Int
    public val topPosition: Int
    public val imageSize: ImageSize
    public val localColorTableFlag: Boolean
    public val interlaceFlag: Boolean
    public val sortFlag: Boolean
    public val localColorTableSize: Int

    init {

        if (bytes.size != 10)
            throw ImageReadException("Invalid size for Image Descriptor chunk: ${bytes.size} bytes, expected 10 bytes.")

        val byteReader = ByteArrayByteReader(bytes)

        /* Read image separator */
        if (byteReader.readByte("image separator") != GifConstants.IMAGE_SEPARATOR)
            throw ImageReadException("Image descriptor did not start with image separator")

        /* Read image position and dimensions */
        leftPosition = byteReader.read2BytesAsInt("left position", GifConstants.GIF_BYTE_ORDER)
        topPosition = byteReader.read2BytesAsInt("top position", GifConstants.GIF_BYTE_ORDER)

        val width = byteReader.read2BytesAsInt("width", GifConstants.GIF_BYTE_ORDER)
        val height = byteReader.read2BytesAsInt("height", GifConstants.GIF_BYTE_ORDER)
        imageSize = ImageSize(width, height)

        val packed = byteReader.readByte("packed fields")
        localColorTableFlag = (packed.toInt() shr 7 and 1) == 1
        interlaceFlag = (packed.toInt() shr 6 and 1) == 1
        sortFlag = (packed.toInt() shr 5 and 1) == 1
        localColorTableSize = packed.toInt() and 0b00000111
    }

    public companion object {

        @JvmStatic
        public fun constructFromProperties(
            leftPosition: Int,
            topPosition: Int,
            imageSize: ImageSize,
            localColorTableFlag: Boolean,
            interlaceFlag: Boolean,
            sortFlag: Boolean,
            localColorTableSize: Int
        ): GifChunkImageDescriptor {

            val byteWriter = ByteArrayByteWriter()

            byteWriter.write(GifConstants.IMAGE_SEPARATOR)

            byteWriter.write2BytesAsInt(leftPosition, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(topPosition, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(imageSize.width, GifConstants.GIF_BYTE_ORDER)
            byteWriter.write2BytesAsInt(imageSize.height, GifConstants.GIF_BYTE_ORDER)

            val packed = ((if (localColorTableFlag) 1 else 0) shl 7) or
                ((if (interlaceFlag) 1 else 0) shl 6) or
                ((if (sortFlag) 1 else 0) shl 5) or
                (localColorTableSize and 0b00000111)

            byteWriter.write(packed)

            return GifChunkImageDescriptor(byteWriter.toByteArray())
        }
    }
}
