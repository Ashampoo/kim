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
package com.ashampoo.kim.format.jpeg.jfif

import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.format.jpeg.iptc.IptcParser
import com.ashampoo.kim.output.ByteWriter

open class JFIFPieceSegment(
    val marker: Int,
    val markerBytes: ByteArray,
    val segmentLengthBytes: ByteArray,
    val segmentBytes: ByteArray
) : JFIFPiece {

    constructor(marker: Int, segmentBytes: ByteArray) : this(
        marker = marker,
        markerBytes = marker.toShort().toBytes(JpegConstants.JPEG_BYTE_ORDER),
        segmentLengthBytes = (segmentBytes.size + 2).toShort().toBytes(JpegConstants.JPEG_BYTE_ORDER),
        segmentBytes = segmentBytes
    )

    override fun toString(): String = "JFIF Piece $marker"

    override fun write(byteWriter: ByteWriter) {
        byteWriter.write(markerBytes)
        byteWriter.write(segmentLengthBytes)
        byteWriter.write(segmentBytes)
    }

    fun isAppSegment(): Boolean =
        marker >= JpegConstants.JPEG_APP0_MARKER && marker <= JpegConstants.JPEG_APP15_MARKER

    fun isExifSegment(): Boolean {

        if (marker != JpegConstants.JPEG_APP1_MARKER)
            return false

        return segmentBytes.startsWith(JpegConstants.EXIF_IDENTIFIER_CODE)
    }

    fun isIptcSegment(): Boolean {

        if (marker != JpegConstants.JPEG_APP13_MARKER)
            return false

        return IptcParser.isPhotoshopApp13Segment(segmentBytes)
    }

    fun isXmpSegment(): Boolean {

        if (marker != JpegConstants.JPEG_APP1_MARKER)
            return false

        return segmentBytes.startsWith(JpegConstants.XMP_IDENTIFIER)
    }
}
