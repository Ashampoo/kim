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
public object TiffTag {

    /*
     * TODO This list is incomplete
     */

    public val TIFF_TAG_NEW_SUBFILE_TYPE: TagInfoLong = TagInfoLong(
        0xFE, "NewSubfileType",
        TIFF_DIRECTORY_IFD0
    )

    public const val SUBFILE_TYPE_VALUE_FULL_RESOLUTION_IMAGE: Int = 0
    public const val SUBFILE_TYPE_VALUE_REDUCED_RESOLUTION_IMAGE: Int = 1
    public const val SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_IMAGE: Int = 2
    public const val SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_REDUCED_RESOLUTION_IMAGE: Int = 3
    public const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK: Int = 4
    public const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_REDUCED_RESOLUTION_IMAGE: Int = 5
    public const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_MULTI_PAGE_IMAGE: Int = 6
    public const val SUBFILE_TYPE_VALUE_TRANSPARENCY_MASK_OF_REDUCED_RESOLUTION_MULTI_PAGE_IMAGE: Int = 7

    public val TIFF_TAG_SUBFILE_TYPE: TagInfoShort = TagInfoShort(
        0xFF, "SubfileType",
        TIFF_DIRECTORY_IFD0
    )

    public const val OLD_SUBFILE_TYPE_VALUE_FULL_RESOLUTION_IMAGE: Int = 1
    public const val OLD_SUBFILE_TYPE_VALUE_REDUCED_RESOLUTION_IMAGE: Int = 2
    public const val OLD_SUBFILE_TYPE_VALUE_SINGLE_PAGE_OF_MULTI_PAGE_IMAGE: Int = 3

