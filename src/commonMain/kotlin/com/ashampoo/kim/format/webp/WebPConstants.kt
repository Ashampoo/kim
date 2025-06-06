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
package com.ashampoo.kim.format.webp

import com.ashampoo.kim.common.ByteOrder

public object WebPConstants {

    public val WEBP_BYTE_ORDER: ByteOrder = ByteOrder.LITTLE_ENDIAN

    public val RIFF_SIGNATURE: ByteArray = "RIFF".encodeToByteArray()
    public val WEBP_SIGNATURE: ByteArray = "WEBP".encodeToByteArray()

    /* ChunkType is a FourCC, so it's 4 bytes. */
    public const val TPYE_LENGTH: Int = 4

    public const val CHUNK_SIZE_LENGTH: Int = 4

    public const val CHUNK_HEADER_LENGTH: Int = TPYE_LENGTH + CHUNK_SIZE_LENGTH

    public const val VP8X_PAYLOAD_LENGTH: Int = 10

    /**
     * 16383 x 16383 pixels is the max size for an WebP
     *
     * https://developers.google.com/speed/webp/faq#what_is_the_maximum_size_a_webp_image_can_be
     */
    public const val MAX_SIDE_LENGTH: Int = 16383

}
