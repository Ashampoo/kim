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
package com.ashampoo.kim.model

import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.startsWithNullable
import com.ashampoo.kim.common.toSingleNumberHexes
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import kotlin.jvm.JvmStatic

public enum class ImageFormat(
    public val mimeType: String,
    public val uniformTypeIdentifier: String,
    public val fileNameExtensions: Set<String>
) {

    JPEG("image/jpeg", "public.jpeg", setOf("jpg", "jpeg")),
    GIF("image/gif", "com.compuserve.gif", setOf("gif")),
    PNG("image/png", "public.png", setOf("png")),
    WEBP("image/webp", "org.webmproject.webp", setOf("webp")),
    TIFF("image/tiff", "public.tiff", setOf("tif", "tiff")),
    HEIC("image/heic", "public.heic", setOf("heic")),
    AVIF("image/avif", "public.avif", setOf("avif")),
    CR2("image/x-canon-cr2", "com.canon.cr2-raw-image", setOf("cr2")),
    CR3("image/x-canon-cr3", "com.canon.cr3", setOf("cr3")),
    RAF("image/x-fuji-raf", "com.fuji.raw-image", setOf("raf")),
    NEF("image/x-nikon-nef", "com.nikon.raw-image", setOf("nef")),
    ARW("image/x-sony-arw", "com.sony.raw-image", setOf("arw")),
    RW2("image/x-panasonic-rw2", "com.panasonic.raw-image", setOf("rw2")),
    ORF("image/x-olympus-orf", "com.olympus.raw-image", setOf("orf")),
    DNG("image/x-adobe-dng", "com.adobe.raw-image", setOf("dng")),
    JXL("image/jxl", "public.jxl", setOf("jxl"));

    public fun isMetadataEmbeddable(): Boolean =
        this == JPEG || this == PNG || this == GIF || this == WEBP || this == JXL

    public companion object {

        /**
         * RAF is the longest format that requires us to read 16 bytes to detect it.
         * Right after that we need 12 bytes to check for HEIC.
         */
        public const val REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION: Int = 16

        private val allImageFormats = ImageFormat.entries

        public val allFileNameExtensions: Set<String> = computeAllFileNameExtensions()

        /*
         * OneDrive reports RAW files under wrong mime types
         */
        private const val CR2_ONEDRIVE_MIME_TYPE = "image/CR2"
        private const val CR3_ONEDRIVE_MIME_TYPE = "image/CR3"
        private const val RAF_ONEDRIVE_MIME_TYPE = "image/RAF"
        private const val NEF_ONEDRIVE_MIME_TYPE = "image/NEF"
        private const val ARW_ONEDRIVE_MIME_TYPE = "image/ARW"
        private const val RW2_ONEDRIVE_MIME_TYPE = "image/RW2"
        private const val ORF_ONEDRIVE_MIME_TYPE = "image/ORF"
        private const val DNG_ONEDRIVE_MIME_TYPE = "image/DNG"

        private fun computeAllFileNameExtensions(): MutableSet<String> {

            val fileNameExtensions = mutableSetOf<String>()

            for (fileType in allImageFormats)
                for (extension in fileType.fileNameExtensions)
                    fileNameExtensions.add(extension)

            return fileNameExtensions
        }

        @JvmStatic
        public fun hasValidFileNameExtension(fileName: String): Boolean {

            for (extension in allFileNameExtensions)
                if (fileName.endsWith(".$extension", ignoreCase = true))
                    return true

            return false
        }

        @JvmStatic
        public fun byMimeType(mimeType: String): ImageFormat? {

            for (fileType in allImageFormats)
                if (mimeType.contentEquals(fileType.mimeType, ignoreCase = true))
                    return fileType

            return when (mimeType) {
                CR2_ONEDRIVE_MIME_TYPE -> CR2
                CR3_ONEDRIVE_MIME_TYPE -> CR3
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
        public fun byUniformTypeIdentifier(
            uniformTypeIdentifier: String
        ): ImageFormat? {

            for (fileType in allImageFormats)
                if (uniformTypeIdentifier.contentEquals(fileType.uniformTypeIdentifier, ignoreCase = true))
                    return fileType

            return null
        }

        @JvmStatic
        public fun byFileNameExtension(fileName: String): ImageFormat? {

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
         */
        @JvmStatic
        public fun detect(bytes: ByteArray): ImageFormat? {

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
                bytes.startsWith(ImageFormatMagicNumbers.jpeg) -> JPEG
                /* Check other common formats. */
                bytes.startsWith(ImageFormatMagicNumbers.png) -> PNG
                bytes.startsWithNullable(ImageFormatMagicNumbers.webP) -> WEBP
                /* Canon CR2 et al *must* be checked before TIFF, because they are based on TIFF */
                bytes.startsWith(ImageFormatMagicNumbers.cr2) -> CR2
                bytes.startsWith(ImageFormatMagicNumbers.rw2) -> RW2
                bytes.startsWith(ImageFormatMagicNumbers.orf_iiro) -> ORF
                bytes.startsWith(ImageFormatMagicNumbers.orf_mmor) -> ORF
                bytes.startsWith(ImageFormatMagicNumbers.orf_iirs) -> ORF
                bytes.startsWith(ImageFormatMagicNumbers.raf) -> RAF
                /* Check TIFF after the RAW files. */
                bytes.startsWith(ImageFormatMagicNumbers.tiffLittleEndian) -> TIFF
                bytes.startsWith(ImageFormatMagicNumbers.tiffBigEndian) -> TIFF
                /* Check JXL ISOBMFF */
                bytes.startsWith(ImageFormatMagicNumbers.jxl) -> JXL
                /* Check HEIC variants */
                bytes.startsWithNullable(ImageFormatMagicNumbers.heic) -> HEIC
                bytes.startsWithNullable(ImageFormatMagicNumbers.mif1) -> HEIC
                bytes.startsWithNullable(ImageFormatMagicNumbers.msf1) -> HEIC
                bytes.startsWithNullable(ImageFormatMagicNumbers.heix) -> HEIC
                bytes.startsWithNullable(ImageFormatMagicNumbers.hevc) -> HEIC
                bytes.startsWithNullable(ImageFormatMagicNumbers.hevx) -> HEIC
                /* Check AVIF */
                bytes.startsWithNullable(ImageFormatMagicNumbers.avif) -> AVIF
                /* Check CR3 */
                bytes.startsWithNullable(ImageFormatMagicNumbers.cr3) -> CR3
                /* Check GIF and other unlikely formats... */
                bytes.startsWith(ImageFormatMagicNumbers.gif87a) -> GIF
                bytes.startsWith(ImageFormatMagicNumbers.gif89a) -> GIF
                else -> null
            }
        }

        /**
         * Method that helps with finding problems with file types.
         * It translates to a readable name or returns a hex presentation of the bytes.
         */
        @JvmStatic
        public fun detectNameOrReturnHex(byteArray: ByteArray): String =
            detect(byteArray)?.name ?: byteArray
                .take(REQUIRED_HEADER_BYTE_COUNT_FOR_DETECTION)
                .toByteArray()
                .toSingleNumberHexes()
    }
}
