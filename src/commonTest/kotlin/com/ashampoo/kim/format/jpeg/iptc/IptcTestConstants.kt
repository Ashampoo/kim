/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.format.jpeg.JpegConstants

/** A keyword with umlauts, great to test encoding. */
internal const val TEST_KEYWORD = "Äußerst schön!"

internal const val IPTC_BLOCK_DATA_HEX =
    "1c015a00031b25471c0200000200041c02190011c38475c39f6572737420736368c3b66e21"

internal const val IPTC_HEX =
    JpegConstants.IPTC_RESOURCE_BLOCK_SIGNATURE_HEX + "0404000000000025" + IPTC_BLOCK_DATA_HEX + "00"
