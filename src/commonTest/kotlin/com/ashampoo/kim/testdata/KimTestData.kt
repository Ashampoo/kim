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
package com.ashampoo.kim.testdata

import com.ashampoo.kim.common.readBytes
import com.ashampoo.kim.getPathForResource
import kotlinx.io.files.Path

/**
 * This object extracts the bundled test
 * photo files to a specified directory.
 * Unit Tests can find the images there and
 * also benchmark operations can happen.
 */
@Suppress("TooManyFunctions")
object KimTestData {

    private const val RESOURCE_PATH: String = "src/commonTest/resources/com/ashampoo/kim/testdata"

    const val TEST_PHOTO_COUNT: Int = 80
    const val HIGHEST_JPEG_INDEX: Int = 50

    const val PNG_TEST_IMAGE_INDEX: Int = 51
    const val PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX: Int = 52
    const val PNG_GIMP_TEST_IMAGE_INDEX: Int = 53
    const val TIFF_NONE_TEST_IMAGE_INDEX: Int = 54
    const val TIFF_ZIP_TEST_IMAGE_INDEX: Int = 55
    const val TIFF_LZW_TEST_IMAGE_INDEX: Int = 56
    const val CR2_TEST_IMAGE_INDEX: Int = 57
    const val RAF_TEST_IMAGE_INDEX: Int = 58
    const val WEBP_TEST_IMAGE_INDEX: Int = 59
    const val HEIC_TEST_IMAGE_INDEX: Int = 60
    const val GIF_TEST_IMAGE_INDEX: Int = 61
    const val NEF_TEST_IMAGE_INDEX: Int = 62
    const val ARW_TEST_IMAGE_INDEX: Int = 63
    const val RW2_TEST_IMAGE_INDEX: Int = 64
    const val ORF_TEST_IMAGE_INDEX: Int = 65
    const val DNG_CR2_TEST_IMAGE_INDEX: Int = 66
    const val DNG_RAF_TEST_IMAGE_INDEX: Int = 67
    const val DNG_NEF_TEST_IMAGE_INDEX: Int = 68
    const val DNG_ARW_TEST_IMAGE_INDEX: Int = 69
    const val DNG_RW2_TEST_IMAGE_INDEX: Int = 70
    const val DNG_ORF_TEST_IMAGE_INDEX: Int = 71
    const val HIF_TEST_IMAGE_INDEX: Int = 72
    const val HEIC_TEST_IMAGE_WITH_XMP_INDEX: Int = 73
    const val AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX: Int = 74
    const val HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX: Int = 75
    const val HEIC_TEST_IMAGE_FROM_JPG_USING_APPLE_INDEX: Int = 76
    const val HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX: Int = 77
    const val JXL_NAKED_BYTESTREAM_UNCOMPRESSED_INDEX: Int = 78
    const val JXL_CONTAINER_UNCOMPRESSED_INDEX: Int = 79
    const val JXL_CONTAINER_COMPRESSED_INDEX: Int = 80

    @Suppress("MagicNumber")
    val photoIdsWithExifThumbnail: Set<Int> = setOf(
        2, 3, 4, 5, 6, 7, 8, 10, 12, 15, 16, 18, 19, 20, 21,
        22, 24, 25, 27, 28, 29, 30, 31, 32, 33, 35, 37,
        38, 39, 40, 41, 42, 44, 45, 46, 47, 48, 49, 50,
        PNG_TEST_IMAGE_INDEX,
        PNG_GIMP_TEST_IMAGE_INDEX,
        CR2_TEST_IMAGE_INDEX,
        RAF_TEST_IMAGE_INDEX,
        ARW_TEST_IMAGE_INDEX,
        RW2_TEST_IMAGE_INDEX,
        ORF_TEST_IMAGE_INDEX,
        AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX,
        HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX,

//        JXL_NAKED_BYTESTREAM_UNCOMPRESSED_INDEX,
//        JXL_CONTAINER_UNCOMPRESSED_INDEX,
//        JXL_CONTAINER_COMPRESSED_INDEX,

        // FIXME DNG have thumbnails. Extraction logic does not fit here.
//        DNG_CR2_TEST_IMAGE_INDEX,
//        DNG_RAF_TEST_IMAGE_INDEX,
//        DNG_NEF_TEST_IMAGE_INDEX,
//        DNG_ARW_TEST_IMAGE_INDEX,
//        DNG_RW2_TEST_IMAGE_INDEX,
//        DNG_ORF_TEST_IMAGE_INDEX
    )

