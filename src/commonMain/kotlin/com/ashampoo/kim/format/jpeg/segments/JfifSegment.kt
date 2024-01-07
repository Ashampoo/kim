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
package com.ashampoo.kim.format.jpeg.segments

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader

class JfifSegment(marker: Int, markerLength: Int, byteReader: ByteReader) : Segment(marker, markerLength) {

    val jfifMajorVersion: Int
    val jfifMinorVersion: Int
    val densityUnits: Int
    val xDensity: Int
    val yDensity: Int
    val xThumbnail: Int
    val yThumbnail: Int
    val thumbnailSize: Int

    init {

        val signature = byteReader.readBytes("Unexpected EOF", JpegConstants.JFIF0_SIGNATURE.size)

        if (
            !JpegConstants.JFIF0_SIGNATURE.contentEquals(signature) &&
            !JpegConstants.JFIF0_SIGNATURE_ALTERNATIVE.contentEquals(signature)
        )
            throw ImageReadException("Not a Valid JPEG File: missing JFIF string")

        jfifMajorVersion = byteReader.readByte("JFIF major version").toInt()
        jfifMinorVersion = byteReader.readByte("JFIF minor version").toInt()
        densityUnits = byteReader.readByte("density units").toInt()
        xDensity = byteReader.read2BytesAsInt("xDensity", byteOrder)
        yDensity = byteReader.read2BytesAsInt("yDensity", byteOrder)
        xThumbnail = byteReader.readByte("xThumbnail").toInt()
        yThumbnail = byteReader.readByte("yThumbnail").toInt()

        thumbnailSize = xThumbnail * yThumbnail

        if (thumbnailSize > 0)
            byteReader.skipBytes("Skip thumbnail", thumbnailSize)
    }

    constructor(marker: Int, segmentBytes: ByteArray) :
        this(marker, segmentBytes.size, ByteArrayByteReader(segmentBytes))

    override fun getDescription(): String =
        "JFIF ($marker)"
}
