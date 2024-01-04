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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.model.TiffOrientation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TiffOrientationTest {

    @Test
    fun testRotateLeft() {

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.STANDARD.rotateLeft()
        )

        assertEquals(
            TiffOrientation.UPSIDE_DOWN,
            TiffOrientation.ROTATE_LEFT.rotateLeft()
        )

        assertEquals(
            TiffOrientation.ROTATE_RIGHT,
            TiffOrientation.UPSIDE_DOWN.rotateLeft()
        )

        assertEquals(
            TiffOrientation.STANDARD,
            TiffOrientation.ROTATE_RIGHT.rotateLeft()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT,
            TiffOrientation.MIRROR_HORIZONTAL.rotateLeft()
        )

        assertEquals(
            TiffOrientation.MIRROR_VERTICAL,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT.rotateLeft()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT,
            TiffOrientation.MIRROR_VERTICAL.rotateLeft()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT.rotateLeft()
        )
    }

    @Test
    fun testRotateRight() {

        assertEquals(
            TiffOrientation.ROTATE_RIGHT,
            TiffOrientation.STANDARD.rotateRight()
        )

        assertEquals(
            TiffOrientation.STANDARD,
            TiffOrientation.ROTATE_LEFT.rotateRight()
        )

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.UPSIDE_DOWN.rotateRight()
        )

        assertEquals(
            TiffOrientation.UPSIDE_DOWN,
            TiffOrientation.ROTATE_RIGHT.rotateRight()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT,
            TiffOrientation.MIRROR_HORIZONTAL.rotateRight()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT.rotateRight()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT,
            TiffOrientation.MIRROR_VERTICAL.rotateRight()
        )

        assertEquals(
            TiffOrientation.MIRROR_VERTICAL,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT.rotateRight()
        )
    }

    @Test
    fun testFlipHorizontally() {

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL,
            TiffOrientation.STANDARD.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT,
            TiffOrientation.ROTATE_LEFT.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT,
            TiffOrientation.ROTATE_RIGHT.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.MIRROR_VERTICAL,
            TiffOrientation.UPSIDE_DOWN.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.STANDARD,
            TiffOrientation.MIRROR_HORIZONTAL.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.ROTATE_RIGHT,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.UPSIDE_DOWN,
            TiffOrientation.MIRROR_VERTICAL.flipHorizontally()
        )

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT.flipHorizontally()
        )
    }

    @Test
    fun testFlipVertically() {

        assertEquals(
            TiffOrientation.MIRROR_VERTICAL,
            TiffOrientation.STANDARD.flipVertically()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT,
            TiffOrientation.ROTATE_LEFT.flipVertically()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT,
            TiffOrientation.ROTATE_RIGHT.flipVertically()
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL,
            TiffOrientation.UPSIDE_DOWN.flipVertically()
        )

        assertEquals(
            TiffOrientation.UPSIDE_DOWN,
            TiffOrientation.MIRROR_HORIZONTAL.flipVertically()
        )

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT.flipVertically()
        )

        assertEquals(
            TiffOrientation.STANDARD,
            TiffOrientation.MIRROR_VERTICAL.flipVertically()
        )

        assertEquals(
            TiffOrientation.ROTATE_RIGHT,
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT.flipVertically()
        )
    }

    @Test
    fun testOf() {

        assertEquals(
            TiffOrientation.STANDARD,
            TiffOrientation.of(1)
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL,
            TiffOrientation.of(2)
        )

        assertEquals(
            TiffOrientation.UPSIDE_DOWN,
            TiffOrientation.of(3)
        )

        assertEquals(
            TiffOrientation.MIRROR_VERTICAL,
            TiffOrientation.of(4)
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_LEFT,
            TiffOrientation.of(5)
        )

        assertEquals(
            TiffOrientation.ROTATE_RIGHT,
            TiffOrientation.of(6)
        )

        assertEquals(
            TiffOrientation.MIRROR_HORIZONTAL_AND_ROTATE_RIGHT,
            TiffOrientation.of(7)
        )

        assertEquals(
            TiffOrientation.ROTATE_LEFT,
            TiffOrientation.of(8)
        )

        /* Check illegal values */
        assertNull(TiffOrientation.of(null))
        assertNull(TiffOrientation.of(0))
        assertNull(TiffOrientation.of(-1))
        assertNull(TiffOrientation.of(9))
    }
}
