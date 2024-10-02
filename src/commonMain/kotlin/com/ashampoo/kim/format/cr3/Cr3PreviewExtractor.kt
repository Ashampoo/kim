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
package com.ashampoo.kim.format.cr3

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.slice
import com.ashampoo.kim.common.tryWithImageReadException
import com.ashampoo.kim.format.bmff.BoxReader
import com.ashampoo.kim.format.bmff.box.MediaDataBox
import com.ashampoo.kim.format.cr3.box.CanonTrakOffsetsBox
import com.ashampoo.kim.input.ByteReader
import kotlin.jvm.JvmStatic

public object Cr3PreviewExtractor {

    @Throws(ImageReadException::class)
    @JvmStatic
    public fun extractPreviewImage(
        byteReader: ByteReader
    ): ByteArray? = tryWithImageReadException {

        val allBoxes = BoxReader.readBoxes(
            byteReader = byteReader,
            stopAfterMetadataRead = false
        )

        val metadataSubBoxes = Cr3Reader.findMetadaSubBoxes(allBoxes)

        val trackOffsetsBox = metadataSubBoxes.filterIsInstance<CanonTrakOffsetsBox>().firstOrNull()
            ?: return@tryWithImageReadException null

        println(trackOffsetsBox)

        val mdat = allBoxes.filterIsInstance<MediaDataBox>().firstOrNull()
            ?: return@tryWithImageReadException null

        return@tryWithImageReadException mdat.payload.slice(
            startIndex = trackOffsetsBox.previewOffset,
            count = trackOffsetsBox.previewLength
        )
    }
}
