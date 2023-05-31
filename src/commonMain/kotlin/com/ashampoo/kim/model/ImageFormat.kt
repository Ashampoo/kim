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

import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.startsWithNullable
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import kotlin.jvm.JvmStatic

enum class ImageFormat(
    val mimeType: String,
    val uniformTypeIdentifier: String,
    val fileNameExtensions: Set<String>
) {

    JPEG("image/jpeg", "public.jpeg", setOf("jpg", "jpeg")),
    GIF("image/gif", "com.compuserve.gif", setOf("gif")),
    PNG("image/png", "public.png", setOf("png")),
    WEBP("image/webp", "public.webp", setOf("webp")),
    TIFF("image/tiff", "public.tiff", setOf("tif", "tiff")),
    HEIC("image/heic", "public.heic", setOf("heic")),
    CR2("image/x-canon-cr2", "com.canon.cr2-raw-image", setOf("cr2")),
    RAF("image/x-fuji-raf", "com.fuji.raw-image", setOf("raf"));

    companion object {

        /** RAF is the longest format that requires us to read 15 bytes to detect it. **/
        const val REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION: Int = 15

        private val allImageFormats = ImageFormat.values()

        private val allFileNameExtensions = getAllFileNameExtensions()

        /*
         * OneDrive reports Canon CR2 files under a wrong mime type.
         */
        private const val CR2_ONEDRIVE_MIME_TYPE = "image/CR2"

        @JvmStatic
        private fun getAllFileNameExtensions(): MutableSet<String> {

            val fileNameExtensions = mutableSetOf<String>()

            for (fileType in allImageFormats)
                for (extension in fileType.fileNameExtensions)
                    fileNameExtensions.add(extension)

            return fileNameExtensions
        }

        @JvmStatic
        fun hasValidFileNameExtension(fileName: String): Boolean {

            for (extension in allFileNameExtensions)
                if (fileName.endsWith(".$extension", ignoreCase = true))
                    return true

            return false
        }

        @JvmStatic
        fun byMimeType(mimeType: String): ImageFormat? {

            for (fileType in allImageFormats)
                if (mimeType.contentEquals(fileType.mimeType, ignoreCase = true))
                    return fileType

            if (mimeType == CR2_ONEDRIVE_MIME_TYPE)
                return CR2

            return null
        }

        @JvmStatic
        fun byUniformTypeIdentifier(
            uniformTypeIdentifier: String
        ): ImageFormat? {

            for (fileType in allImageFormats)
                if (uniformTypeIdentifier.contentEquals(fileType.uniformTypeIdentifier, ignoreCase = true))
                    return fileType

            return null
        }

        @JvmStatic
        fun byFileNameExtension(fileName: String): ImageFormat? {

            for (fileType in allImageFormats)
                for (extension in fileType.fileNameExtensions)
                    if (fileName.endsWith(".$extension", ignoreCase = true))
                        return fileType

            return null
        }

        /**
         * Detects JPEG, GIF, PNG, TIFF & WEBP files based on the header bytes.
         *
         * Note: Can NOT detect HEIC!
         */
        @JvmStatic
        fun detect(bytes: ByteArray): ImageFormat? {

            if (bytes.isEmpty())
                return null

            if (bytes.size < REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                throw ImageReadException("Only got ${bytes.size} for detection of format.")

            /*
             * We want to exit this detection early, so we order the
             * detections in a way that the first checks most likely hit.
             */
            return when {
                /* JPG is the most common format. Check this first. */
                bytes.startsWith(ImageFormatMagicNumbers.jpeg) -> ImageFormat.JPEG
                /* Check other common formats. */
                bytes.startsWith(ImageFormatMagicNumbers.png) -> ImageFormat.PNG
                bytes.startsWithNullable(ImageFormatMagicNumbers.webP) -> ImageFormat.WEBP
                /* Canon CR2 *must* be checked before TIFF, because it's based on TIFF */
                bytes.startsWith(ImageFormatMagicNumbers.cr2) -> ImageFormat.CR2
                bytes.startsWith(ImageFormatMagicNumbers.raf) -> ImageFormat.RAF
                /* Check TIFF after the RAW files. */
                bytes.startsWith(ImageFormatMagicNumbers.tiffLittleEndian) -> ImageFormat.TIFF
                bytes.startsWith(ImageFormatMagicNumbers.tiffBigEndian) -> ImageFormat.TIFF
                /* Check GIF and other unlikely formats... */
                bytes.startsWith(ImageFormatMagicNumbers.gif87a) -> ImageFormat.GIF
                bytes.startsWith(ImageFormatMagicNumbers.gif89a) -> ImageFormat.GIF
                else -> null
            }
        }

        /**
         * Method that helps with finding problems with file types.
         * It translates to a readable name or returns a hex presentation of the bytes.
         */
        @JvmStatic
        fun detectNameOrReturnHex(byteArray: ByteArray): String =
            detect(byteArray)?.name ?: byteArray
                .take(REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                .toByteArray()
                .toSingleNumberHexes()
    }
}
