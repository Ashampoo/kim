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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.output.BinaryByteWriter
import com.ashampoo.kim.output.BinaryByteWriter.Companion.createBinaryByteWriter
import com.ashampoo.kim.output.ByteWriter
import kotlin.jvm.JvmStatic

class TiffWriterLossy(
    byteOrder: ByteOrder
) : TiffWriterBase(byteOrder) {

    override fun write(byteWriter: ByteWriter, outputSet: TiffOutputSet) {

        val offsetItems = createOffsetItems(outputSet)

        val outputItems = outputSet.getOutputItems(offsetItems)

        calcNewOffsets(outputItems)

        offsetItems.writeOffsetsToOutputFields()

        val binaryByteWriter = createBinaryByteWriter(byteWriter, byteOrder)

        writeInternal(binaryByteWriter, outputItems)
    }

    private fun calcNewOffsets(outputItems: List<TiffOutputItem>) {

        var offset: Int = TIFF_HEADER_SIZE

        for (outputItem in outputItems) {

            outputItem.offset = offset

            val itemLength = outputItem.getItemLength()

            offset += itemLength + imageDataPaddingLength(itemLength)
        }
    }

    private fun writeInternal(
        bos: BinaryByteWriter,
        outputItems: List<TiffOutputItem>
    ) {

        writeImageFileHeader(bos)

        for (outputItem in outputItems) {

            outputItem.writeItem(bos)

            val length = outputItem.getItemLength()

            val remainder = imageDataPaddingLength(length)

            repeat(remainder) {
                bos.write(0)
            }
        }
    }

    companion object {

        private const val FOUR = 4

        @JvmStatic
        private fun imageDataPaddingLength(dataLength: Int): Int =
            (FOUR - dataLength % FOUR) % FOUR
    }
}
