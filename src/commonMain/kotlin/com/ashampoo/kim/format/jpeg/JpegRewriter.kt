/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.BinaryFileParser
import com.ashampoo.kim.common.ExifOverflowException
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.getRemainingBytes
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.format.jpeg.JpegConstants.JPEG_BYTE_ORDER
import com.ashampoo.kim.format.jpeg.iptc.IptcBlock
import com.ashampoo.kim.format.jpeg.iptc.IptcConstants
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcParser
import com.ashampoo.kim.format.jpeg.iptc.IptcWriter
import com.ashampoo.kim.format.jpeg.jfif.JFIFPiece
import com.ashampoo.kim.format.jpeg.jfif.JFIFPieceImageData
import com.ashampoo.kim.format.jpeg.jfif.JFIFPieceSegment
import com.ashampoo.kim.format.jpeg.jfif.JFIFPieceSegmentExif
import com.ashampoo.kim.format.tiff.write.TiffImageWriterBase
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossless
import com.ashampoo.kim.format.tiff.write.TiffImageWriterLossy
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import io.ktor.utils.io.core.toByteArray

/**
 * Interface for Exif write/update/remove functionality for Jpeg/JFIF images.
 */
object JpegRewriter : BinaryFileParser() {

    init {
        byteOrder = JPEG_BYTE_ORDER
    }

    private fun readSegments(byteReader: ByteReader): JFIFPieces {

        val allPieces = mutableListOf<JFIFPiece>()
        val segmentPieces = mutableListOf<JFIFPiece>()

        val visitor: JpegVisitor = object : JpegVisitor {

            /* Read the whole file. */
            override fun beginSOS(): Boolean {
                return true
            }

            override fun visitSOS(marker: Int, markerBytes: ByteArray, imageData: ByteArray) {
                allPieces.add(JFIFPieceImageData(markerBytes, imageData))
            }

            override fun visitSegment(
                marker: Int,
                markerBytes: ByteArray,
                segmentLength: Int,
                segmentLengthBytes: ByteArray,
                segmentBytes: ByteArray
            ): Boolean {

                val piece: JFIFPiece = JFIFPieceSegment(marker, markerBytes, segmentLengthBytes, segmentBytes)

                allPieces.add(piece)
                segmentPieces.add(piece)

                return true
            }
        }

        JpegUtils.traverseJFIF(byteReader, visitor)

        return JFIFPieces(allPieces, segmentPieces)
    }

    private fun insertAfterLastAppSegments(
        segments: List<JFIFPiece>,
        newSegments: List<JFIFPiece>
    ): List<JFIFPiece> {

        val lastAppIndex = segments.indices.lastOrNull { index ->
            val segment = segments[index]
            segment is JFIFPieceSegment && segment.isAppSegment()
        } ?: -1

        val mergedSegments = segments.toMutableList()

        if (lastAppIndex == -1) {

            if (segments.isEmpty())
                throw ImageWriteException("JPEG file has no APP segments.")

            mergedSegments.addAll(1, newSegments)

        } else
            mergedSegments.addAll(lastAppIndex + 1, newSegments)

        return mergedSegments
    }

    fun updateExifMetadataLossless(byteReader: ByteReader, byteWriter: ByteWriter, outputSet: TiffOutputSet) {

        val (oldSegments, segmentPieces) = readSegments(byteReader)

        val oldSegmentsWithoutExif =
            oldSegments.filterNot { piece -> piece is JFIFPieceSegment && piece.isExifSegment() }

        val exifSegmentPieces =
            segmentPieces.filterIsInstance<JFIFPieceSegment>().filter { it.isExifSegment() }

        val writer: TiffImageWriterBase

        if (exifSegmentPieces.isNotEmpty()) {

            val exifPiece = exifSegmentPieces.first()

            val exifBytes = exifPiece.segmentBytes.getRemainingBytes(6)

            writer = TiffImageWriterLossless(outputSet.byteOrder, exifBytes)

        } else
            writer = TiffImageWriterLossy(outputSet.byteOrder)

        val newBytes = writeExifSegment(writer, outputSet)

        writeSegmentsReplacingExif(byteWriter, oldSegmentsWithoutExif, newBytes)
    }

