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
package com.ashampoo.kim.output

import com.ashampoo.kim.input.Closeable

public interface ByteWriter : Closeable {

    public fun write(byte: Byte)

    public fun write(byte: Int) {
        write(byte.toByte())
    }

    public fun write(byteArray: ByteArray)

    public fun flush()

}
