/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
 * Copyright 2007-2023 The Apache Software Foundation
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

import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.write.TiffOutputSet

data class TiffContents(
    val header: TiffHeader,
    val directories: List<TiffDirectory>,
    /** Artifical MakerNote directory */
    val makerNoteDirectory: TiffDirectory?
) {

    fun findTiffField(tagInfo: TagInfo): TiffField? =
        TiffDirectory.findTiffField(directories, tagInfo)

    fun findTiffDirectory(directoryType: Int): TiffDirectory? =
        directories.find { it.type == directoryType }

    fun getExifThumbnailBytes(): ByteArray? =
        directories.asSequence()
            .mapNotNull { it.jpegImageDataElement?.bytes }
            .firstOrNull()

    fun createOutputSet(): TiffOutputSet {

        val result = TiffOutputSet(header.byteOrder)

        for (directory in directories) {

            /*
             * Certain cameras write some directories more than once.
             * Ignore this bug and just tage the first occurence.
             */
            if (result.findDirectory(directory.type) != null)
                continue

            result.addDirectory(
                directory.createOutputDirectory(header.byteOrder)
            )
        }

        return result
    }

    override fun toString(): String {

        val sb = StringBuilder()

        sb.appendLine("---- TIFF ----")
        sb.appendLine(header)

        for (directory in directories)
            sb.appendLine(directory)

        makerNoteDirectory?.let {
            sb.appendLine(makerNoteDirectory)
        }

        return sb.toString()
    }
}
