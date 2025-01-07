/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType.TIFF_DIRECTORY_IFD1
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType.TIFF_DIRECTORY_IFD2
import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType.TIFF_DIRECTORY_IFD3
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoDouble
import com.ashampoo.kim.format.tiff.taginfo.TagInfoGpsText
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLongs
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShort
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShorts
import com.ashampoo.kim.format.tiff.taginfo.TagInfoUndefined
import com.ashampoo.kim.format.tiff.taginfo.TagInfoUndefineds

/**
 * Standard Exif Tags as defined in EXIF 2.3 standard
 *
 * See https://exiv2.org/tags.html
 */
@Suppress("MagicNumber", "LargeClass", "StringLiteralDuplication")
public object ExifTag {

    /*
     * TODO This list is incomplete
     */

    internal val EXIF_DIRECTORY_UNKNOWN: TiffDirectoryType? = null

    public val EXIF_TAG_INTEROPERABILITY_INDEX: TagInfoAscii = TagInfoAscii(
        0x0001, "InteroperabilityIndex", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_INTEROP_IFD
    )

    public val EXIF_TAG_INTEROPERABILITY_VERSION: TagInfoUndefined = TagInfoUndefined(
        0x0002, "InteroperabilityVersion",
        TiffDirectoryType.EXIF_DIRECTORY_INTEROP_IFD
    )

    public val EXIF_TAG_INTEROPERABILITY_RELATED_IMAGE_WIDTH: TagInfoShort = TagInfoShort(
        0x1001, "RelatedImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_INTEROP_IFD
    )

    public val EXIF_TAG_INTEROPERABILITY_RELATED_IMAGE_HEIGHT: TagInfoShort = TagInfoShort(
        0x1002, "RelatedImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_INTEROP_IFD
    )

