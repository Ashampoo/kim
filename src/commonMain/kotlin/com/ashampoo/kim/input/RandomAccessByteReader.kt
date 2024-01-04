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
package com.ashampoo.kim.input

/**
 * Random access to the bytes are required to read TIFF files
 * where an offset can even be lower than the current position.
 */
interface RandomAccessByteReader : ByteReader {

    fun reset() = moveTo(0)

    fun moveTo(position: Int)

    fun readBytes(offset: Int, length: Int): ByteArray

}
