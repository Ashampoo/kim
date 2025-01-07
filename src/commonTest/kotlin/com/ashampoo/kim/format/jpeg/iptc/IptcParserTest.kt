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

import com.ashampoo.kim.common.convertHexStringToByteArray
import com.ashampoo.kim.common.toHex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IptcParserTest {

    @Test
    fun testParseIptc() {

        val iptcBytes = convertHexStringToByteArray(IPTC_HEX)

        val actualIptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        assertEquals(1, actualIptc.records.size)
        assertEquals(1, actualIptc.rawBlocks.size)

        assertEquals(
            expected = IptcRecord(IptcTypes.KEYWORDS, TEST_KEYWORD),
            actual = actualIptc.records.first()
        )

        val rawBlock = actualIptc.rawBlocks.first()

        assertEquals(
            expected = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            actual = rawBlock.blockType
        )

        assertTrue(rawBlock.blockNameBytes.isEmpty())

        assertEquals(
            expected = IPTC_BLOCK_DATA_HEX,
            actual = rawBlock.blockData.toHex()
        )
    }
}
