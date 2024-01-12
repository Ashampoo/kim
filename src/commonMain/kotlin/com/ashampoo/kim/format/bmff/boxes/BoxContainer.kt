/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2002-2023 Drew Noakes and contributors
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
package com.ashampoo.kim.format.bmff.boxes

interface BoxContainer {

    val boxes: List<Box>

    companion object {

        fun findAllBoxesRecursive(boxes: List<Box>): List<Box> {

            val allBoxes = mutableListOf<Box>()

            for (box in boxes) {

                allBoxes.add(box)

                if (box is BoxContainer)
                    allBoxes.addAll(findAllBoxesRecursive(box.boxes))
            }

            return allBoxes
        }
    }
}
