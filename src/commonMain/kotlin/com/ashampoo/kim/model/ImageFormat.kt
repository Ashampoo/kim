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
    RAF("image/x-fuji-raf", "com.fuji.raw-image", setOf("raf")),
    NEF("image/x-nikon-nef", "com.nikon.raw-image", setOf("nef")),
    ARW("image/x-sony-arw", "com.sony.raw-image", setOf("arw")),
    RW2("image/x-panasonic-rw2", "com.panasonic.raw-image", setOf("rw2")),
    ORF("image/x-olympus-orf", "com.olympus.raw-image", setOf("orf")),
    DNG("image/x-adobe-dng", "com.adobe.raw-image", setOf("dng"));

    fun isMetadataEmbeddable(): Boolean =
        this == ImageFormat.JPEG || this == ImageFormat.PNG

    companion object {

        /** RAF is the longest format that requires us to read 15 bytes to detect it. **/
        const val REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION: Int = 15

        private val allImageFormats = ImageFormat.values()

        private val allFileNameExtensions = getAllFileNameExtensions()

        /*
         * OneDrive reports RAW files under wrong mime types
         */
        private const val CR2_ONEDRIVE_MIME_TYPE = "image/CR2"
        private const val RAF_ONEDRIVE_MIME_TYPE = "image/RAF"
        private const val NEF_ONEDRIVE_MIME_TYPE = "image/NEF"
        private const val ARW_ONEDRIVE_MIME_TYPE = "image/ARW"
        private const val RW2_ONEDRIVE_MIME_TYPE = "image/RW2"
        private const val ORF_ONEDRIVE_MIME_TYPE = "image/ORF"
        private const val DNG_ONEDRIVE_MIME_TYPE = "image/DNG"

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

            return when (mimeType) {
                CR2_ONEDRIVE_MIME_TYPE -> CR2
                RAF_ONEDRIVE_MIME_TYPE -> RAF
                NEF_ONEDRIVE_MIME_TYPE -> NEF
                ARW_ONEDRIVE_MIME_TYPE -> ARW
                RW2_ONEDRIVE_MIME_TYPE -> RW2
                ORF_ONEDRIVE_MIME_TYPE -> ORF
                DNG_ONEDRIVE_MIME_TYPE -> DNG
                else -> null
            }
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
         * If the byte array is less than REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION
         * (for example empty) than the detection returns null.
         *
         * Note: Can NOT detect HEIC!
         */
        @JvmStatic
        fun detect(bytes: ByteArray): ImageFormat? {

            /*
             * If empty or not enough bytes we can't detect the format and will return NULL.
             * We don't want to throw an Exception, because we can't change the fact that
             * a file is too short to be an image and also we don't want Kotlin/Native to crash.
             */
            if (bytes.size < REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                return null

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
                /* Canon CR2 et al *must* be checked before TIFF, because they are based on TIFF */
                bytes.startsWith(ImageFormatMagicNumbers.cr2) -> ImageFormat.CR2
                bytes.startsWith(ImageFormatMagicNumbers.rw2) -> ImageFormat.RW2
                bytes.startsWith(ImageFormatMagicNumbers.orf_iiro) -> ImageFormat.ORF
                bytes.startsWith(ImageFormatMagicNumbers.orf_mmor) -> ImageFormat.ORF
                bytes.startsWith(ImageFormatMagicNumbers.orf_iirs) -> ImageFormat.ORF
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