    public val TIFF_TAG_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0x100, "ImageWidth", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_IMAGE_LENGTH: TagInfoLong = TagInfoLong(
        0x0101, "ImageLength", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_BITS_PER_SAMPLE: TagInfoShorts = TagInfoShorts(
        0x0102, "BitsPerSample", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_COMPRESSION: TagInfoShort = TagInfoShort(
        0x0103, "Compression",
        TIFF_DIRECTORY_IFD0
    )

    public const val COMPRESSION_VALUE_UNCOMPRESSED: Int = 1
    public const val COMPRESSION_VALUE_CCITT_1D: Int = 2
    public const val COMPRESSION_VALUE_T4_GROUP_3_FAX: Int = 3
    public const val COMPRESSION_VALUE_T6_GROUP_4_FAX: Int = 4
    public const val COMPRESSION_VALUE_LZW: Int = 5
    public const val COMPRESSION_VALUE_JPEG_OLD_STYLE: Int = 6
    public const val COMPRESSION_VALUE_JPEG: Int = 7
    public const val COMPRESSION_VALUE_ADOBE_DEFLATE: Int = 8
    public const val COMPRESSION_VALUE_JBIG_B_AND_W: Int = 9
    public const val COMPRESSION_VALUE_JBIG_COLOR: Int = 10
    public const val COMPRESSION_VALUE_NEXT: Int = 32766
    public const val COMPRESSION_VALUE_EPSON_ERF_COMPRESSED: Int = 32769
    public const val COMPRESSION_VALUE_CCIRLEW: Int = 32771
    public const val COMPRESSION_VALUE_PACK_BITS: Int = 32773
    public const val COMPRESSION_VALUE_THUNDERSCAN: Int = 32809
    public const val COMPRESSION_VALUE_IT8CTPAD: Int = 32895
    public const val COMPRESSION_VALUE_IT8LW: Int = 32896
    public const val COMPRESSION_VALUE_IT8MP: Int = 32897
    public const val COMPRESSION_VALUE_IT8BL: Int = 32898
    public const val COMPRESSION_VALUE_PIXAR_FILM: Int = 32908
    public const val COMPRESSION_VALUE_PIXAR_LOG: Int = 32909
    public const val COMPRESSION_VALUE_DEFLATE: Int = 32946
    public const val COMPRESSION_VALUE_DCS: Int = 32947
    public const val COMPRESSION_VALUE_JBIG: Int = 34661
    public const val COMPRESSION_VALUE_SGILOG: Int = 34676
    public const val COMPRESSION_VALUE_SGILOG_24: Int = 34677
    public const val COMPRESSION_VALUE_JPEG_2000: Int = 34712
    public const val COMPRESSION_VALUE_NIKON_NEF_COMPRESSED: Int = 34713
    public const val COMPRESSION_VALUE_KODAK_DCR_COMPRESSED: Int = 65000
    public const val COMPRESSION_VALUE_PENTAX_PEF_COMPRESSED: Int = 65535

    public val TIFF_TAG_PHOTOMETRIC_INTERPRETATION: TagInfoShort = TagInfoShort(
        0x106, "PhotometricInterpretation",
        TIFF_DIRECTORY_IFD0
    )

    public const val PHOTOMETRIC_INTERPRETATION_VALUE_WHITE_IS_ZERO: Int = 0
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_BLACK_IS_ZERO: Int = 1
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_RGB: Int = 2
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_RGB_PALETTE: Int = 3
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_TRANSPARENCY_MASK: Int = 4
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_CMYK: Int = 5
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_YCB_CR: Int = 6
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_CIELAB: Int = 8
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_ICCLAB: Int = 9
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_ITULAB: Int = 10
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_COLOR_FILTER_ARRAY: Int = 32803
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_PIXAR_LOG_L: Int = 32844
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_PIXAR_LOG_LUV: Int = 32845
    public const val PHOTOMETRIC_INTERPRETATION_VALUE_LINEAR_RAW: Int = 34892

    public val TIFF_TAG_THRESHHOLDING: TagInfoShort = TagInfoShort(
        0x107, "Threshholding",
        TIFF_DIRECTORY_IFD0
    )

    public const val THRESHOLDING_VALUE_NO_DITHERING_OR_HALFTONING: Int = 1
    public const val THRESHOLDING_VALUE_ORDERED_DITHER_OR_HALFTONE: Int = 2
    public const val THRESHOLDING_VALUE_RANDOMIZED_DITHER: Int = 3

    public val TIFF_TAG_CELL_WIDTH: TagInfoShort = TagInfoShort(
        0x108, "CellWidth",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_CELL_LENGTH: TagInfoShort = TagInfoShort(
        0x109, "CellLength",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_FILL_ORDER: TagInfoShort = TagInfoShort(
        0x10A, "FillOrder",
        TIFF_DIRECTORY_IFD0
    )

    public const val FILL_ORDER_VALUE_NORMAL: Int = 1
    public const val FILL_ORDER_VALUE_REVERSED: Int = 2

    public val TIFF_TAG_DOCUMENT_NAME: TagInfoAscii = TagInfoAscii(
        0x10D, "DocumentName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_IMAGE_DESCRIPTION: TagInfoAscii = TagInfoAscii(
        0x10E, "ImageDescription", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_MAKE: TagInfoAscii = TagInfoAscii(
        0x10F, "Make", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_MODEL: TagInfoAscii = TagInfoAscii(
        0x0110, "Model", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_STRIP_OFFSETS: TagInfoLong = TagInfoLong(
        0x0111, "StripOffsets",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    public val TIFF_TAG_ORIENTATION: TagInfoShort = TagInfoShort(
        0x0112, "Orientation",
        TIFF_DIRECTORY_IFD0
    )

    public const val ORIENTATION_VALUE_HORIZONTAL_NORMAL: Int = 1
    public const val ORIENTATION_VALUE_MIRROR_HORIZONTAL: Int = 2
    public const val ORIENTATION_VALUE_ROTATE_180: Int = 3
    public const val ORIENTATION_VALUE_MIRROR_VERTICAL: Int = 4
    public const val ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_270_CW: Int = 5
    public const val ORIENTATION_VALUE_ROTATE_90_CW: Int = 6
    public const val ORIENTATION_VALUE_MIRROR_HORIZONTAL_AND_ROTATE_90_CW: Int = 7
    public const val ORIENTATION_VALUE_ROTATE_270_CW: Int = 8

    public val TIFF_TAG_SAMPLES_PER_PIXEL: TagInfoShort = TagInfoShort(
        0x0115, "SamplesPerPixel",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The number of rows per strip. This is the number of rows in
     * the image of one strip when an image is divided into strips.
     * With JPEG compressed data this designation is not needed and is omitted.
     * See also <StripOffsets> and <StripByteCounts>.
     */
    public val TIFF_TAG_ROWS_PER_STRIP: TagInfoLong = TagInfoLong(
        0x0116, "RowsPerStrip", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_STRIP_BYTE_COUNTS: TagInfoLong = TagInfoLong(
        0x0117, "StripByteCounts",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_MIN_SAMPLE_VALUE: TagInfoShorts = TagInfoShorts(
        0x0118, "MinSampleValue", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_MAX_SAMPLE_VALUE: TagInfoShorts = TagInfoShorts(
        0x0119, "MaxSampleValue", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_XRESOLUTION: TagInfoRational = TagInfoRational(
        0x011A, "XResolution",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_YRESOLUTION: TagInfoRational = TagInfoRational(
        0x011B, "YResolution",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_PLANAR_CONFIGURATION: TagInfoShort = TagInfoShort(
        0x11C, "PlanarConfiguration",
        TIFF_DIRECTORY_IFD0
    )

    public const val PLANAR_CONFIGURATION_VALUE_CHUNKY: Int = 1
    public const val PLANAR_CONFIGURATION_VALUE_PLANAR: Int = 2

    public val TIFF_TAG_PAGE_NAME: TagInfoAscii = TagInfoAscii(
        0x11D, "PageName", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_XPOSITION: TagInfoRationals = TagInfoRationals(
        0x11E, "XPosition", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_YPOSITION: TagInfoRationals = TagInfoRationals(
        0x11F, "YPosition", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_FREE_OFFSETS: TagInfoLongs = TagInfoLongs(
        0x120, "FreeOffsets", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_FREE_BYTE_COUNTS: TagInfoLongs = TagInfoLongs(
        0x121, "FreeByteCounts", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_GRAY_RESPONSE_UNIT: TagInfoShort = TagInfoShort(
        0x122, "GrayResponseUnit",
        TIFF_DIRECTORY_IFD0
    )

    public const val GRAY_RESPONSE_UNIT_VALUE_0_1: Int = 1
    public const val GRAY_RESPONSE_UNIT_VALUE_0_01: Int = 2
    public const val GRAY_RESPONSE_UNIT_VALUE_0_001: Int = 3
    public const val GRAY_RESPONSE_UNIT_VALUE_0_0001: Int = 4
    public const val GRAY_RESPONSE_UNIT_VALUE_0_00001: Int = 5

    public val TIFF_TAG_GRAY_RESPONSE_CURVE: TagInfoShorts = TagInfoShorts(
        0x123, "GrayResponseCurve", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_T4_OPTIONS: TagInfoLong = TagInfoLong(
        0x124, "T4Options",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_T6_OPTIONS: TagInfoLong = TagInfoLong(
        0x125, "T6Options",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_RESOLUTION_UNIT: TagInfoShort = TagInfoShort(
        0x128, "ResolutionUnit",
        TIFF_DIRECTORY_IFD0
    )

    public const val RESOLUTION_UNIT_VALUE_NONE: Int = 1
    public const val RESOLUTION_UNIT_VALUE_INCHES: Int = 2
    public const val RESOLUTION_UNIT_VALUE_CM: Int = 3

    public val TIFF_TAG_PAGE_NUMBER: TagInfoShorts = TagInfoShorts(
        0x129, "PageNumber", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_TRANSFER_FUNCTION: TagInfoShorts = TagInfoShorts(
        0x12D, "TransferFunction", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_SOFTWARE: TagInfoAscii = TagInfoAscii(
        0x131, "Software", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_DATE_TIME: TagInfoAscii = TagInfoAscii(
        0x132, "ModifyDate", 20,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_ARTIST: TagInfoAscii = TagInfoAscii(
        0x13B, "Artist", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_HOST_COMPUTER: TagInfoAscii = TagInfoAscii(
        0x13C, "HostComputer", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_PREDICTOR: TagInfoShort = TagInfoShort(
        0x13D, "Predictor",
        TIFF_DIRECTORY_IFD0
    )

    public const val PREDICTOR_VALUE_NONE: Int = 1
    public const val PREDICTOR_VALUE_HORIZONTAL_DIFFERENCING: Int = 2
    public const val PREDICTOR_VALUE_FLOATING_POINT_DIFFERENCING: Int = 3

    public val TIFF_TAG_WHITE_POINT: TagInfoRationals = TagInfoRationals(
        0x13E, "WhitePoint", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_PRIMARY_CHROMATICITIES: TagInfoRationals = TagInfoRationals(
        0x13F, "PrimaryChromaticities", 6,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_COLOR_MAP: TagInfoShorts = TagInfoShorts(
        0x140, "ColorMap", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_HALFTONE_HINTS: TagInfoShorts = TagInfoShorts(
        0x141, "HalftoneHints", 2,
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The tile width in pixels.
     * This is the number of columns in each tile.
     */
    public val TIFF_TAG_TILE_WIDTH: TagInfoLong = TagInfoLong(
        0x142, "TileWidth", TIFF_DIRECTORY_IFD0
    )

    /**
     * The tile length (height) in pixels.
     * This is the number of rows in each tile.
     */
    public val TIFF_TAG_TILE_LENGTH: TagInfoLong = TagInfoLong(
        0x143, "TileLength", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_TILE_OFFSETS: TagInfoLongs = TagInfoLongs(
        0x144, "TileOffsets", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0, true
    )

    public val TIFF_TAG_TILE_BYTE_COUNTS: TagInfoLong = TagInfoLong(
        0x145, "TileByteCounts", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_INK_SET: TagInfoShort = TagInfoShort(
        0x14C, "InkSet",
        TIFF_DIRECTORY_IFD0
    )

    public const val INK_SET_VALUE_CMYK: Int = 1
    public const val INK_SET_VALUE_NOT_CMYK: Int = 2

    public val TIFF_TAG_INK_NAMES: TagInfoAscii = TagInfoAscii(
        0x14D, "InkNames", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_NUMBER_OF_INKS: TagInfoShort = TagInfoShort(
        0x14E, "NumberOfInks",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * The component values that correspond to a 0% dot and 100% dot.
     */
    public val TIFF_TAG_DOT_RANGE: TagInfoByte = TagInfoByte(
        0x150, "DotRange", TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_TARGET_PRINTER: TagInfoAscii = TagInfoAscii(
        0x151, "TargetPrinter", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_EXTRA_SAMPLES: TagInfoShorts = TagInfoShorts(
        0x152, "ExtraSamples", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public const val EXTRA_SAMPLE_ASSOCIATED_ALPHA: Int = 1
    public const val EXTRA_SAMPLE_UNASSOCIATED_ALPHA: Int = 2

    public val TIFF_TAG_SAMPLE_FORMAT: TagInfoShorts = TagInfoShorts(
        0x153, "SampleFormat", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public const val SAMPLE_FORMAT_VALUE_UNSIGNED_INTEGER: Int = 1
    public const val SAMPLE_FORMAT_VALUE_TWOS_COMPLEMENT_SIGNED_INTEGER: Int = 2
    public const val SAMPLE_FORMAT_VALUE_IEEE_FLOATING_POINT: Int = 3
    public const val SAMPLE_FORMAT_VALUE_UNDEFINED: Int = 4
    public const val SAMPLE_FORMAT_VALUE_COMPLEX_INTEGER: Int = 5
    public const val SAMPLE_FORMAT_VALUE_IEEE_COMPLEX_FLOAT: Int = 6

    /**
     * This field specifies the minimum sample value.
     */
    public val TIFF_TAG_SMIN_SAMPLE_VALUE: TagInfoShort = TagInfoShort(
        0x154, "SMinSampleValue",
        TIFF_DIRECTORY_IFD0
    )

    /**
     * This field specifies the maximum sample value.
     */
    public val TIFF_TAG_SMAX_SAMPLE_VALUE: TagInfoShort = TagInfoShort(
        0x155, "SMaxSampleValue",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_TRANSFER_RANGE: TagInfoShorts = TagInfoShorts(
        0x156, "TransferRange", 6,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_PROC: TagInfoShort = TagInfoShort(
        0x200, "JPEGProc",
        TIFF_DIRECTORY_IFD0
    )

    public const val JPEGPROC_VALUE_BASELINE: Int = 1
    public const val JPEGPROC_VALUE_LOSSLESS: Int = 14

    /**
     * This marks where the thumbnail starts.
     * It's called "JPEGInterchangeFormat" in the specficiation,
     * but depending on the manufacturer it is also named
     * "ThumbnailOffset", "PreviewImageStart", "JpgFromRawStart"
     * and "OtherImageStart".
     */
    public val TIFF_TAG_JPEG_INTERCHANGE_FORMAT: TagInfoLong = TagInfoLong(
        0x0201, "JPEGInterchangeFormat",
        TIFF_DIRECTORY_IFD0, true
    )

    public val TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH: TagInfoLong = TagInfoLong(
        0x0202, "JPEGInterchangeFormatLength",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_RESTART_INTERVAL: TagInfoShort = TagInfoShort(
        0x203, "JPEGRestartInterval",
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_LOSSLESS_PREDICTORS: TagInfoShorts = TagInfoShorts(
        0x205, "JPEGLosslessPredictors", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_POINT_TRANSFORMS: TagInfoShorts = TagInfoShorts(
        0x206, "JPEGPointTransforms", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_QTABLES: TagInfoLongs = TagInfoLongs(
        0x207, "JPEGQTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_DCTABLES: TagInfoLongs = TagInfoLongs(
        0x208, "JPEGDCTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_JPEG_ACTABLES: TagInfoLongs = TagInfoLongs(
        0x209, "JPEGACTables", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_YCBCR_COEFFICIENTS: TagInfoRationals = TagInfoRationals(
        0x211, "YCbCrCoefficients", 3,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_YCBCR_SUB_SAMPLING: TagInfoShorts = TagInfoShorts(
        0x212, "YCbCrSubSampling", 2,
        TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_YCBCR_POSITIONING: TagInfoShort = TagInfoShort(
        0x213, "YCbCrPositioning",
        TIFF_DIRECTORY_IFD0
    )

    public const val YCB_CR_POSITIONING_VALUE_CENTERED: Int = 1
    public const val YCB_CR_POSITIONING_VALUE_CO_SITED: Int = 2

    public val TIFF_TAG_REFERENCE_BLACK_WHITE: TagInfoLongs = TagInfoLongs(
        0x214, "ReferenceBlackWhite", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_COPYRIGHT: TagInfoAscii = TagInfoAscii(
        0x8298, "Copyright", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    public val TIFF_TAG_XMP: TagInfoBytes = TagInfoBytes(
        0x2BC, "XMP", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    /** Panasonic RW2 special tag. */
    public val TIFF_TAG_JPG_FROM_RAW: TagInfoBytes = TagInfoBytes(
        0x002E, "JpgFromRaw", TagInfo.LENGTH_UNKNOWN, TIFF_DIRECTORY_IFD0
    )

    /** Required field for all DNGs. Can be used to detect if TIFF is a DNG. */
    public val TIFF_TAG_DNG_VERSION: TagInfoBytes = TagInfoBytes(
        0xC612, "DNGVersion", 4, TIFF_DIRECTORY_IFD0
    )

    public val ALL: List<TagInfo> = listOf(
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
