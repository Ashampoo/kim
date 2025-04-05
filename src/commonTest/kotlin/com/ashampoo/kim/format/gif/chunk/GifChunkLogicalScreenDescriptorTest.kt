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

import com.ashampoo.kim.model.ImageSize
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals

class GifChunkLogicalScreenDescriptorTest {

    @Test
    fun testConstructFromProperties() {

        val canvasSize = ImageSize((100..200).random(), (100..200).random())
        val globalColorTableFlag = Random.nextBoolean()
        val colorResolution = (0..7).random()
        val sortFlag = Random.nextBoolean()
        val globalColorTableSize = (0..7).random()
        val backgroundColorIndex = (0..255).random()
        val pixelAspectRatio = (0..255).random()

        val chunk = GifChunkLogicalScreenDescriptor.constructFromProperties(
            canvasSize,
            globalColorTableFlag,
            colorResolution,
            sortFlag,
            globalColorTableSize,
            backgroundColorIndex,
            pixelAspectRatio
        )

        assertEquals(canvasSize, chunk.canvasSize)
        assertEquals(globalColorTableFlag, chunk.globalColorTableFlag)
        assertEquals(colorResolution, chunk.colorResolution)
        assertEquals(sortFlag, chunk.sortFlag)
        assertEquals(globalColorTableSize, chunk.globalColorTableSize)
        assertEquals(backgroundColorIndex, chunk.backgroundColorIndex)
        assertEquals(pixelAspectRatio, chunk.pixelAspectRatio)
    }
}
