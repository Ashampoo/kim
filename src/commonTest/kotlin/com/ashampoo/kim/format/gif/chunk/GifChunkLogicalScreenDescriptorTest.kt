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
