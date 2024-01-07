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
package com.ashampoo.kim.format.jpeg.xmp

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.format.jpeg.JpegConstants

object JpegXmpParser {

    fun isXmpJpegSegment(segmentData: ByteArray): Boolean =
        segmentData.startsWith(JpegConstants.XMP_IDENTIFIER)

    fun parseXmpJpegSegment(segmentData: ByteArray): String {

        if (!isXmpJpegSegment(segmentData))
            throw ImageReadException("Invalid JPEG XMP Segment.")

        val index = JpegConstants.XMP_IDENTIFIER.size

        /* The data is UTF-8 encoded XML */
        return segmentData.slice(
            startIndex = index,
            count = segmentData.size - index
        ).decodeToString()
    }
}
