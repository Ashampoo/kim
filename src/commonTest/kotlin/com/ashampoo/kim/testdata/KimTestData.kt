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
package com.ashampoo.kim.testdata

import com.goncalossilva.resources.Resource

/**
 * This object extracts the bundled test
 * photo files to a specified directory.
 * Unit Tests can find the images there and
 * also benchmark operations can happen.
 */
@Suppress("TooManyFunctions")
object KimTestData {

    private const val RESOURCE_PATH: String = "src/commonTest/resources/com/ashampoo/kim/testdata"

    const val TEST_PHOTO_COUNT: Int = 61
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

    @Suppress("MagicNumber")
    val photoIdsWithExifThumbnail: Set<Int> = setOf(
        2, 3, 4, 5, 6, 7, 8, 10, 12, 15, 16, 18, 19, 20, 21,
        22, 23, 24, 25, 27, 28, 29, 30, 31, 32, 33, 35, 37,
        38, 39, 40, 41, 42, 44, 45, 46, 47, 48, 49, 50
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
        CR2_TEST_IMAGE_INDEX -> "cr2"
        RAF_TEST_IMAGE_INDEX -> "raf"
        TIFF_NONE_TEST_IMAGE_INDEX -> "tif"
        TIFF_ZIP_TEST_IMAGE_INDEX -> "tif"
        TIFF_LZW_TEST_IMAGE_INDEX -> "tif"
        PNG_TEST_IMAGE_INDEX -> "png"
        PNG_APPLE_PREVIEW_TEST_IMAGE_INDEX -> "png"
        PNG_GIMP_TEST_IMAGE_INDEX -> "png"
        else -> "jpg"
    }

    fun getFileName(index: Int): String = "photo_$index.${getExtension(index)}"

    fun getBytesOf(index: Int): ByteArray =
        getBytesOf(getFileName(index))

    fun getBytesOf(fileName: String): ByteArray =
        Resource("$RESOURCE_PATH/full/$fileName").readBytes()

    fun getHeaderBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/headers/photo_${index}_header.${getExtension(index)}").readBytes()

    fun getModifiedBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/modified/photo_${index}_modified.${getExtension(index)}").readBytes()

    fun getExifThumbnailBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/exifthumbs/photo_${index}_exifthumb.jpg").readBytes()

    fun getHeaderExifBytesOf(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/headers/photo_${index}_header_exif.tif").readBytes()

    fun getHeaderTextFile(index: Int, identifier: String): String =
        Resource("$RESOURCE_PATH/headers/photo_${index}_header_$identifier.txt").readText()

    fun getToStringText(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/txt/photo_$index.txt").readBytes()

    fun getOriginalXmp(index: Int): ByteArray =
        Resource("$RESOURCE_PATH/xmp/photo_$index.xmp").readBytes()

    fun getFormattedXmp(index: Int): String =
        Resource("$RESOURCE_PATH/xmp/photo_${index}_formatted.xmp").readText()

    fun getMetadataCsvString(): String =
        Resource("$RESOURCE_PATH/metadata.csv").readText()
}
