/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jpeg.iptc

import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals

class IptcWriterTest {

    @Test
    fun testWriteIptcBlocks() {

        val newIptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(
                listOf(
                    IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
                )
            )
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(newIptcBlock),
            includeApp13Identifier = false
        )

        assertEquals(
            expected = IPTC_HEX,
            actual = iptcBytes.toHex()
        )
    }

    @Test
    fun testWriteIptcBlocksWithApp13Identifier() {

        val newIptcBlock = IptcBlock(
            blockType = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            blockNameBytes = IptcParser.EMPTY_BYTE_ARRAY,
            blockData = IptcWriter.writeIptcBlockData(
                listOf(
                    IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
                )
            )
        )

        val iptcBytes = IptcWriter.writeIptcBlocks(
            blocks = listOf(newIptcBlock),
            includeApp13Identifier = true
        )

        assertEquals(
            expected = JpegConstants.APP13_IDENTIFIER.toHex() + IPTC_HEX,
            actual = iptcBytes.toHex()
        )
    }

    @Test
    fun testWriteIptcBlockData() {

        val blockData = IptcWriter.writeIptcBlockData(
            listOf(
                IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD)
            )
        )

        assertEquals(
            expected = IPTC_BLOCK_DATA_HEX,
            actual = blockData.toHex()
        )
    }
}
