/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
enum class TiffOrientation(val value: Int) {

    /** The image is in its default orientation. */
    STANDARD(1),

    /** The image has been mirrored horizontally. */
    MIRROR_HORIZONTAL(2),

    /** The image has been rotated 180 degrees. */
    UPSIDE_DOWN(3),

    /** The image has been mirrored vertically. */
    MIRROR_VERTICAL(4),

    /** The image has been mirrored horizontally and rotated to the right. */
    MIRROR_HORIZONTAL_AND_ROTATE_RIGHT(5),

    /** The image has been rotated to the right. */
    ROTATE_RIGHT(6),

    /** The image has been mirrored vertically and rotated to the right. */
    MIRROR_VERTICAL_AND_ROTATE_RIGHT(7),

    /** The image has been rotated to the left. */
    ROTATE_LEFT(8);

    /** Returns the orientation after rotating the image to the left. */
    fun rotateLeft(): TiffOrientation = when (this) {
        STANDARD -> ROTATE_LEFT
        ROTATE_LEFT -> UPSIDE_DOWN
        UPSIDE_DOWN -> ROTATE_RIGHT
        ROTATE_RIGHT -> STANDARD
        MIRROR_HORIZONTAL -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> MIRROR_VERTICAL
        MIRROR_VERTICAL -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
        MIRROR_VERTICAL_AND_ROTATE_RIGHT -> MIRROR_HORIZONTAL
    }

    /** Returns the orientation after rotating the image to the right. */
    fun rotateRight(): TiffOrientation = when (this) {
        STANDARD -> ROTATE_RIGHT
        ROTATE_RIGHT -> UPSIDE_DOWN
        UPSIDE_DOWN -> ROTATE_LEFT
        ROTATE_LEFT -> STANDARD
        MIRROR_HORIZONTAL -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
        MIRROR_VERTICAL_AND_ROTATE_RIGHT -> MIRROR_VERTICAL
        MIRROR_VERTICAL -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> MIRROR_HORIZONTAL
    }

    /** Returns the orientation after flipping the image horizontally. */
    fun flipHorizontally(): TiffOrientation = when (this) {
        STANDARD -> MIRROR_HORIZONTAL
        ROTATE_LEFT -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
        ROTATE_RIGHT -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
        UPSIDE_DOWN -> MIRROR_HORIZONTAL
        MIRROR_HORIZONTAL -> STANDARD
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> ROTATE_LEFT
        MIRROR_VERTICAL -> ROTATE_RIGHT
        MIRROR_VERTICAL_AND_ROTATE_RIGHT -> ROTATE_LEFT
    }

    /** Returns the orientation after flipping the image vertically. */
    fun flipVertically(): TiffOrientation = when (this) {
        STANDARD -> MIRROR_VERTICAL
        ROTATE_LEFT -> ROTATE_RIGHT
        ROTATE_RIGHT -> ROTATE_LEFT
        UPSIDE_DOWN -> MIRROR_VERTICAL
        MIRROR_HORIZONTAL -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
        MIRROR_HORIZONTAL_AND_ROTATE_RIGHT -> MIRROR_HORIZONTAL
        MIRROR_VERTICAL -> STANDARD
        MIRROR_VERTICAL_AND_ROTATE_RIGHT -> ROTATE_RIGHT
    }

    companion object {

        /** Returns the TiffOrientation enum value that corresponds to the given integer value. */
        fun of(value: Int?): TiffOrientation? = when (value) {
            1 -> STANDARD
            2 -> MIRROR_HORIZONTAL
            3 -> UPSIDE_DOWN
            4 -> MIRROR_VERTICAL
            5 -> MIRROR_HORIZONTAL_AND_ROTATE_RIGHT
            6 -> ROTATE_RIGHT
            7 -> MIRROR_VERTICAL_AND_ROTATE_RIGHT
            8 -> ROTATE_LEFT
            else -> null
        }
    }
}
