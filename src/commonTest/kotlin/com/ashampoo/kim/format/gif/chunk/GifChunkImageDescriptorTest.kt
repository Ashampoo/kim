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
