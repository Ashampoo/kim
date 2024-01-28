/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ashampoo.kim.format.bmff

import com.ashampoo.kim.format.bmff.box.BoxContainer
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.testdata.KimTestData
import kotlin.test.Test
import kotlin.test.assertEquals

class BoxReaderTest {

    @Test
    fun testExtractMetadataBytes() {

        val bytes = KimTestData.getBytesOf(KimTestData.HEIC_TEST_IMAGE_INDEX)

        val byteReader = ByteArrayByteReader(bytes)

        val boxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val allBoxes = BoxContainer.findAllBoxesRecursive(boxes)

        assertEquals(0, allBoxes.first { it.type == BoxType.FTYP }.offset)
        assertEquals(36, allBoxes.first { it.type == BoxType.META }.offset)
        assertEquals(48, allBoxes.first { it.type == BoxType.HDLR }.offset)
        assertEquals(118, allBoxes.first { it.type == BoxType.PITM }.offset)
        assertEquals(132, allBoxes.first { it.type == BoxType.IINF }.offset)
        assertEquals(144, allBoxes.first { it.type == BoxType.INFE }.offset)
        assertEquals(2572, allBoxes.first { it.type == BoxType.ILOC }.offset)
        assertEquals(3404, allBoxes.first { it.type == BoxType.MDAT }.offset)
    }
}
