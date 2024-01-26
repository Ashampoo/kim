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
package com.ashampoo.kim.format.tiff.constant

import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType.TIFF_DIRECTORY_IFD0
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLongs
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShort
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShorts

/**
 * Standard Tiff Tags
 *
 * http://partners.adobe.com/public/developer/en/tiff/TIFF6.pdf
 */
@Suppress("MagicNumber", "VariableMaxLength", "UnderscoresInNumericLiterals")
object TiffTag {

    /*
     * TODO This list is incomplete
     */

    val TIFF_TAG_NEW_SUBFILE_TYPE = TagInfoLong(
        0xFE, "NewSubfileType",
        TIFF_DIRECTORY_IFD0
    )

    const val SUBFILE_TYPE_VALUE_FULL_RESOLUTION_IMAGE = 0
    const val SUBFILE_TYPE_VALUE_REDUCED_RESOLUTION_IMAGE = 1
    const val SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_IMAGE = 2
    const val SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_REDUCED_RESOLUTION_IMAGE = 3
    const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK = 4
    const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_REDUCED_RESOLUTION_IMAGE = 5
    const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_MULTI_PAGE_IMAGE = 6
    const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_REDUCED_RESOLUTION_MULTI_PAGE_IMAGE = 7

    val TIFF_TAG_SUBFILE_TYPE = TagInfoShort(
        0xFF, "SubfileType",
        TIFF_DIRECTORY_IFD0
    )

    const val OLD_SUBFILE_TYPE_VALUE_FULL_RESOLUTION_IMAGE = 1
    const val OLD_SUBFILE_TYPE_VALUE_REDUCED_RESOLUTION_IMAGE = 2
    const val OLD_SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_IMAGE = 3