    private fun writeSegmentsReplacingExif(
        byteWriter: ByteWriter,
        oldSegments: List<JFIFPiece>,
        newBytes: ByteArray?
    ) {

        val newSegments = oldSegments.toMutableList()

        byteWriter.write(JpegConstants.SOI)

        if (newBytes != null) {

            val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(byteOrder)

            if (newBytes.size > JpegConstants.MAX_SEGMENT_SIZE)
                throw ExifOverflowException("APP1 Segment is too long: " + newBytes.size)

            val markerLength = newBytes.size + 2
            val markerLengthBytes = markerLength.toShort().toBytes(byteOrder)
            var index = 0

            val firstSegment = newSegments[index] as JFIFPieceSegment

            if (firstSegment.marker == JpegConstants.JFIF_MARKER)
                index = 1

            val exifSegment =
                JFIFPieceSegmentExif(JpegConstants.JPEG_APP1_MARKER, markerBytes, markerLengthBytes, newBytes)

            newSegments.add(index, exifSegment)
        }

        var app1Written = false

        @Suppress("LoopWithTooManyJumpStatements")
        for (piece in newSegments) {

            if (piece is JFIFPieceSegmentExif) {

                /* Only replace first APP1 segment; skips others. */
                if (app1Written)
                    continue

                app1Written = true

                /* It's NULL if the user wants to delete EXIF */
                if (newBytes == null)
                    continue

                val markerBytes = JpegConstants.JPEG_APP1_MARKER.toShort().toBytes(byteOrder)

                if (newBytes.size > JpegConstants.MAX_SEGMENT_SIZE)
                    throw ExifOverflowException("APP1 Segment is too long: " + newBytes.size)

                val markerLength = newBytes.size + 2
                val markerLengthBytes = markerLength.toShort().toBytes(byteOrder)

                byteWriter.write(markerBytes)
                byteWriter.write(markerLengthBytes)
                byteWriter.write(newBytes)

            } else {

                piece.write(byteWriter)
            }
        }
    }

    private fun writeExifSegment(
        writer: TiffImageWriterBase,
        outputSet: TiffOutputSet
    ): ByteArray {

        val byteWriter = ByteArrayByteWriter()

        /* Write prefix */
        byteWriter.write(JpegConstants.EXIF_IDENTIFIER_CODE)

        writer.write(byteWriter, outputSet)

        return byteWriter.toByteArray()
    }

    /**
     * Reads a Jpeg image, replaces the IPTC data in the App13 segment but
     * leaves the other data in that segment (if present) unchanged and writes
     * the result to a stream.
     */
    fun writeIPTC(byteReader: ByteReader, byteWriter: ByteWriter, newData: IptcMetadata) {

        val (oldPieces) = readSegments(byteReader)

        val photoshopApp13Segments =
            oldPieces.filterIsInstance<JFIFPieceSegment>().filter { it.isIptcSegment() }

        if (photoshopApp13Segments.size > 1)
            throw ImageReadException("Image contains more than one Photoshop App13 segment.")

        val newBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIPTCBlock(newData.records)
        )

        val newIptc = IptcMetadata(
            records = newData.records,
            rawBlocks = newData.nonIptcBlocks + newBlock
        )

        val iptcSegment = JFIFPieceSegment(
            marker = JpegConstants.JPEG_APP13_MARKER,
            segmentBytes = IptcWriter.writePhotoshopApp13Segment(newIptc)
        )

        val oldPiecesWithoutPhotoshopApp13Segments =
            oldPieces.filterNot { piece -> piece is JFIFPieceSegment && piece.isIptcSegment() }

        val mergedPieces = insertAfterLastAppSegments(
            oldPiecesWithoutPhotoshopApp13Segments,
            listOf(iptcSegment)
        )

        byteWriter.write(JpegConstants.SOI)

        for (piece in mergedPieces)
            piece.write(byteWriter)
    }

    fun updateXmpXml(byteReader: ByteReader, byteWriter: ByteWriter, xmpXml: String) {

        val (allPieces) = readSegments(byteReader)

        val piecesWithoutXmpSegments =
            allPieces.filterNot { piece -> piece is JFIFPieceSegment && piece.isXmpSegment() }

        val newPieces = mutableListOf<JFIFPieceSegment>()

        val xmpXmlBytes = xmpXml.toByteArray()

        /*
         * If the XMP is larger than the maximal JPEG segment size (around 65 kb),
         * we need to write multiple APP1 segments with XMP. Most XMP is around
         * 10 to 30 kb, so this is really seldom needed.
         */

        if (xmpXmlBytes.size <= JpegConstants.MAX_SEGMENT_SIZE) {

            val segmentWriter = ByteArrayByteWriter()
            segmentWriter.write(JpegConstants.XMP_IDENTIFIER)
            segmentWriter.write(xmpXmlBytes)

            val segmentData = segmentWriter.toByteArray()

            newPieces.add(JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentData))

        } else {

            var offset = 0

            while (offset < xmpXmlBytes.size) {

                val segmentSize = minOf(xmpXmlBytes.size, JpegConstants.MAX_SEGMENT_SIZE)

                val bytesToWrite =
                    xmpXmlBytes.slice(offset until segmentSize).toByteArray()

                val segmentWriter = ByteArrayByteWriter()
                segmentWriter.write(JpegConstants.XMP_IDENTIFIER)
                segmentWriter.write(bytesToWrite)

                val segmentData = segmentWriter.toByteArray()

                newPieces.add(JFIFPieceSegment(JpegConstants.JPEG_APP1_MARKER, segmentData))

                offset += segmentSize
            }
        }

        val mergedPieces = insertAfterLastAppSegments(piecesWithoutXmpSegments, newPieces)

        byteWriter.write(JpegConstants.SOI)

        for (piece in mergedPieces)
            piece.write(byteWriter)
    }

    private data class JFIFPieces(
        val allPieces: List<JFIFPiece>,
        val segmentPieces: List<JFIFPiece>
    )
}
