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

class GifChunkImageDescriptorTest {

    @Test
    fun testConstructFromProperties() {

        val leftPosition = (0..100).random()
        val topPosition = (0..100).random()
        val imageSize = ImageSize((100..200).random(), (100..200).random())
        val localColorTableFlag = Random.nextBoolean()
        val interlaceFlag = Random.nextBoolean()
        val sortFlag = Random.nextBoolean()
        val localColorTableSize = (0..7).random()

        val chunk = GifChunkImageDescriptor.constructFromProperties(
            leftPosition,
            topPosition,
            imageSize,
            localColorTableFlag,
            interlaceFlag,
            sortFlag,
            localColorTableSize
        )

        assertEquals(leftPosition, chunk.leftPosition)
        assertEquals(topPosition, chunk.topPosition)
        assertEquals(imageSize, chunk.imageSize)
        assertEquals(localColorTableFlag, chunk.localColorTableFlag)
        assertEquals(interlaceFlag, chunk.interlaceFlag)
        assertEquals(sortFlag, chunk.sortFlag)
        assertEquals(localColorTableSize, chunk.localColorTableSize)
    }
}