    public val EXIF_TAG_PROCESSING_SOFTWARE: TagInfoAscii = TagInfoAscii(
        0x000b, "ProcessingSoftware", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_SUB_IFDS_OFFSET: TagInfoLongs = TagInfoLongs(
        0x014a, "SubIFD", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    public val EXIF_TAG_SOFTWARE: TagInfoAscii = TagInfoAscii(
        0x0131, "Software", TagInfo.LENGTH_UNKNOWN,
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_PREVIEW_IMAGE_START_IFD0: TagInfoLong = TagInfoLong(
        0x0111, "PreviewImageStart",
        TIFF_DIRECTORY_IFD0,
        isOffset = true
    )

    public val EXIF_TAG_PREVIEW_IMAGE_START_SUB_IFD1: TagInfoLong = TagInfoLong(
        0x0111, "PreviewImageStart",
        TIFF_DIRECTORY_IFD2,
        isOffset = true
    )

    public val EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD2: TagInfoLong = TagInfoLong(
        0x0111, "JpgFromRawStart",
        TIFF_DIRECTORY_IFD3,
        isOffset = true
    )

    public val EXIF_TAG_PREVIEW_IMAGE_LENGTH_IFD0: TagInfoLong = TagInfoLong(
        0x0117, "PreviewImageLength",
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_PREVIEW_IMAGE_LENGTH_SUB_IFD1: TagInfoLong = TagInfoLong(
        0x0117, "PreviewImageLength",
        TIFF_DIRECTORY_IFD2
    )

    public val EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD2: TagInfoLong = TagInfoLong(
        0x0117, "JpgFromRawLength",
        TIFF_DIRECTORY_IFD3
    )

    public val EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD: TagInfoLong = TagInfoLong(
        0x0201, "JpgFromRawStart",
        TIFF_DIRECTORY_IFD1,
        isOffset = true
    )

    public val EXIF_TAG_JPG_FROM_RAW_START_IFD2: TagInfoLong = TagInfoLong(
        0x0201, "JpgFromRawStart",
        TIFF_DIRECTORY_IFD2,
        isOffset = true
    )

    public val EXIF_TAG_OTHER_IMAGE_START: TagInfoLong = TagInfoLong(
        0x0201, "OtherImageStart",
        EXIF_DIRECTORY_UNKNOWN,
        isOffset = true
    )

    public val EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD: TagInfoLong = TagInfoLong(
        0x0202, "JpgFromRawLength",
        TIFF_DIRECTORY_IFD1
    )

    public val EXIF_TAG_JPG_FROM_RAW_LENGTH_IFD2: TagInfoLong = TagInfoLong(
        0x0202, "JpgFromRawLength",
        TIFF_DIRECTORY_IFD2
    )

    public val EXIF_TAG_OTHER_IMAGE_LENGTH: TagInfoLong = TagInfoLong(
        0x0202, "OtherImageLength",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_APPLICATION_NOTES: TagInfoBytes = TagInfoBytes(
        0x02bc, "ApplicationNotes", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_EXPOSURE_TIME: TagInfoRationals = TagInfoRationals(
        0x829a, "ExposureTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_FNUMBER: TagInfoRationals = TagInfoRationals(
        0x829d, "FNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_IPTC_NAA: TagInfoLong = TagInfoLong(
        0x83bb, "IPTC-NAA",
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_INTERGRAPH_PACKET_DATA: TagInfoShorts = TagInfoShorts(
        0x847e, "IntergraphPacketData", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_INTERGRAPH_FLAG_REGISTERS: TagInfoLongs = TagInfoLongs(
        0x847f, "IntergraphFlagRegisters", 16,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_SITE: TagInfoAscii = TagInfoAscii(
        0x84e0, "Site", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_COLOR_SEQUENCE: TagInfoAscii = TagInfoAscii(
        0x84e1, "ColorSequence", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_IT8HEADER: TagInfoAscii = TagInfoAscii(
        0x84e2, "IT8Header", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_RASTER_PADDING: TagInfoShort = TagInfoShort(
        0x84e3, "RasterPadding",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_BITS_PER_RUN_LENGTH: TagInfoShort = TagInfoShort(
        0x84e4, "BitsPerRunLength",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_BITS_PER_EXTENDED_RUN_LENGTH: TagInfoShort = TagInfoShort(
        0x84e5, "BitsPerExtendedRunLength",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_COLOR_TABLE: TagInfoBytes = TagInfoBytes(
        0x84e6, "ColorTable", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_IMAGE_COLOR_INDICATOR: TagInfoByte = TagInfoByte(
        0x84e7, "ImageColorIndicator",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_BACKGROUND_COLOR_INDICATOR: TagInfoByte = TagInfoByte(
        0x84e8, "BackgroundColorIndicator",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_IMAGE_COLOR_VALUE: TagInfoBytes = TagInfoBytes(
        0x84e9, "ImageColorValue", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_BACKGROUND_COLOR_VALUE: TagInfoBytes = TagInfoBytes(
        0x84ea, "BackgroundColorValue", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_PIXEL_INTENSITY_RANGE: TagInfoBytes = TagInfoBytes(
        0x84eb, "PixelIntensityRange", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_TRANSPARENCY_INDICATOR: TagInfoByte = TagInfoByte(
        0x84ec, "TransparencyIndicator",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_COLOR_CHARACTERIZATION: TagInfoAscii = TagInfoAscii(
        0x84ed, "ColorCharacterization", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_SEMINFO: TagInfoAscii = TagInfoAscii(
        0x8546, "SEMInfo", 1,
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_AFCP_IPTC: TagInfoLong = TagInfoLong(
        0x8568, "AFCP_IPTC",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_LEAF_DATA: TagInfoLong = TagInfoLong(
        0x8606, "LeafData",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_PHOTOSHOP_SETTINGS: TagInfoBytes = TagInfoBytes(
        0x8649, "PhotoshopSettings", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_EXIF_OFFSET: TagInfoLong = TagInfoLong(
        0x8769, "ExifOffset",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_EXPOSURE_PROGRAM: TagInfoShort = TagInfoShort(
        0x8822, "ExposureProgram",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val EXPOSURE_PROGRAM_VALUE_MANUAL: Int = 1
    public const val EXPOSURE_PROGRAM_VALUE_PROGRAM_AE: Int = 2
    public const val EXPOSURE_PROGRAM_VALUE_APERTURE_PRIORITY_AE: Int = 3
    public const val EXPOSURE_PROGRAM_VALUE_SHUTTER_SPEED_PRIORITY_AE: Int = 4
    public const val EXPOSURE_PROGRAM_VALUE_CREATIVE_SLOW_SPEED: Int = 5
    public const val EXPOSURE_PROGRAM_VALUE_ACTION_HIGH_SPEED: Int = 6
    public const val EXPOSURE_PROGRAM_VALUE_PORTRAIT: Int = 7
    public const val EXPOSURE_PROGRAM_VALUE_LANDSCAPE: Int = 8

    public val EXIF_TAG_SPECTRAL_SENSITIVITY: TagInfoAscii = TagInfoAscii(
        0x8824, "SpectralSensitivity", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_GPSINFO: TagInfoLong = TagInfoLong(
        0x8825, "GPSInfo",
        EXIF_DIRECTORY_UNKNOWN,
        isOffset = true
    )

    /**
     * The ISO field, which has several names in different specs.
     *
     * EXIF 2.2: "ISOSpeedRatings"
     * EXIF 2.3: "PhotographicSensitivity"
     * ExifTool: "ISO"
     */
    public val EXIF_TAG_ISO: TagInfoShorts = TagInfoShorts(
        0x8827, "ISO", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    /**
     * Panasonic RW2 writes the ISO value to IFD0
     * instead of writing to the standard ExifIFD field.
     *
     * See https://exiftool.org/TagNames/PanasonicRaw.html
     */
    public val EXIF_TAG_ISO_PANASONIC: TagInfoShort = TagInfoShort(
        0x0017, "ISO",
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_OPTO_ELECTRIC_CONV_FACTOR: TagInfoUndefineds = TagInfoUndefineds(
        0x8828, "Opto - Electric Conv Factor", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_LEAF_SUB_IFD: TagInfoLong = TagInfoLong(
        0x888a, "LeafSubIFD",
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_EXIF_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0x9000, "ExifVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_DATE_TIME_ORIGINAL: TagInfoAscii = TagInfoAscii(
        0x9003, "DateTimeOriginal", 20,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_DATE_TIME_DIGITIZED: TagInfoAscii = TagInfoAscii(
        0x9004, "DateTimeDigitized", 20,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_COMPONENTS_CONFIGURATION: TagInfoUndefineds = TagInfoUndefineds(
        0x9101, "ComponentsConfiguration", 4,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_COMPRESSED_BITS_PER_PIXEL: TagInfoRational = TagInfoRational(
        0x9102, "CompressedBitsPerPixel",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SHUTTER_SPEED_VALUE: TagInfoSRational = TagInfoSRational(
        0x9201, "ShutterSpeedValue",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_APERTURE_VALUE: TagInfoRational = TagInfoRational(
        0x9202, "ApertureValue",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_BRIGHTNESS_VALUE: TagInfoSRational = TagInfoSRational(
        0x9203, "BrightnessValue",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_EXPOSURE_COMPENSATION: TagInfoSRational = TagInfoSRational(
        0x9204, "ExposureCompensation",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_MAX_APERTURE_VALUE: TagInfoRational = TagInfoRational(
        0x9205, "MaxApertureValue",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SUBJECT_DISTANCE: TagInfoRationals = TagInfoRationals(
        0x9206, "Subject Distance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_METERING_MODE: TagInfoShort = TagInfoShort(
        0x9207, "MeteringMode",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val METERING_MODE_VALUE_AVERAGE: Int = 1
    public const val METERING_MODE_VALUE_CENTER_WEIGHTED_AVERAGE: Int = 2
    public const val METERING_MODE_VALUE_SPOT: Int = 3
    public const val METERING_MODE_VALUE_MULTI_SPOT: Int = 4
    public const val METERING_MODE_VALUE_MULTI_SEGMENT: Int = 5
    public const val METERING_MODE_VALUE_PARTIAL: Int = 6
    public const val METERING_MODE_VALUE_OTHER: Int = 255

    public val EXIF_TAG_LIGHT_SOURCE: TagInfoShort = TagInfoShort(
        0x9208, "LightSource",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val LIGHT_SOURCE_VALUE_DAYLIGHT: Int = 1
    public const val LIGHT_SOURCE_VALUE_FLUORESCENT: Int = 2
    public const val LIGHT_SOURCE_VALUE_TUNGSTEN: Int = 3
    public const val LIGHT_SOURCE_VALUE_FLASH: Int = 4
    public const val LIGHT_SOURCE_VALUE_FINE_WEATHER: Int = 9
    public const val LIGHT_SOURCE_VALUE_CLOUDY: Int = 10
    public const val LIGHT_SOURCE_VALUE_SHADE: Int = 11
    public const val LIGHT_SOURCE_VALUE_DAYLIGHT_FLUORESCENT: Int = 12
    public const val LIGHT_SOURCE_VALUE_DAY_WHITE_FLUORESCENT: Int = 13
    public const val LIGHT_SOURCE_VALUE_COOL_WHITE_FLUORESCENT: Int = 14
    public const val LIGHT_SOURCE_VALUE_WHITE_FLUORESCENT: Int = 15
    public const val LIGHT_SOURCE_VALUE_STANDARD_LIGHT_A: Int = 17
    public const val LIGHT_SOURCE_VALUE_STANDARD_LIGHT_B: Int = 18
    public const val LIGHT_SOURCE_VALUE_STANDARD_LIGHT_C: Int = 19
    public const val LIGHT_SOURCE_VALUE_D55: Int = 20
    public const val LIGHT_SOURCE_VALUE_D65: Int = 21
    public const val LIGHT_SOURCE_VALUE_D75: Int = 22
    public const val LIGHT_SOURCE_VALUE_D50: Int = 23
    public const val LIGHT_SOURCE_VALUE_ISO_STUDIO_TUNGSTEN: Int = 24
    public const val LIGHT_SOURCE_VALUE_OTHER: Int = 255

    public val EXIF_TAG_FLASH: TagInfoShort = TagInfoShort(
        0x9209, "Flash",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val FLASH_VALUE_NO_FLASH: Int = 0x0
    public const val FLASH_VALUE_FIRED: Int = 0x1
    public const val FLASH_VALUE_FIRED_RETURN_NOT_DETECTED: Int = 0x5
    public const val FLASH_VALUE_FIRED_RETURN_DETECTED: Int = 0x7
    public const val FLASH_VALUE_ON_DID_NOT_FIRE: Int = 0x8
    public const val FLASH_VALUE_ON: Int = 0x9
    public const val FLASH_VALUE_ON_RETURN_NOT_DETECTED: Int = 0xd
    public const val FLASH_VALUE_ON_RETURN_DETECTED: Int = 0xf
    public const val FLASH_VALUE_OFF: Int = 0x10
    public const val FLASH_VALUE_OFF_DID_NOT_FIRE_RETURN_NOT_DETECTED: Int = 0x14
    public const val FLASH_VALUE_AUTO_DID_NOT_FIRE: Int = 0x18
    public const val FLASH_VALUE_AUTO_FIRED: Int = 0x19
    public const val FLASH_VALUE_AUTO_FIRED_RETURN_NOT_DETECTED: Int = 0x1d
    public const val FLASH_VALUE_AUTO_FIRED_RETURN_DETECTED: Int = 0x1f
    public const val FLASH_VALUE_NO_FLASH_FUNCTION: Int = 0x20
    public const val FLASH_VALUE_OFF_NO_FLASH_FUNCTION: Int = 0x30
    public const val FLASH_VALUE_FIRED_RED_EYE_REDUCTION: Int = 0x41
    public const val FLASH_VALUE_FIRED_RED_EYE_REDUCTION_RETURN_NOT_DETECTED: Int = 0x45
    public const val FLASH_VALUE_FIRED_RED_EYE_REDUCTION_RETURN_DETECTED: Int = 0x47
    public const val FLASH_VALUE_ON_RED_EYE_REDUCTION: Int = 0x49
    public const val FLASH_VALUE_ON_RED_EYE_REDUCTION_RETURN_NOT_DETECTED: Int = 0x4d
    public const val FLASH_VALUE_ON_RED_EYE_REDUCTION_RETURN_DETECTED: Int = 0x4f
    public const val FLASH_VALUE_OFF_RED_EYE_REDUCTION: Int = 0x50
    public const val FLASH_VALUE_AUTO_DID_NOT_FIRE_RED_EYE_REDUCTION: Int = 0x58
    public const val FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION: Int = 0x59
    public const val FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION_RETURN_NOT_DETECTED: Int = 0x5d
    public const val FLASH_VALUE_AUTO_FIRED_RED_EYE_REDUCTION_RETURN_DETECTED: Int = 0x5f

    public val EXIF_TAG_FOCAL_LENGTH: TagInfoRationals = TagInfoRationals(
        0x920a, "FocalLength", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SUBJECT_AREA: TagInfoShorts = TagInfoShorts(
        0x9214, "SubjectArea", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_STO_NITS: TagInfoDouble = TagInfoDouble(
        0x923f, "StoNits",
        EXIF_DIRECTORY_UNKNOWN
    )

    /**
     * A tag for manufacturers of Exif writers to record any desired information.
     * The contents are up to the manufacturer.
     */
    public val EXIF_TAG_MAKER_NOTE: TagInfoUndefineds = TagInfoUndefineds(
        0x927c, "MakerNote", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_USER_COMMENT: TagInfoGpsText = TagInfoGpsText(
        0x9286, "UserComment",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SUB_SEC_TIME: TagInfoAscii = TagInfoAscii(
        0x9290, "SubSecTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SUB_SEC_TIME_ORIGINAL: TagInfoAscii = TagInfoAscii(
        0x9291, "SubSecTimeOriginal", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SUB_SEC_TIME_DIGITIZED: TagInfoAscii = TagInfoAscii(
        0x9292, "SubSecTimeDigitized", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_OFFSET_TIME: TagInfoAscii = TagInfoAscii(
        0x9010, "OffsetTime", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_OFFSET_TIME_ORIGINAL: TagInfoAscii = TagInfoAscii(
        0x9011, "OffsetTimeOriginal", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_FLASHPIX_VERSION: TagInfoUndefineds = TagInfoUndefineds(
        0xa000, "FlashpixVersion", 4,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_EXIF_IMAGE_WIDTH: TagInfoLong = TagInfoLong(
        0xa002, "ExifImageWidth",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_EXIF_IMAGE_HEIGHT: TagInfoLong = TagInfoLong(
        0xa003, "ExifImageHeight",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_RELATED_SOUND_FILE: TagInfoAscii = TagInfoAscii(
        0xa004, "RelatedSoundFile", 13,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_INTEROP_OFFSET: TagInfoLong = TagInfoLong(
        0xa005, "InteropOffset",
        EXIF_DIRECTORY_UNKNOWN,
        isOffset = true
    )

    public val EXIF_TAG_FLASH_ENERGY_EXIF_IFD: TagInfoRationals = TagInfoRationals(
        0xa20b, "FlashEnergy", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SPATIAL_FREQUENCY_RESPONSE_2: TagInfoUndefineds = TagInfoUndefineds(
        0xa20c, "SpatialFrequencyResponse", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_FOCAL_PLANE_XRESOLUTION_EXIF_IFD: TagInfoRational = TagInfoRational(
        0xa20e, "FocalPlaneXResolution",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_FOCAL_PLANE_YRESOLUTION_EXIF_IFD: TagInfoRational = TagInfoRational(
        0xa20f, "FocalPlaneYResolution",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD: TagInfoShort = TagInfoShort(
        0xa210, "FocalPlaneResolutionUnit",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD_VALUE_NONE: Int = 1
    public const val FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD_VALUE_INCHES: Int = 2
    public const val FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD_VALUE_CM: Int = 3
    public const val FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD_VALUE_MM: Int = 4
    public const val FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD_VALUE_UM: Int = 5

    public val EXIF_TAG_SUBJECT_LOCATION: TagInfoShorts = TagInfoShorts(
        0xa214, "SubjectLocation", 2,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_EXPOSURE_INDEX_EXIF_IFD: TagInfoRational = TagInfoRational(
        0xa215, "ExposureIndex",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SENSING_METHOD_EXIF_IFD: TagInfoShort = TagInfoShort(
        0xa217, "SensingMethod",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val SENSING_METHOD_EXIF_IFD_VALUE_NOT_DEFINED: Int = 1
    public const val SENSING_METHOD_EXIF_IFD_VALUE_ONE_CHIP_COLOR_AREA: Int = 2
    public const val SENSING_METHOD_EXIF_IFD_VALUE_TWO_CHIP_COLOR_AREA: Int = 3
    public const val SENSING_METHOD_EXIF_IFD_VALUE_THREE_CHIP_COLOR_AREA: Int = 4
    public const val SENSING_METHOD_EXIF_IFD_VALUE_COLOR_SEQUENTIAL_AREA: Int = 5
    public const val SENSING_METHOD_EXIF_IFD_VALUE_TRILINEAR: Int = 7
    public const val SENSING_METHOD_EXIF_IFD_VALUE_COLOR_SEQUENTIAL_LINEAR: Int = 8

    public val EXIF_TAG_FILE_SOURCE: TagInfoUndefined = TagInfoUndefined(
        0xa300, "FileSource",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val FILE_SOURCE_VALUE_FILM_SCANNER: Int = 1
    public const val FILE_SOURCE_VALUE_REFLECTION_PRINT_SCANNER: Int = 2
    public const val FILE_SOURCE_VALUE_DIGITAL_CAMERA: Int = 3

    public val EXIF_TAG_SCENE_TYPE: TagInfoUndefined = TagInfoUndefined(
        0xa301, "SceneType",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_CFAPATTERN: TagInfoUndefineds = TagInfoUndefineds(
        0xa302, "CFAPattern", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_CUSTOM_RENDERED: TagInfoShort = TagInfoShort(
        0xa401, "CustomRendered",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val CUSTOM_RENDERED_VALUE_NORMAL: Int = 0
    public const val CUSTOM_RENDERED_VALUE_CUSTOM: Int = 1

    public val EXIF_TAG_EXPOSURE_MODE: TagInfoShort = TagInfoShort(
        0xa402, "ExposureMode",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val EXPOSURE_MODE_VALUE_AUTO: Int = 0
    public const val EXPOSURE_MODE_VALUE_MANUAL: Int = 1
    public const val EXPOSURE_MODE_VALUE_AUTO_BRACKET: Int = 2

    public val EXIF_TAG_WHITE_BALANCE_1: TagInfoShort = TagInfoShort(
        0xa403, "WhiteBalance",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val WHITE_BALANCE_1_VALUE_AUTO: Int = 0
    public const val WHITE_BALANCE_1_VALUE_MANUAL: Int = 1

    public val EXIF_TAG_DIGITAL_ZOOM_RATIO: TagInfoRational = TagInfoRational(
        0xa404, "DigitalZoomRatio",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT: TagInfoShort = TagInfoShort(
        0xa405, "FocalLengthIn35mmFormat",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SCENE_CAPTURE_TYPE: TagInfoShort = TagInfoShort(
        0xa406, "SceneCaptureType",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val SCENE_CAPTURE_TYPE_VALUE_STANDARD: Int = 0
    public const val SCENE_CAPTURE_TYPE_VALUE_LANDSCAPE: Int = 1
    public const val SCENE_CAPTURE_TYPE_VALUE_PORTRAIT: Int = 2
    public const val SCENE_CAPTURE_TYPE_VALUE_NIGHT: Int = 3

    public val EXIF_TAG_GAIN_CONTROL: TagInfoShort = TagInfoShort(
        0xa407, "GainControl",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val GAIN_CONTROL_VALUE_NONE: Int = 0
    public const val GAIN_CONTROL_VALUE_LOW_GAIN_UP: Int = 1
    public const val GAIN_CONTROL_VALUE_HIGH_GAIN_UP: Int = 2
    public const val GAIN_CONTROL_VALUE_LOW_GAIN_DOWN: Int = 3
    public const val GAIN_CONTROL_VALUE_HIGH_GAIN_DOWN: Int = 4

    public val EXIF_TAG_CONTRAST_1: TagInfoShort = TagInfoShort(
        0xa408, "Contrast",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val CONTRAST_1_VALUE_NORMAL: Int = 0
    public const val CONTRAST_1_VALUE_LOW: Int = 1
    public const val CONTRAST_1_VALUE_HIGH: Int = 2

    public val EXIF_TAG_SATURATION_1: TagInfoShort = TagInfoShort(
        0xa409, "Saturation",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val SATURATION_1_VALUE_NORMAL: Int = 0
    public const val SATURATION_1_VALUE_LOW: Int = 1
    public const val SATURATION_1_VALUE_HIGH: Int = 2

    public val EXIF_TAG_SHARPNESS_1: TagInfoShort = TagInfoShort(
        0xa40a, "Sharpness",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val SHARPNESS_1_VALUE_NORMAL: Int = 0
    public const val SHARPNESS_1_VALUE_SOFT: Int = 1
    public const val SHARPNESS_1_VALUE_HARD: Int = 2

    public val EXIF_TAG_DEVICE_SETTING_DESCRIPTION: TagInfoUndefineds = TagInfoUndefineds(
        0xa40b, "DeviceSettingDescription", TagInfo.LENGTH_UNKNOWN,
        EXIF_DIRECTORY_UNKNOWN
    )

    public val EXIF_TAG_SUBJECT_DISTANCE_RANGE: TagInfoShort = TagInfoShort(
        0xa40c, "SubjectDistanceRange",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public const val SUBJECT_DISTANCE_RANGE_VALUE_MACRO: Int = 1
    public const val SUBJECT_DISTANCE_RANGE_VALUE_CLOSE: Int = 2
    public const val SUBJECT_DISTANCE_RANGE_VALUE_DISTANT: Int = 3

    public val EXIF_TAG_IMAGE_UNIQUE_ID: TagInfoAscii = TagInfoAscii(
        0xa420, "ImageUniqueID", 33,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_CAMERA_OWNER_NAME: TagInfoAscii = TagInfoAscii(
        0xa430, "CameraOwnerName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_BODY_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0xa431, "BodySerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_LENS_SPECIFICATION: TagInfoRationals = TagInfoRationals(
        0xa432, "LensSpecification", 4,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_LENS_MAKE: TagInfoAscii = TagInfoAscii(
        0xa433, "LensMake", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_LENS_MODEL: TagInfoAscii = TagInfoAscii(
        0xa434, "LensModel", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_LENS_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0xa435, "LensSerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_GAMMA: TagInfoRational = TagInfoRational(
        0xa500, "Gamma",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_PRINT_IM: TagInfoUndefined = TagInfoUndefined(
        0xc4a5, "PrintIM",
        TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_OFFSET_SCHEMA: TagInfoSLong = TagInfoSLong(
        0xea1d, "OffsetSchema",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_OWNER_NAME: TagInfoAscii = TagInfoAscii(
        0xfde8, "OwnerName", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SERIAL_NUMBER: TagInfoAscii = TagInfoAscii(
        0xfde9, "SerialNumber", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_LENS: TagInfoAscii = TagInfoAscii(
        0xfdea, "Lens", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_RAW_FILE: TagInfoAscii = TagInfoAscii(
        0xfe4c, "RawFile", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_CONVERTER: TagInfoAscii = TagInfoAscii(
        0xfe4d, "Converter", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_WHITE_BALANCE_2: TagInfoAscii = TagInfoAscii(
        0xfe4e, "WhiteBalance", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_EXPOSURE: TagInfoAscii = TagInfoAscii(
        0xfe51, "Exposure", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SHADOWS: TagInfoAscii = TagInfoAscii(
        0xfe52, "Shadows", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_BRIGHTNESS: TagInfoAscii = TagInfoAscii(
        0xfe53, "Brightness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_CONTRAST_2: TagInfoAscii = TagInfoAscii(
        0xfe54, "Contrast", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SATURATION_2: TagInfoAscii = TagInfoAscii(
        0xfe55, "Saturation", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SHARPNESS_2: TagInfoAscii = TagInfoAscii(
        0xfe56, "Sharpness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_SMOOTHNESS: TagInfoAscii = TagInfoAscii(
        0xfe57, "Smoothness", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_MOIRE_FILTER: TagInfoAscii = TagInfoAscii(
        0xfe58, "MoireFilter", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    /** Rating tag used by Windows */
    public val EXIF_TAG_RATING: TagInfoShort = TagInfoShort(
        0x4746, "Rating",
        TiffDirectoryType.TIFF_DIRECTORY_IFD0
    )

    /** Rating tag used by Windows, value in percent */
    public val EXIF_TAG_RATING_PERCENT: TagInfoShort = TagInfoShort(
        0x4749, "RatingPercent",
        TiffDirectoryType.TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_MODIFY_DATE: TagInfoAscii = TagInfoAscii(
        0x0132, "ModifyDate", TagInfo.LENGTH_UNKNOWN,
        TiffDirectoryType.TIFF_DIRECTORY_IFD0
    )

    public val EXIF_TAG_SENSITIVITY_TYPE: TagInfoShort = TagInfoShort(
        0x8830, "SensitivityType",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_RECOMMENDED_EXPOSURE_INDEX: TagInfoLong = TagInfoLong(
        0x8832, "RecommendedExposureIndex",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_COLOR_SPACE: TagInfoShort = TagInfoShort(
        0xa001, "ColorSpace",
        TiffDirectoryType.EXIF_DIRECTORY_EXIF_IFD
    )

    public val EXIF_TAG_ICC_PROFILE_OFFSET: TagInfoUndefined = TagInfoUndefined(
        0x8773, "ICC_Profile",
        TIFF_DIRECTORY_IFD0
    )

    /* Affinity Photo creates it's own tag with custom data. */
    public val EXIF_TAG_AFFINITY_PHOTO_OFFSET: TagInfoUndefined = TagInfoUndefined(
        0xC7E0, "AffinityPhoto",
        TIFF_DIRECTORY_IFD0
    )

    /*
     * Page 18 of the XMPSpecificationPart1.pdf:
     * When XMP is embedded within digital files, including white-space padding
     * is sometimes helpful. Doing so facilitates modification of the XMP packet
     * in-place. The rest of the file is unaffected, which could eliminate a need
     * to rewrite the entire file if the XMP changes in size. Appropriate padding
     * is SPACE characters placed anywhere white space is allowed by the general
     * XML syntax and XMP serialization rules, with a linefeed (U+000A) every
     * 100 characters or so to improve human display. The amount of padding is
     * workflow-dependent; around 2000 bytes is often a reasonable amount.
     */
    public val EXIF_TAG_PADDING: TagInfoUndefined = TagInfoUndefined(
        0xEA1C, "Padding",
        TIFF_DIRECTORY_IFD0
    )

    public val ALL: List<TagInfo> = listOf(
        EXIF_TAG_INTEROPERABILITY_INDEX, EXIF_TAG_INTEROPERABILITY_VERSION,
        EXIF_TAG_INTEROPERABILITY_RELATED_IMAGE_WIDTH,
        EXIF_TAG_INTEROPERABILITY_RELATED_IMAGE_HEIGHT,
        EXIF_TAG_PROCESSING_SOFTWARE,
        EXIF_TAG_SOFTWARE,
        EXIF_TAG_PREVIEW_IMAGE_START_IFD0,
        EXIF_TAG_PREVIEW_IMAGE_START_SUB_IFD1,
        EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD2,
        EXIF_TAG_PREVIEW_IMAGE_LENGTH_IFD0,
        EXIF_TAG_PREVIEW_IMAGE_LENGTH_SUB_IFD1,
        EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD2,
        EXIF_TAG_JPG_FROM_RAW_START_SUB_IFD,
        EXIF_TAG_JPG_FROM_RAW_START_IFD2, EXIF_TAG_OTHER_IMAGE_START,
        EXIF_TAG_JPG_FROM_RAW_LENGTH_SUB_IFD,
        EXIF_TAG_JPG_FROM_RAW_LENGTH_IFD2, EXIF_TAG_OTHER_IMAGE_LENGTH,
        EXIF_TAG_APPLICATION_NOTES,
        EXIF_TAG_EXPOSURE_TIME,
        EXIF_TAG_FNUMBER, EXIF_TAG_IPTC_NAA,
        EXIF_TAG_INTERGRAPH_PACKET_DATA,
        EXIF_TAG_INTERGRAPH_FLAG_REGISTERS,
        EXIF_TAG_SITE, EXIF_TAG_COLOR_SEQUENCE,
        EXIF_TAG_IT8HEADER, EXIF_TAG_RASTER_PADDING,
        EXIF_TAG_BITS_PER_RUN_LENGTH,
        EXIF_TAG_BITS_PER_EXTENDED_RUN_LENGTH, EXIF_TAG_COLOR_TABLE,
        EXIF_TAG_IMAGE_COLOR_INDICATOR,
        EXIF_TAG_BACKGROUND_COLOR_INDICATOR, EXIF_TAG_IMAGE_COLOR_VALUE,
        EXIF_TAG_BACKGROUND_COLOR_VALUE, EXIF_TAG_PIXEL_INTENSITY_RANGE,
        EXIF_TAG_TRANSPARENCY_INDICATOR, EXIF_TAG_COLOR_CHARACTERIZATION,
        EXIF_TAG_SEMINFO, EXIF_TAG_AFCP_IPTC,
        EXIF_TAG_LEAF_DATA,
        EXIF_TAG_PHOTOSHOP_SETTINGS, EXIF_TAG_EXIF_OFFSET,
        EXIF_TAG_EXPOSURE_PROGRAM,
        EXIF_TAG_SPECTRAL_SENSITIVITY, EXIF_TAG_GPSINFO,
        EXIF_TAG_ISO, EXIF_TAG_ISO_PANASONIC,
        EXIF_TAG_OPTO_ELECTRIC_CONV_FACTOR,
        EXIF_TAG_LEAF_SUB_IFD,
        EXIF_TAG_EXIF_VERSION, EXIF_TAG_DATE_TIME_ORIGINAL,
        EXIF_TAG_DATE_TIME_DIGITIZED, EXIF_TAG_COMPONENTS_CONFIGURATION,
        EXIF_TAG_COMPRESSED_BITS_PER_PIXEL, EXIF_TAG_SHUTTER_SPEED_VALUE,
        EXIF_TAG_APERTURE_VALUE, EXIF_TAG_BRIGHTNESS_VALUE,
        EXIF_TAG_EXPOSURE_COMPENSATION, EXIF_TAG_MAX_APERTURE_VALUE,
        EXIF_TAG_SUBJECT_DISTANCE, EXIF_TAG_IMAGE_UNIQUE_ID,
        EXIF_TAG_CAMERA_OWNER_NAME,
        EXIF_TAG_BODY_SERIAL_NUMBER,
        EXIF_TAG_LENS_SPECIFICATION,
        EXIF_TAG_LENS_MAKE,
        EXIF_TAG_LENS_MODEL,
        EXIF_TAG_LENS_SERIAL_NUMBER,
        EXIF_TAG_METERING_MODE,
        EXIF_TAG_LIGHT_SOURCE, EXIF_TAG_FLASH, EXIF_TAG_FOCAL_LENGTH,
        EXIF_TAG_SUBJECT_AREA,
        EXIF_TAG_STO_NITS, EXIF_TAG_SUB_SEC_TIME,
        EXIF_TAG_SUB_SEC_TIME_ORIGINAL, EXIF_TAG_SUB_SEC_TIME_DIGITIZED,
        EXIF_TAG_OFFSET_TIME, EXIF_TAG_OFFSET_TIME_ORIGINAL,
        EXIF_TAG_FLASHPIX_VERSION,
        EXIF_TAG_EXIF_IMAGE_WIDTH, EXIF_TAG_EXIF_IMAGE_HEIGHT,
        EXIF_TAG_RELATED_SOUND_FILE, EXIF_TAG_INTEROP_OFFSET,
        EXIF_TAG_FLASH_ENERGY_EXIF_IFD,
        EXIF_TAG_SPATIAL_FREQUENCY_RESPONSE_2,
        EXIF_TAG_FOCAL_PLANE_XRESOLUTION_EXIF_IFD,
        EXIF_TAG_FOCAL_PLANE_YRESOLUTION_EXIF_IFD,
        EXIF_TAG_FOCAL_PLANE_RESOLUTION_UNIT_EXIF_IFD,
        EXIF_TAG_SUBJECT_LOCATION,
        EXIF_TAG_EXPOSURE_INDEX_EXIF_IFD,
        EXIF_TAG_SENSING_METHOD_EXIF_IFD, EXIF_TAG_FILE_SOURCE,
        EXIF_TAG_SCENE_TYPE, EXIF_TAG_CFAPATTERN, EXIF_TAG_CUSTOM_RENDERED,
        EXIF_TAG_EXPOSURE_MODE, EXIF_TAG_WHITE_BALANCE_1,
        EXIF_TAG_DIGITAL_ZOOM_RATIO, EXIF_TAG_FOCAL_LENGTH_IN_35MM_FORMAT,
        EXIF_TAG_SCENE_CAPTURE_TYPE, EXIF_TAG_GAIN_CONTROL,
        EXIF_TAG_CONTRAST_1, EXIF_TAG_SATURATION_1, EXIF_TAG_SHARPNESS_1,
        EXIF_TAG_DEVICE_SETTING_DESCRIPTION,
        EXIF_TAG_SUBJECT_DISTANCE_RANGE, EXIF_TAG_IMAGE_UNIQUE_ID,
        EXIF_TAG_GAMMA,
        EXIF_TAG_PRINT_IM,
        EXIF_TAG_OFFSET_SCHEMA, EXIF_TAG_OWNER_NAME,
        EXIF_TAG_SERIAL_NUMBER, EXIF_TAG_LENS, EXIF_TAG_RAW_FILE,
        EXIF_TAG_CONVERTER, EXIF_TAG_WHITE_BALANCE_2, EXIF_TAG_EXPOSURE,
        EXIF_TAG_SHADOWS, EXIF_TAG_BRIGHTNESS, EXIF_TAG_CONTRAST_2,
        EXIF_TAG_SATURATION_2, EXIF_TAG_SHARPNESS_2, EXIF_TAG_SMOOTHNESS,
        EXIF_TAG_MOIRE_FILTER, EXIF_TAG_USER_COMMENT,
        EXIF_TAG_MAKER_NOTE, EXIF_TAG_RATING, EXIF_TAG_RATING_PERCENT,
        EXIF_TAG_SUB_IFDS_OFFSET, EXIF_TAG_MODIFY_DATE, EXIF_TAG_SENSITIVITY_TYPE,
        EXIF_TAG_RECOMMENDED_EXPOSURE_INDEX, EXIF_TAG_COLOR_SPACE,
        EXIF_TAG_ICC_PROFILE_OFFSET, EXIF_TAG_AFFINITY_PHOTO_OFFSET,
        EXIF_TAG_PADDING
    )
}
