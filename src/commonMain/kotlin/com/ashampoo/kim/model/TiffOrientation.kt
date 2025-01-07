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
package com.ashampoo.kim.model

@Suppress("MagicNumber")
public enum class TiffOrientation(
    public val value: Int
) {

    /** 1: The image is in its default orientation. */
    STANDARD(1),

    /** 2: The image has been mirrored horizontally. */
    MIRROR_HORIZONTAL(2),

    /** 3: The image has been rotated 180 degrees. */
    UPSIDE_DOWN(3),

    /** 4: The image has been mirrored vertically. */
    MIRROR_VERTICAL(4),

    /** 5: The image has been mirrored horizontally and rotated to the right. */
    MIRROR_HORIZONTAL_AND_ROTATE_LEFT(5),

    /** 6: The image has been rotated to the right. */
    ROTATE_RIGHT(6),

    /** 7: The image has been mirrored vertically and rotated to the left. */
    MIRROR_HORIZONTAL_AND_ROTATE_RIGHT(7),

    /** 8: The image has been rotated to the left. */
    ROTATE_LEFT(8);

    /** Returns the orientation after rotating the image to the left. */
    public fun rotateLeft(): TiffOrientation = when (this) {
        STANDARD -> ROTATE_LEFT
        MIRROR_HORIZONTAL -> MIRROR_HORIZONTAL_AND_ROTATE_LEFT
        UPSIDE_DOWN -> ROTATE_RIGHT
        MIRROR_VERTICAL -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
        MIRROR_HORIZONTAL_AND_ROTATE_LEFT -> MIRROR_VERTICAL
        ROTATE_RIGHT -> STANDARD
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> MIRROR_HORIZONTAL
        ROTATE_LEFT -> UPSIDE_DOWN
    }

    /** Returns the orientation after rotating the image to the right. */
    public fun rotateRight(): TiffOrientation = when (this) {
        STANDARD -> ROTATE_RIGHT
        MIRROR_HORIZONTAL -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
        UPSIDE_DOWN -> ROTATE_LEFT
        MIRROR_VERTICAL -> MIRROR_HORIZONTAL_AND_ROTATE_LEFT
        MIRROR_HORIZONTAL_AND_ROTATE_LEFT -> MIRROR_HORIZONTAL
        ROTATE_RIGHT -> UPSIDE_DOWN
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> MIRROR_VERTICAL
        ROTATE_LEFT -> STANDARD
    }

    /** Returns the orientation after flipping the image horizontally. */
    public fun flipHorizontally(): TiffOrientation = when (this) {
        STANDARD -> MIRROR_HORIZONTAL
        MIRROR_HORIZONTAL -> STANDARD
        UPSIDE_DOWN -> MIRROR_VERTICAL
        MIRROR_VERTICAL -> UPSIDE_DOWN
        MIRROR_HORIZONTAL_AND_ROTATE_LEFT -> ROTATE_RIGHT
        ROTATE_RIGHT -> MIRROR_HORIZONTAL_AND_ROTATE_LEFT
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> ROTATE_LEFT
        ROTATE_LEFT -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
    }

    /** Returns the orientation after flipping the image vertically. */
    public fun flipVertically(): TiffOrientation = when (this) {
        STANDARD -> MIRROR_VERTICAL
        MIRROR_HORIZONTAL -> UPSIDE_DOWN
        UPSIDE_DOWN -> MIRROR_HORIZONTAL
        MIRROR_VERTICAL -> STANDARD
        MIRROR_HORIZONTAL_AND_ROTATE_LEFT -> ROTATE_LEFT
        ROTATE_RIGHT -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> ROTATE_RIGHT
        ROTATE_LEFT -> MIRROR_HORIZONTAL_AND_ROTATE_LEFT
    }

    public companion object {

        /** Returns the TiffOrientation enum value that corresponds to the given integer value. */
        public fun of(value: Int?): TiffOrientation? = when (value) {
            1 -> STANDARD
            2 -> MIRROR_HORIZONTAL
            3 -> UPSIDE_DOWN
            4 -> MIRROR_VERTICAL
            5 -> MIRROR_HORIZONTAL_AND_ROTATE_LEFT
            6 -> ROTATE_RIGHT
            7 -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
            8 -> ROTATE_LEFT
            else -> null
        }
    }
}
