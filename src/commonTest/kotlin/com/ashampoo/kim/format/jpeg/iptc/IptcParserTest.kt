/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jpeg.iptc

import com.ashampoo.kim.common.convertHexStringToByteArray
import com.ashampoo.kim.common.toHex
import com.ashampoo.kim.format.jpeg.JpegConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IptcParserTest {

    @Test
    fun testParseIptc() {

        val iptcBlockDataHex = "1c015a00031b25471c0200000200041c02190011c38475c39f6572737420736368c3b66e21"

        val iptcHex =
            JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX + "0404000000000025" + iptcBlockDataHex + "00"

        val iptcBytes = convertHexStringToByteArray(iptcHex)

        val actualIptc = IptcParser.parseIptc(
            bytes = iptcBytes,
            startsWithApp13Header = false
        )

        println(
            actualIptc.rawBlocks.first().blockType
        )

        println(
            actualIptc.rawBlocks.first().blockNameBytes.toHex()
        )

        assertEquals(1, actualIptc.records.size)
        assertEquals(1, actualIptc.rawBlocks.size)

        assertEquals(
            expected = IptcRecord(IptcTypes.KEYWORDS, "Äußerst schön!"),
            actual = actualIptc.records.first()
        )

        val rawBlock = actualIptc.rawBlocks.first()

        assertEquals(
            expected = IptcConstants.IMAGE_RESOURCE_BLOCK_IPTC_DATA,
            actual = rawBlock.blockType
        )

        assertTrue(rawBlock.blockNameBytes.isEmpty())

        assertEquals(
            expected = iptcBlockDataHex,
            actual = rawBlock.blockData.toHex()
        )
    }
}
