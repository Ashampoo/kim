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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.getRemainingBytes
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.ImageParser
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.segments.App13Segment
import com.ashampoo.kim.format.jpeg.segments.AppnSegment
import com.ashampoo.kim.format.jpeg.segments.GenericSegment
import com.ashampoo.kim.format.jpeg.segments.JfifSegment
import com.ashampoo.kim.format.jpeg.segments.Segment
import com.ashampoo.kim.format.jpeg.segments.SofnSegment
import com.ashampoo.kim.format.jpeg.segments.UnknownSegment
import com.ashampoo.kim.format.jpeg.xmp.JpegXmpParser
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.TiffReader
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.model.ImageFormat
import com.ashampoo.kim.model.ImageSize

object JpegImageParser : ImageParser {

    @Throws(ImageReadException::class)
    override fun parseMetadata(byteReader: ByteReader, length: Long): ImageMetadata =
        tryWithImageReadException {

            val segments = readSegments(
                byteReader,
                JpegConstants.SOFN_MARKERS +
                    listOf(JpegConstants.JPEG_APP1_MARKER, JpegConstants.JPEG_APP13_MARKER)
            )

            val imageSize = getImageSize(segments)

            val exifBytes = getExifBytes(segments)

            val exif = exifBytes?.let { getExif(it) }

            val iptc = getIptc(segments)

            val xmp = getXmpXml(segments)

            return@tryWithImageReadException ImageMetadata(
                imageFormat = ImageFormat.JPEG,
                imageSize = imageSize,
                exif = exif,
                exifBytes = exifBytes,
                iptc = iptc,
                xmp = xmp
            )
        }

    private fun getImageSize(segments: List<Segment>): ImageSize {

        val sofnSegment = segments.filterIsInstance<SofnSegment>()

        if (sofnSegment.isEmpty())
            throw ImageReadException("No JFIF Data Found.")

        if (sofnSegment.size > 1)
            throw ImageReadException("Redundant JFIF Data Found.")

        val firstSegment = sofnSegment.first()

        return ImageSize(firstSegment.width, firstSegment.height)
    }

    private fun getExif(bytes: ByteArray): TiffContents? {

        val exifByteReader = ByteArrayByteReader(bytes)

        val contents = TiffReader().read(exifByteReader)

        return contents
    }

    private fun getExifBytes(segments: List<Segment>): ByteArray? {

        val exifSegments = segments
            .filterIsInstance<GenericSegment>()
            .filter { it.segmentBytes.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE) }

        if (exifSegments.isEmpty())
            return null

        if (exifSegments.size > 1)
            throw ImageReadException("Multiple APP1 EXIF segments are unsupported right now.")

        val firstSegment = exifSegments.first()

        return firstSegment.segmentBytes.getRemainingBytes(JpegConstants.EXIF_IDENTIFIER_CODE.size)
    }

    private fun getXmpXml(segments: List<Segment>): String? {

        val xmpSegments = segments
            .filterIsInstance<AppnSegment>()
            .filter { segment -> JpegXmpParser.isXmpJpegSegment(segment.segmentBytes) }

        if (xmpSegments.isEmpty())
            return null

        /*
         * Some files in our test repo have multiple XMP strings.
         * This seems to be an error, because it's the same content, but only formatted.
         * We do here what ExifTool does on "exiftool -xmp -b photo.jpg > photo.xmp"
         * and take the first by ignoring the rest.
         */

        val xmp = JpegXmpParser.parseXmpJpegSegment(xmpSegments.first().segmentBytes)

        if (xmp.isBlank())
            return null

        return xmp
    }

    private fun getIptc(segments: List<Segment>): IptcMetadata? {

        val app13Segments = segments.filterIsInstance<App13Segment>()

        for (segment in app13Segments) {

            val iptcMetadata = try {
                segment.parseIptcMetadata()
            } catch (ignore: ImageReadException) {
                println("Ignored broken IPTC: ${ignore.message}")
                continue
            }

            /* Take the first record. */
            if (iptcMetadata != null)
                return iptcMetadata

            /*
             * TODO In case of multiple APP13 segements the records should be merged.
             *  It's now allowed to have multiple ones, if it does not fit into a single one.
             */
        }

        return null
    }

    private fun keepMarker(marker: Int, markers: List<Int>?): Boolean =
        markers?.contains(marker) ?: false

    private fun readSegments(byteReader: ByteReader, markers: List<Int>): List<Segment> {

        val segments = mutableListOf<Segment>()

        val visitor: JpegVisitor = object : JpegVisitor {

            /* Don't read actual image data. */
            override fun beginSOS(): Boolean = false

            override fun visitSOS(marker: Int, markerBytes: ByteArray, imageData: ByteArray) =
                error("Should not be called.")

            // return false to exit traversal.
            override fun visitSegment(
                marker: Int,
                markerBytes: ByteArray,
                segmentLength: Int,
                segmentLengthBytes: ByteArray,
                segmentBytes: ByteArray
            ): Boolean {

                if (marker == JpegConstants.EOI_MARKER)
                    return false

                if (!keepMarker(marker, markers))
                    return true

                when (marker) {
                    JpegConstants.JPEG_APP1_MARKER -> segments.add(AppnSegment(marker, segmentBytes))
                    JpegConstants.JPEG_APP13_MARKER -> segments.add(App13Segment(marker, segmentBytes))
                    JpegConstants.JFIF_MARKER -> segments.add(JfifSegment(marker, segmentBytes))
                    else ->
                        when {

                            JpegConstants.SOFN_MARKERS.binarySearch(marker) >= 0 ->
                                segments.add(SofnSegment(marker, segmentBytes))

                            marker >= JpegConstants.JPEG_APP1_MARKER &&
                                marker <= JpegConstants.JPEG_APP15_MARKER ->
                                segments.add(UnknownSegment(marker, segmentBytes))
                        }
                }

                return true
            }
        }

        JpegUtils.traverseJFIF(byteReader, visitor)

        return segments
    }
}
