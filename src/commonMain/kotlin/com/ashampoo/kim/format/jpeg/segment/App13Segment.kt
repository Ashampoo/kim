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
package com.ashampoo.kim.format.jpeg.segment

import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcParser
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader

internal class App13Segment(marker: Int, markerLength: Int, byteReader: ByteReader) :
    AppnSegment(marker, markerLength, byteReader) {

    constructor(marker: Int, segmentData: ByteArray) :
        this(marker, segmentData.size, ByteArrayByteReader(segmentData))

    fun isPhotoshopJpegSegment(): Boolean =
        IptcParser.isPhotoshopApp13Segment(segmentBytes)

    fun parseIptcMetadata(): IptcMetadata? {

        /*
         * In practice, App13 segments are only used for Photoshop/IPTC
         * metadata. However, we should not treat App13 signatures without
         * Photoshop's signature as Photoshop/IPTC segments.
         */
        if (!isPhotoshopJpegSegment())
            return null

        return IptcParser.parseIptc(
            bytes = segmentBytes,
            startsWithApp13Header = true
        )
    }
}