    val TIFF_TAG_IMAGE_WIDTH = TagInfoLong(
        0x100, "ImageWidth", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_IMAGE_LENGTH = TagInfoLong(
        0x0101, "ImageLength", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_BITS_PER_SAMPLE = TagInfoShorts(
        0x0102, "BitsPerSample", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_COMPRESSION = TagInfoShort(
        0x0103, "Compression",
        TIFF_DIRECTORY_IFD0
    )

    const val COMPRESSION_VALUE_UNCOMPRESSED = 1
    const val COMPRESSION_VALUE_CCITT_1D = 2
    const val COMPRESSION_VALUE_T4_GROUP_3_FAX = 3
    const val COMPRESSION_VALUE_T6_GROUP_4_FAX = 4
    const val COMPRESSION_VALUE_LZW = 5
    const val COMPRESSION_VALUE_JPEG_OLD_STYLE = 6
    const val COMPRESSION_VALUE_JPEG = 7
    const val COMPRESSION_VALUE_ADOBE_DEFLATE = 8
    const val COMPRESSION_VALUE_JBIG_B_AND_W = 9
    const val COMPRESSION_VALUE_JBIG_COLOR = 10
    const val COMPRESSION_VALUE_NEXT = 32766
    const val COMPRESSION_VALUE_EPSON_ERF_COMPRESSED = 32769
    const val COMPRESSION_VALUE_CCIRLEW = 32771
    const val COMPRESSION_VALUE_PACK_BITS = 32773
    const val COMPRESSION_VALUE_THUNDERSCAN = 32809
    const val COMPRESSION_VALUE_IT8CTPAD = 32895
    const val COMPRESSION_VALUE_IT8LW = 32896
    const val COMPRESSION_VALUE_IT8MP = 32897
    const val COMPRESSION_VALUE_IT8BL = 32898
    const val COMPRESSION_VALUE_PIXAR_FILM = 32908
    const val COMPRESSION_VALUE_PIXAR_LOG = 32909
    const val COMPRESSION_VALUE_DEFLATE = 32946
    const val COMPRESSION_VALUE_DCS = 32947
    const val COMPRESSION_VALUE_JBIG = 34661
    const val COMPRESSION_VALUE_SGILOG = 34676
    const val COMPRESSION_VALUE_SGILOG_24 = 34677
    const val COMPRESSION_VALUE_JPEG_2000 = 34712
    const val COMPRESSION_VALUE_NIKON_NEF_COMPRESSED = 34713
    const val COMPRESSION_VALUE_KODAK_DCR_COMPRESSED = 65000
    const val COMPRESSION_VALUE_PENTAX_PEF_COMPRESSED = 65535

    val TIFF_TAG_PHOTOMETRIC_INTERPRETATION = TagInfoShort(
        0x106, "PhotometricInterpretation",
        TIFF_DIRECTORY_IFD0
    )

    const val PHOTOMETRIC_INTERPRETATION_VALUE_WHITE_IS_ZERO = 0
    const val PHOTOMETRIC_INTERPRETATION_VALUE_BLACK_IS_ZERO = 1
    const val PHOTOMETRIC_INTERPRETATION_VALUE_RGB = 2
    const val PHOTOMETRIC_INTERPRETATION_VALUE_RGB_PALETTE = 3
    const val PHOTOMETRIC_INTERPRETATION_VALUE_TRANSPARENCY_MASK = 4
    const val PHOTOMETRIC_INTERPRETATION_VALUE_CMYK = 5
    const val PHOTOMETRIC_INTERPRETATION_VALUE_YCB_CR = 6
    const val PHOTOMETRIC_INTERPRETATION_VALUE_CIELAB = 8
    const val PHOTOMETRIC_INTERPRETATION_VALUE_ICCLAB = 9
    const val PHOTOMETRIC_INTERPRETATION_VALUE_ITULAB = 10
    const val PHOTOMETRIC_INTERPRETATION_VALUE_COLOR_FILTER_ARRAY = 32803
    const val PHOTOMETRIC_INTERPRETATION_VALUE_PIXAR_LOG_L = 32844
    const val PHOTOMETRIC_INTERPRETATION_VALUE_PIXAR_LOG_LUV = 32845
    const val PHOTOMETRIC_INTERPRETATION_VALUE_LINEAR_RAW = 34892

    val TIFF_TAG_THRESHHOLDING = TagInfoShort(
        0x107, "Threshholding",
        TIFF_DIRECTORY_IFD0
    )

    const val THRESHOLDING_VALUE_NO_DITHERING_OR_HALFTONING = 1
    const val THRESHOLDING_VALUE_ORDERED_DITHER_OR_HALFTONE = 2
    const val THRESHOLDING_VALUE_RANDOMIZED_DITHER = 3

    val TIFF_TAG_CELL_WIDTH = TagInfoShort(
        0x108, "CellWidth",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_CELL_LENGTH = TagInfoShort(
        0x109, "CellLength",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_FILL_ORDER = TagInfoShort(
        0x10A, "FillOrder",
        TIFF_DIRECTORY_IFD0
    )

    const val FILL_ORDER_VALUE_NORMAL = 1
    const val FILL_ORDER_VALUE_REVERSED = 2

    val TIFF_TAG_DOCUMENT_NAME = TagInfoAscii(
        0x10D, "DocumentName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_IMAGE_DESCRIPTION = TagInfoAscii(
        0x10E, "ImageDescription", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_MAKE = TagInfoAscii(
        0x10F, "Make", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_MODEL = TagInfoAscii(
        0x0110, "Model", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_ORIENTATION = TagInfoShort(
        0x0112, "Orientation",
        TIFF_DIRECTORY_IFD0
    )

    const val ORIENTATION_VALUE_HORIZONTAL_NORMAL = 1
    const val ORIENTATION_VALUE_MIRROR_HORIZONTAL = 2
    const val ORIENTATION_VALUE_ROTATE_180 = 3
    const val ORIENTATION_VALUE_MIRROR_VERTICAL = 4
    const val ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW = 5
    const val ORIENTATION_VALUE_ROTATE_90_CW = 6
    const val ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW = 7
    const val ORIENTATION_VALUE_ROTATE_270_CW = 8

    val TIFF_TAG_SAMPLES_PER_PIXEL = TagInfoShort(
        0x0115, "SamplesPerPixel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The number of rows per strip. This is the number of rows in
     * the image of one strip when an image is divided into strips.
     * With JPEG compressed data this designation is not needed and is omitted.
     * See also <StripOffsets> and <StripByteCounts>.
     */
    val TIFF_TAG_ROWS_PER_STRIP = TagInfoLong(
        0x0116, "RowsPerStrip", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_MIN_SAMPLE_VALUE = TagInfoShorts(
        0x0118, "MinSampleValue", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_MAX_SAMPLE_VALUE = TagInfoShorts(
        0x0119, "MaxSampleValue", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_XRESOLUTION = TagInfoRational(
        0x011A, "XResolution",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_YRESOLUTION = TagInfoRational(
        0x011B, "YResolution",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_PLANAR_CONFIGURATION = TagInfoShort(
        0x11C, "PlanarConfiguration",
        TIFF_DIRECTORY_IFD0
    )

    const val PLANAR_CONFIGURATION_VALUE_CHUNKY = 1
    const val PLANAR_CONFIGURATION_VALUE_PLANAR = 2

    val TIFF_TAG_PAGE_NAME = TagInfoAscii(
        0x11D, "PageName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_XPOSITION = TagInfoRationals(
        0x11E, "XPosition", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_YPOSITION = TagInfoRationals(
        0x11F, "YPosition", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_FREE_OFFSETS = TagInfoLongs(
        0x120, "FreeOffsets", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_FREE_BYTE_COUNTS = TagInfoLongs(
        0x121, "FreeByteCounts", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_GRAY_RESPONSE_UNIT = TagInfoShort(
        0x122, "GrayResponseUnit",
        TIFF_DIRECTORY_IFD0
    )

    const val GRAY_RESPONSE_UNIT_VALUE_0_1 = 1
    const val GRAY_RESPONSE_UNIT_VALUE_0_01 = 2
    const val GRAY_RESPONSE_UNIT_VALUE_0_001 = 3
    const val GRAY_RESPONSE_UNIT_VALUE_0_0001 = 4
    const val GRAY_RESPONSE_UNIT_VALUE_0_00001 = 5

    val TIFF_TAG_GRAY_RESPONSE_CURVE = TagInfoShorts(
        0x123, "GrayResponseCurve", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_T4_OPTIONS = TagInfoLong(
        0x124, "T4Options",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_T6_OPTIONS = TagInfoLong(
        0x125, "T6Options",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_RESOLUTION_UNIT = TagInfoShort(
        0x128, "ResolutionUnit",
        TIFF_DIRECTORY_IFD0
    )

    const val RESOLUTION_UNIT_VALUE_NONE = 1
    const val RESOLUTION_UNIT_VALUE_INCHES = 2
    const val RESOLUTION_UNIT_VALUE_CM = 3

    val TIFF_TAG_PAGE_NUMBER = TagInfoShorts(
        0x129, "PageNumber", 2,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_TRANSFER_FUNCTION = TagInfoShorts(
        0x12D, "TransferFunction", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_SOFTWARE = TagInfoAscii(
        0x131, "Software", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_DATE_TIME = TagInfoAscii(
        0x132, "ModifyDate", 20,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_ARTIST = TagInfoAscii(
        0x13B, "Artist", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_HOST_COMPUTER = TagInfoAscii(
        0x13C, "HostComputer", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_PREDICTOR = TagInfoShort(
        0x13D, "Predictor",
        TIFF_DIRECTORY_IFD0
    )

    const val PREDICTOR_VALUE_NONE = 1
    const val PREDICTOR_VALUE_HORIZONTAL_DIFFERENCING = 2
    const val PREDICTOR_VALUE_FLOATING_POINT_DIFFERENCING = 3

    val TIFF_TAG_WHITE_POINT = TagInfoRationals(
        0x13E, "WhitePoint", 2,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_PRIMARY_CHROMATICITIES = TagInfoRationals(
        0x13F, "PrimaryChromaticities", 6,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_COLOR_MAP = TagInfoShorts(
        0x140, "ColorMap", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_HALFTONE_HINTS = TagInfoShorts(
        0x141, "HalftoneHints", 2,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The tile width in pixels.
     * This is the number of columns in each tile.
     */
    val TIFF_TAG_TILE_WIDTH = TagInfoLong(
        0x142, "TileWidth", TIFF_DIRECTORY_IFD0
    )

    /**
     * The tile length (height) in pixels.
     * This is the number of rows in each tile.
     */
    val TIFF_TAG_TILE_LENGTH = TagInfoLong(
        0x143, "TileLength", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_TILE_OFFSETS = TagInfoLongs(
        0x144, "TileOffsets", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0, true
    )

    val TIFF_TAG_TILE_BYTE_COUNTS = TagInfoLong(
        0x145, "TileByteCounts", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_INK_SET = TagInfoShort(
        0x14C, "InkSet",
        TIFF_DIRECTORY_IFD0
    )

    const val INK_SET_VALUE_CMYK = 1
    const val INK_SET_VALUE_NOT_CMYK = 2

    val TIFF_TAG_INK_NAMES = TagInfoAscii(
        0x14D, "InkNames", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_NUMBER_OF_INKS = TagInfoShort(
        0x14E, "NumberOfInks",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The component values that correspond to a 0% dot and 100% dot.
     */
    val TIFF_TAG_DOT_RANGE = TagInfoByte(
        0x150, "DotRange", TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_TARGET_PRINTER = TagInfoAscii(
        0x151, "TargetPrinter", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_EXTRA_SAMPLES = TagInfoShorts(
        0x152, "ExtraSamples", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    const val EXTRA_SAMPLE_ASSOCIATED_ALPHA = 1
    const val EXTRA_SAMPLE_UNASSOCIATED_ALPHA = 2

    val TIFF_TAG_SAMPLE_FORMAT = TagInfoShorts(
        0x153, "SampleFormat", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    const val SAMPLE_FORMAT_VALUE_UNSIGNED_INTEGER = 1
    const val SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER = 2
    const val SAMPLE_FORMAT_VALUE_IEEE_FLOATING_POINT = 3
    const val SAMPLE_FORMAT_VALUE_UNDEFINED = 4
    const val SAMPLE_FORMAT_VALUE_COMPLEX_INTEGER = 5
    const val SAMPLE_FORMAT_VALUE_IEEE_COMPLEX_FLOAT = 6

    /**
     * This field specifies the minimum sample value.
     */
    val TIFF_TAG_SMIN_SAMPLE_VALUE = TagInfoShort(
        0x154, "SMinSampleValue",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * This field specifies the maximum sample value.
     */
    val TIFF_TAG_SMAX_SAMPLE_VALUE = TagInfoShort(
        0x155, "SMaxSampleValue",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_TRANSFER_RANGE = TagInfoShorts(
        0x156, "TransferRange", 6,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_PROC = TagInfoShort(
        0x200, "JPEGProc",
        TIFF_DIRECTORY_IFD0
    )

    const val JPEGPROC_VALUE_BASELINE = 1
    const val JPEGPROC_VALUE_LOSSLESS = 14

    /**
     * This marks where the thumbnail starts.
     * It's called "JPEGInterchangeFormat" in the specficiation,
     * but depending on the manufacturer it is also named
     * "ThumbnailOffset", "PreviewImageStart", "JpgFromRawStart"
     * and "OtherImageStart".
     */
    val TIFF_TAG_JPEG_INTERCHANGE_FORMAT = TagInfoLong(
        0x0201, "JPEGInterchangeFormat",
        TIFF_DIRECTORY_IFD0, true
    )

    val TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = TagInfoLong(
        0x0202, "JPEGInterchangeFormatLength",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_RESTART_INTERVAL = TagInfoShort(
        0x203, "JPEGRestartInterval",
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_LOSSLESS_PREDICTORS = TagInfoShorts(
        0x205, "JPEGLosslessPredictors", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_POINT_TRANSFORMS = TagInfoShorts(
        0x206, "JPEGPointTransforms", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_QTABLES = TagInfoLongs(
        0x207, "JPEGQTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_DCTABLES = TagInfoLongs(
        0x208, "JPEGDCTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_JPEG_ACTABLES = TagInfoLongs(
        0x209, "JPEGACTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_YCBCR_COEFFICIENTS = TagInfoRationals(
        0x211, "YCbCrCoefficients", 3,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_YCBCR_SUB_SAMPLING = TagInfoShorts(
        0x212, "YCbCrSubSampling", 2,
        TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_YCBCR_POSITIONING = TagInfoShort(
        0x213, "YCbCrPositioning",
        TIFF_DIRECTORY_IFD0
    )

    const val YCB_CR_POSITIONING_VALUE_CENTERED = 1
    const val YCB_CR_POSITIONING_VALUE_CO_SITED = 2

    val TIFF_TAG_REFERENCE_BLACK_WHITE = TagInfoLongs(
        0x214, "ReferenceBlackWhite", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_COPYRIGHT = TagInfoAscii(
        0x8298, "Copyright", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    val TIFF_TAG_XMP = TagInfoBytes(
        0x2BC, "XMP", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    /** Panasonic RW2 special tag. */
    val TIFF_TAG_JPG_FROM_RAW = TagInfoBytes(
        0x002E, "JpgFromRaw", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    /** Required field for all DNGs. Can be used to detect if TIFF is a DNG. */
    val TIFF_TAG_DNG_VERSION = TagInfoBytes(
        0xC612, "DNGVersion", 4, TIFF_DIRECTORY_IFD0
    )

    val ALL_TIFF_TAGS = listOf(
        TIFF_TAG_NEW_SUBFILE_TYPE, TIFF_TAG_SUBFILE_TYPE,
        TIFF_TAG_IMAGE_WIDTH, TIFF_TAG_IMAGE_LENGTH,
        TIFF_TAG_BITS_PER_SAMPLE, TIFF_TAG_COMPRESSION,
        TIFF_TAG_PHOTOMETRIC_INTERPRETATION, TIFF_TAG_THRESHHOLDING,
        TIFF_TAG_CELL_WIDTH, TIFF_TAG_CELL_LENGTH, TIFF_TAG_FILL_ORDER,
        TIFF_TAG_DOCUMENT_NAME, TIFF_TAG_IMAGE_DESCRIPTION, TIFF_TAG_MAKE,
        TIFF_TAG_MODEL, TIFF_TAG_ORIENTATION,
        TIFF_TAG_SAMPLES_PER_PIXEL, TIFF_TAG_ROWS_PER_STRIP,
        TIFF_TAG_MIN_SAMPLE_VALUE, TIFF_TAG_MAX_SAMPLE_VALUE, TIFF_TAG_XRESOLUTION,
        TIFF_TAG_YRESOLUTION, TIFF_TAG_PLANAR_CONFIGURATION,
        TIFF_TAG_PAGE_NAME, TIFF_TAG_XPOSITION, TIFF_TAG_YPOSITION,
        TIFF_TAG_FREE_OFFSETS, TIFF_TAG_FREE_BYTE_COUNTS,
        TIFF_TAG_GRAY_RESPONSE_UNIT, TIFF_TAG_GRAY_RESPONSE_CURVE,
        TIFF_TAG_T4_OPTIONS, TIFF_TAG_T6_OPTIONS, TIFF_TAG_RESOLUTION_UNIT,
        TIFF_TAG_PAGE_NUMBER, TIFF_TAG_TRANSFER_FUNCTION,
        TIFF_TAG_SOFTWARE, TIFF_TAG_DATE_TIME, TIFF_TAG_ARTIST,
        TIFF_TAG_HOST_COMPUTER, TIFF_TAG_PREDICTOR, TIFF_TAG_WHITE_POINT,
        TIFF_TAG_PRIMARY_CHROMATICITIES, TIFF_TAG_COLOR_MAP,
        TIFF_TAG_HALFTONE_HINTS, TIFF_TAG_TILE_WIDTH, TIFF_TAG_TILE_LENGTH,
        TIFF_TAG_TILE_OFFSETS, TIFF_TAG_TILE_BYTE_COUNTS, TIFF_TAG_INK_SET,
        TIFF_TAG_INK_NAMES, TIFF_TAG_NUMBER_OF_INKS, TIFF_TAG_DOT_RANGE,
        TIFF_TAG_TARGET_PRINTER, TIFF_TAG_EXTRA_SAMPLES,
        TIFF_TAG_SAMPLE_FORMAT, TIFF_TAG_SMIN_SAMPLE_VALUE,
        TIFF_TAG_SMAX_SAMPLE_VALUE, TIFF_TAG_TRANSFER_RANGE,
        TIFF_TAG_JPEG_PROC, TIFF_TAG_JPEG_INTERCHANGE_FORMAT,
        TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
        TIFF_TAG_JPEG_RESTART_INTERVAL, TIFF_TAG_JPEG_LOSSLESS_PREDICTORS,
        TIFF_TAG_JPEG_POINT_TRANSFORMS, TIFF_TAG_JPEG_QTABLES,
        TIFF_TAG_JPEG_DCTABLES, TIFF_TAG_JPEG_ACTABLES,
        TIFF_TAG_YCBCR_COEFFICIENTS, TIFF_TAG_YCBCR_SUB_SAMPLING,
        TIFF_TAG_YCBCR_POSITIONING, TIFF_TAG_REFERENCE_BLACK_WHITE,
        TIFF_TAG_COPYRIGHT,
        TIFF_TAG_XMP,
        TIFF_TAG_JPG_FROM_RAW,
        TIFF_TAG_DNG_VERSION
    )
}
