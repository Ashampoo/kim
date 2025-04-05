/*
 * Copyright 2025 Ramon Bouckaert
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

package com.ashampoo.kim.format.gif.chunk

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.gif.GifChunkType
import com.ashampoo.kim.format.gif.GifConstants

public class GifChunkTerminator(
    bytes: ByteArray
) : GifChunk(GifChunkType.TERMINATOR, bytes) {

    init {

        if (bytes.size != 1 || bytes[0] != GifConstants.GIF_TERMINATOR)
            throw ImageReadException("Invalid GIF terminator byte(s): $bytes")
    }
}