    val pngPhotoIds: Set<Int> = setOf(
        PNG_TEST_IMAGE_INDEX,
        PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX,
        PNG_GIMP_TEST_IMAGE_INDEX
    )

    private fun getExtension(index: Int) = when (index) {
        GIF_TEST_IMAGE_INDEX -> "gif"
        WEBP_TEST_IMAGE_INDEX -> "webp"
        HEIC_TEST_IMAGE_INDEX -> "heic"
        HEIC_TEST_IMAGE_WITH_XMP_INDEX -> "heic"
        AVIF_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> "avif"
        HEIC_TEST_IMAGE_FROM_JPG_USING_IMAGEMAGICK_INDEX -> "heic"
        HEIC_TEST_IMAGE_FROM_JPG_USING_APPLE_INDEX -> "heic"
        HEIC_TEST_IMAGE_FROM_SAMSUNG_INDEX -> "heic"
        CR2_TEST_IMAGE_INDEX -> "cr2"
        RAF_TEST_IMAGE_INDEX -> "raf"
        TIFF_NONE_TEST_IMAGE_INDEX -> "tif"
        TIFF_ZIP_TEST_IMAGE_INDEX -> "tif"
        TIFF_LZW_TEST_IMAGE_INDEX -> "tif"
        PNG_TEST_IMAGE_INDEX -> "png"
        PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX -> "png"
        PNG_GIMP_TEST_IMAGE_INDEX -> "png"
        NEF_TEST_IMAGE_INDEX -> "nef"
        ARW_TEST_IMAGE_INDEX -> "arw"
        RW2_TEST_IMAGE_INDEX -> "rw2"
        ORF_TEST_IMAGE_INDEX -> "orf"
        DNG_CR2_TEST_IMAGE_INDEX -> "dng"
        DNG_RAF_TEST_IMAGE_INDEX -> "dng"
        DNG_NEF_TEST_IMAGE_INDEX -> "dng"
        DNG_ARW_TEST_IMAGE_INDEX -> "dng"
        DNG_RW2_TEST_IMAGE_INDEX -> "dng"
        DNG_ORF_TEST_IMAGE_INDEX -> "dng"
        HIF_TEST_IMAGE_INDEX -> "hif"
        JXL_NAKED_BYTESTREAM_UNCOMPRESSED_INDEX -> "jxl"
        JXL_CONTAINER_UNCOMPRESSED_INDEX -> "jxl"
        JXL_CONTAINER_COMPRESSED_INDEX -> "jxl"
        else -> "jpg"
    }

    fun getFileName(index: Int): String = "photo_$index.${getExtension(index)}"

    fun getFullImageDiskPath(index: Int): String =
        getPathForResource("$RESOURCE_PATH/full/${getFileName(index)}")

    fun getBytesOf(index: Int): ByteArray =
        getBytesOf(getFileName(index))

    fun getBytesOf(fileName: String): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/full/$fileName")).readBytes()

    fun getHeaderBytesOf(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/headers/photo_${index}_header.${getExtension(index)}")).readBytes()

    fun getModifiedBytesOf(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/modified/photo_${index}_modified.${getExtension(index)}")).readBytes()

    fun getExifThumbnailBytesOf(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/exifthumbs/photo_${index}_exifthumb.jpg")).readBytes()

    fun getPreviewBytesOf(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/previews/photo_${index}_preview.jpg")).readBytes()

    fun getHeaderExifBytesOf(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/headers/photo_${index}_header_exif.tif")).readBytes()

    fun getHeaderTextFile(index: Int, identifier: String): String =
        Path(getPathForResource("$RESOURCE_PATH/headers/photo_${index}_header_$identifier.txt")).readBytes()
            .decodeToString()

    fun getToStringText(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/txt/photo_$index.txt")).readBytes()

    fun getXmp(fileName: String): String =
        Path(getPathForResource("$RESOURCE_PATH/xmp/$fileName")).readBytes().decodeToString()

    fun getOriginalXmp(index: Int): ByteArray =
        Path(getPathForResource("$RESOURCE_PATH/xmp/photo_$index.xmp")).readBytes()

    fun getFormattedXmp(index: Int): String =
        Path(getPathForResource("$RESOURCE_PATH/xmp/photo_${index}_formatted.xmp")).readBytes().decodeToString()

    fun getMetadataCsvString(): String =
        Path(getPathForResource("$RESOURCE_PATH/metadata.csv")).readBytes().decodeToString()
}
