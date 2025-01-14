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
package com.ashampoo.kim.common

import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray

internal actual fun ByteArray.decodeLatin1BytesToString(): String =
    io.ktor.utils.io.core.String(
        bytes = this,
        charset = Charsets.ISO_8859_1
    )

internal actual fun String.encodeToLatin1Bytes(): ByteArray =
    this.toByteArray(Charsets.ISO_8859_1)
