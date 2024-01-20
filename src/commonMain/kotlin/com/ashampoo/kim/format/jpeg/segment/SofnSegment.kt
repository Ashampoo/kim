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
package com.ashampoo.kim.format.jpeg.segment

import com.ashampoo.kim.format.jpeg.JpegConstants
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader

class SofnSegment(marker: Int, markerLength: Int, byteReader: ByteReader) : Segment(marker, markerLength) {

    val width: Int
    val height: Int

    init {

        /* Skip precision */
        byteReader.skipBytes("Precision", 1)

        height = byteReader.read2BytesAsInt("Height", byteOrder)
        width = byteReader.read2BytesAsInt("Width", byteOrder)
    }

    constructor(marker: Int, segmentData: ByteArray) :
        this(marker, segmentData.size, ByteArrayByteReader(segmentData))

    override fun getDescription(): String =
        "SOFN (SOF" + (marker - JpegConstants.SOF0_MARKER) + ") (" + marker + ")"
}
