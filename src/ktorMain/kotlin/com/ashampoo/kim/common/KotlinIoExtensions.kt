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

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

@OptIn(ExperimentalStdlibApi::class)
public fun Path.copyTo(destination: Path) {

    require(exists()) { "$this does not exist." }

    val metadata = SystemFileSystem.metadataOrNull(this)

    requireNotNull(metadata) { "Failed to read metadata of $this" }
    require(metadata.isRegularFile) { "Source $this must be a regular file." }

    SystemFileSystem.source(this).buffered().use { rawSource ->
        SystemFileSystem.sink(destination).buffered().use { sink ->
            sink.write(rawSource, metadata.size)
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
public fun Path.writeBytes(byteArray: ByteArray): Unit =
    SystemFileSystem
        .sink(this)
        .buffered()
        .use { it.write(byteArray) }

@OptIn(ExperimentalStdlibApi::class)
public fun Path.readBytes(): ByteArray =
    SystemFileSystem
        .source(this)
        .buffered()
        .use { it.readByteArray() }

public fun Path.exists(): Boolean =
    SystemFileSystem.exists(this)

public fun Path.list(): Collection<Path> =
    SystemFileSystem.list(this)
