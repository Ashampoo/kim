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

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffElement
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.output.BinaryByteWriter.Companion.createBinaryByteWriter
import com.ashampoo.kim.output.BufferByteWriter
import com.ashampoo.kim.output.ByteWriter

public class TiffWriterLossless(
    byteOrder: ByteOrder,
    private val exifBytes: ByteArray
) : TiffWriterBase(byteOrder) {

    private fun findRewritableSpaceRanges(
        makerNoteField: TiffOutputField?
    ): List<RewritableSpaceRange> {

        try {

            val tiffContents = TiffReader.read(exifBytes)

            val existingElements = findExistingTiffElements(
                tiffContents,
                makerNoteField
            )

            return calcRewritableSpaceRanges(existingElements)

        } catch (ex: ImageReadException) {
            throw ImageWriteException(ex.message, ex)
        }
    }

    /**
     * Returns a sorted list of existing [TiffElement]s.
     */
    @Suppress("NestedBlockDepth")
    private fun findExistingTiffElements(
        tiffContents: TiffContents,
        makerNoteField: TiffOutputField?
    ): MutableList<TiffElement> {

        val elements = mutableListOf<TiffElement>()

        for (directory in tiffContents.directories) {

            /*
             * Don't write IFD1 directories without image data. If the thumbnail image is broken
             * on load, we should drop the IFD1 on rewrite entirely. It's just a waste of space.
             */
            if (directory.type == 1 && directory.thumbnailBytes == null)
                continue

            elements.add(directory)

            for (field in directory.getDirectoryEntries()) {

                val oversizeValue = field.createOversizeValueElement()

                if (oversizeValue != null) {

                    /* MakerNote offsets must stay the same. */
                    if (makerNoteField?.separateValue != null &&
                        makerNoteField.bytesEqual(field.valueBytes)
                    )
                        makerNoteField.separateValue.offset = field.valueOffset!!
                    else
                        elements.add(oversizeValue)
                }
            }

            directory.getJpegImageDataElement()?.let {
                elements.add(it)
            }

            directory.getStripImageDataElements()?.let {
                elements.addAll(it)
            }
        }

        elements.sort()

        return elements
    }

    private fun calcRewritableSpaceRanges(
        elements: List<TiffElement>
    ): List<RewritableSpaceRange> {

        val rewritableSpaceRanges = mutableListOf<RewritableSpaceRange>()

        var lastElement: TiffElement? = null
        var position: Int = -1

        for (element in elements) {

            /* Usually the root directory IFD0 comes first. */
            if (lastElement == null) {

                /*
                 * Set IFD0 as first element and our current position to
                 * where it ends.
                 */

                lastElement = element
                position = element.offset + element.length

                continue
            }

            /*
             * Ensure that the list of elements is sorted.
             */
            require(element.offset > lastElement.offset) {
                "Parameterized elements must be sorted."
            }

            /*
             * We look for the next big gap. This is when an element
             * has an offset, that does not come next. This way we
             * know when we collected all bytes of the current directory.
             */
            if (element.offset - position > OFFSET_TOLERANCE) {

                /* Local variables to support debugging. */
                val offset = lastElement.offset
                val length = position - lastElement.offset

                rewritableSpaceRanges.add(
                    RewritableSpaceRange(
                        offset = offset,
                        length = length
                    )
                )

                lastElement = element
            }

            position = element.offset + element.length
        }

        lastElement?.let {

            val offset = lastElement.offset
            val length = position - lastElement.offset

            rewritableSpaceRanges.add(
                RewritableSpaceRange(
                    offset = offset,
                    length = length
                )
            )
        }

        return rewritableSpaceRanges
    }

    override fun write(byteWriter: ByteWriter, outputSet: TiffOutputSet) {

        /*
         * The MakerNote field offset must not be changed or
         * else the data will be corrupted.
         */
        val makerNoteField = outputSet.findMakerNoteField()

        val rewritableSpaceRanges = findRewritableSpaceRanges(makerNoteField)

        val oldLength = exifBytes.size

        if (rewritableSpaceRanges.isEmpty())
            throw ImageWriteException("Couldn't analyze old tiff data.")

        if (rewritableSpaceRanges.size == 1) {

            val onlyRange = rewritableSpaceRanges.first()

            val newLength: Int = onlyRange.offset + onlyRange.length + TIFF_HEADER_SIZE

            /*
             * Check if there are no gaps in the old data. If so, it's safe to complete overwrite.
             */
            if (onlyRange.offset == TIFF_HEADER_SIZE && newLength == oldLength) {
                TiffWriterLossy(byteOrder).write(byteWriter, outputSet)
                return
            }
        }

        val frozenFieldOffsets = mutableSetOf<Int>()

        makerNoteField?.separateValue?.offset?.let {

            if (it != TiffOutputItem.UNDEFINED_VALUE)
                frozenFieldOffsets.add(it)
        }

        val offsetItems = createOffsetItems(outputSet)

        /*
         * Receive all items from the OutputSet except for the frozen MakerNotes.
         */
        val outputItems = outputSet.getOutputItems(offsetItems)
            .filterNot { frozenFieldOffsets.contains(it.offset) }

        val outputLength = calcNewOffsets(rewritableSpaceRanges, outputItems)

        offsetItems.writeOffsetsToOutputFields()

        writeInternal(byteWriter, outputSet, rewritableSpaceRanges, outputItems, outputLength)
    }

    private fun calcNewOffsets(
        rewritableSpaceRanges: List<RewritableSpaceRange>,
        outputItems: List<TiffOutputItem>
    ): Int {

        val result = filterRewriteableSpaceRanges(
            rewritableSpaceRanges,
            exifBytes.size
        )

        val unusedSpaceRanges = result.first

        /* Keeps track of the total length the exif bytes we have. */
        var newExifBytesLength = result.second

        val unplacedItems = outputItems.toMutableList()

        while (unplacedItems.isNotEmpty()) {

            /* Pop off largest unplaced item. */
            val outputItem = unplacedItems.removeAt(0)

            val outputItemLength = outputItem.getItemLength()

            /*
             * Find the first range large enough to place this item.
             */
            val fittingRange: RewritableSpaceRange? = unusedSpaceRanges.firstOrNull { range ->
                range.length >= outputItemLength
            }

            if (fittingRange == null) {

                /* Overflow if we couldn't place this item. */
                if (newExifBytesLength and 1 != 0)
                    newExifBytesLength += 1

                outputItem.offset = newExifBytesLength

                newExifBytesLength += outputItemLength

            } else {

                var offset = fittingRange.offset
                var length = fittingRange.length

                /* Offsets have to be a multiple of 2 */
                if (offset and 1 != 0) {
                    offset += 1
                    length -= 1
                }

                outputItem.offset = offset

                unusedSpaceRanges.remove(fittingRange)

                /* If we have space left, create a new range for the reamining space. */
                if (length > outputItemLength) {

                    val excessOffset = offset + outputItemLength
                    val excessLength = length - outputItemLength

                    unusedSpaceRanges.add(
                        RewritableSpaceRange(
                            excessOffset,
                            excessLength
                        )
                    )

                    /* Sort again by offset. */
                    unusedSpaceRanges.sortBy {
                        it.offset
                    }
                }
            }
        }

        return newExifBytesLength
    }

    private fun filterRewriteableSpaceRanges(
        rewritableSpaceRanges: List<RewritableSpaceRange>,
        exifBytesLength: Int
    ): Pair<MutableList<RewritableSpaceRange>, Int> {

        var newExifBytesLength = exifBytesLength

        val filteredRewritableSpaceRanges = rewritableSpaceRanges.toMutableList()

        /*
         * Any items that represent a gap at the end of
         * the exif segment, can be discarded.
         */
        while (filteredRewritableSpaceRanges.isNotEmpty()) {

            val lastRange = filteredRewritableSpaceRanges.last()

            val rangeEnd = lastRange.offset + lastRange.length

            /* If there is nothing to do, stop. */
            if (rangeEnd != newExifBytesLength)
                break

            /* Discarding a tail element. Should only happen once. */

            newExifBytesLength -= lastRange.length

            filteredRewritableSpaceRanges.removeLast()
        }

        return filteredRewritableSpaceRanges to newExifBytesLength
    }

    private fun writeInternal(
        byteWriter: ByteWriter,
        outputSet: TiffOutputSet,
        rewritableSpaceRanges: List<RewritableSpaceRange>,
        outputItems: List<TiffOutputItem>,
        outputLength: Int
    ) {

        val rootDirectory = outputSet.getOrCreateRootDirectory()

        val outputByteArray = ByteArray(outputLength)

        /* Copy old data that inclued makers notes and other stuff. */
        exifBytes.copyInto(
            destination = outputByteArray,
            endIndex = minOf(exifBytes.size, outputByteArray.size)
        )

        /* Write image header */
        writeImageFileHeader(
            byteWriter = createBinaryByteWriter(
                byteWriter = BufferByteWriter(
                    buffer = outputByteArray,
                    index = 0
                ),
                byteOrder = byteOrder
            ),
            offsetToFirstIFD = rootDirectory.offset
        )

        /*
         * Zero out the parsed pieces of old exif segment,
         * in case we don't overwrite them.
         */
        for (element in rewritableSpaceRanges)
            outputByteArray.fill(
                element = 0.toByte(),
                fromIndex = element.offset,
                toIndex = minOf(
                    a = element.offset + element.length,
                    b = outputByteArray.size
                ),
            )

        /* Write in the new items */
        for (outputItem in outputItems) {

            val binaryByteWriter = createBinaryByteWriter(
                byteWriter = BufferByteWriter(
                    buffer = outputByteArray,
                    index = outputItem.offset
                ),
                byteOrder = byteOrder
            )

            outputItem.writeItem(binaryByteWriter)
        }

        byteWriter.write(outputByteArray)
    }

    private companion object {

        const val OFFSET_TOLERANCE = 3
    }
}
