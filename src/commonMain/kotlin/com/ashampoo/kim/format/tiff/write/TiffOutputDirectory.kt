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
package com.ashampoo.kim.format.tiff.write

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.RationalNumber
import com.ashampoo.kim.common.RationalNumbers
import com.ashampoo.kim.common.toBytes
import com.ashampoo.kim.format.tiff.TiffDirectory.Companion.description
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_FOOTER_LENGTH
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_HEADER_LENGTH
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_LENGTH
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeAscii
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeByte
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeDouble
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeFloat
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeLong
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeRational
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeSByte
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeSLong
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeSRational
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeSShort
import com.ashampoo.kim.format.tiff.fieldtype.FieldTypeShort
import com.ashampoo.kim.format.tiff.taginfo.TagInfo
import com.ashampoo.kim.format.tiff.taginfo.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfo.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoDouble
import com.ashampoo.kim.format.tiff.taginfo.TagInfoDoubles
import com.ashampoo.kim.format.tiff.taginfo.TagInfoFloat
import com.ashampoo.kim.format.tiff.taginfo.TagInfoFloats
import com.ashampoo.kim.format.tiff.taginfo.TagInfoGpsText
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLongs
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSByte
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSBytes
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSLongs
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSRational
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSRationals
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSShort
import com.ashampoo.kim.format.tiff.taginfo.TagInfoSShorts
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShort
import com.ashampoo.kim.format.tiff.taginfo.TagInfoShorts
import com.ashampoo.kim.format.tiff.write.TiffOutputItem.Companion.UNDEFINED_VALUE
import com.ashampoo.kim.output.BinaryByteWriter

@Suppress("TooManyFunctions", "MethodOverloading")
public class TiffOutputDirectory(
    public val type: Int,
    private val byteOrder: ByteOrder
) : TiffOutputItem {

    private val fields = mutableSetOf<TiffOutputField>()

    private var nextDirectory: TiffOutputDirectory? = null

    override var offset: Int = UNDEFINED_VALUE

    public var thumbnailBytes: ByteArray? = null
        private set

    public var tiffImageBytes: ByteArray? = null
        private set

    internal fun setNextDirectory(nextDirectory: TiffOutputDirectory?) {
        this.nextDirectory = nextDirectory
    }

    private fun checkMatchingLength(tagInfo: TagInfo, length: Int) {

        if (tagInfo.length > 0 && tagInfo.length != length)
            throw ImageWriteException("Tag length is ${tagInfo.length}, parameter length was $length")
    }

    public fun add(tagInfo: TagInfoByte, value: Byte) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeByte,
                count = 1,
                bytes = byteArrayOf(value)
            )
        )
    }

    public fun add(tagInfo: TagInfoBytes, bytes: ByteArray) {

        checkMatchingLength(tagInfo, bytes.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeByte,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoAscii, value: String) {

        val bytes = FieldTypeAscii.writeData(value, byteOrder)

        checkMatchingLength(tagInfo, bytes.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeAscii,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoShort, value: Short) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeShort,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeShort,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoLong, value: Int) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeLong,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeLong,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoRational, value: RationalNumber) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeRational,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoRationals, values: RationalNumbers) {

        checkMatchingLength(tagInfo, values.values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeRational,
                count = values.values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSByte, value: Byte) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSByte,
                count = 1,
                bytes = byteArrayOf(value)
            )
        )
    }

    public fun add(tagInfo: TagInfoSBytes, value: ByteArray) {

        checkMatchingLength(tagInfo, value.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSByte,
                count = value.size,
                bytes = value
            )
        )
    }

    public fun add(tagInfo: TagInfoSShort, value: Short) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeShort,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSShort,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSLong, value: Int) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSLong,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSLong,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSRational, value: RationalNumber) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSRational,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoSRationals, value: RationalNumbers) {

        checkMatchingLength(tagInfo, value.values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeSRational,
                count = value.values.size,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoFloat, value: Float) {

        val bytes = value.toBytes(byteOrder)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeFloat,
                count = 1,
                bytes = bytes
            )
        )
    }

    public fun add(tagInfo: TagInfoFloats, values: FloatArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeFloat,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoDouble, value: Double) {

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeDouble,
                count = 1,
                bytes = value.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoDoubles, values: DoubleArray) {

        checkMatchingLength(tagInfo, values.size)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = FieldTypeDouble,
                count = values.size,
                bytes = values.toBytes(byteOrder)
            )
        )
    }

    public fun add(tagInfo: TagInfoGpsText, value: String) {

        val bytes = tagInfo.encodeValue(value)

        add(
            TiffOutputField(
                tag = tagInfo.tag,
                fieldType = tagInfo.fieldType,
                count = bytes.size,
                bytes = bytes
            )
        )
    }

    public fun add(field: TiffOutputField): Boolean =
        fields.add(field)

    public fun getFields(): Set<TiffOutputField> =
        fields

    public fun removeField(tagInfo: TagInfo): Boolean =
        removeField(tagInfo.tag)

    public fun removeField(tag: Int): Boolean =
        fields.removeAll { it.tag == tag }

    public fun findField(tagInfo: TagInfo): TiffOutputField? =
        findField(tagInfo.tag)

    public fun findField(tag: Int): TiffOutputField? =
        fields.find { it.tag == tag }

    override fun writeItem(
        binaryByteWriter: BinaryByteWriter
    ) {

        /* Write directory field count. */
        binaryByteWriter.write2Bytes(fields.size)

        for (field in fields.sorted())
            field.writeField(binaryByteWriter)

        var nextDirectoryOffset = 0

        if (nextDirectory != null)
            nextDirectoryOffset = nextDirectory!!.offset

        if (nextDirectoryOffset == UNDEFINED_VALUE)
            binaryByteWriter.write4Bytes(0)
        else
            binaryByteWriter.write4Bytes(nextDirectoryOffset)
    }

    /* Internal, because callers should use setThumbnailBytes() */
    internal fun setThumbnailBytes(thumbnailBytes: ByteArray?) {
        this.thumbnailBytes = thumbnailBytes
    }

    internal fun setTiffImageBytes(tiffImageBytes: ByteArray?) {
        this.tiffImageBytes = tiffImageBytes
    }

    override fun getItemLength(): Int =
        TIFF_ENTRY_LENGTH * fields.size + TIFF_DIRECTORY_HEADER_LENGTH + TIFF_DIRECTORY_FOOTER_LENGTH

    private fun removeFieldIfPresent(tagInfo: TagInfo) =
        findField(tagInfo)?.let { field -> fields.remove(field) }

    internal fun getOutputItems(tiffOffsetItems: TiffOffsetItems): List<TiffOutputItem> {

        /* First remove old fields */
        removeFieldIfPresent(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)
        removeFieldIfPresent(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)

        var thumbnailOffsetField: TiffOutputField? = null

        if (thumbnailBytes != null) {

            thumbnailOffsetField = TiffOutputField(
                TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag,
                FieldTypeLong, 1,
                ByteArray(TIFF_ENTRY_MAX_VALUE_LENGTH)
            )

            add(thumbnailOffsetField)

            val lengthValue = FieldTypeLong.writeData(
                thumbnailBytes!!.size,
                tiffOffsetItems.byteOrder
            )

            val jpegLengthField = TiffOutputField(
                TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH.tag,
                FieldTypeLong, 1, lengthValue
            )

            add(jpegLengthField)
        }

        var stripOffsetField: TiffOutputField? = null

        if (tiffImageBytes != null) {

            removeFieldIfPresent(TiffTag.TIFF_TAG_STRIP_OFFSETS)
            removeFieldIfPresent(TiffTag.TIFF_TAG_ROWS_PER_STRIP)
            removeFieldIfPresent(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS)

            stripOffsetField = TiffOutputField(
                TiffTag.TIFF_TAG_STRIP_OFFSETS.tag,
                FieldTypeLong, 1,
                ByteArray(TIFF_ENTRY_MAX_VALUE_LENGTH)
            )

            add(stripOffsetField)

            val lengthValue = FieldTypeLong.writeData(
                tiffImageBytes!!.size,
                tiffOffsetItems.byteOrder
            )

            val stripByteCountsField = TiffOutputField(
                TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS.tag,
                FieldTypeLong, 1, lengthValue
            )

            add(stripByteCountsField)

            /* Set to MAX value. We combine all strips into one block. */
            val rowsPerStripValue = FieldTypeLong.writeData(
                Int.MAX_VALUE,
                tiffOffsetItems.byteOrder
            )

            val rowsPerStripField = TiffOutputField(
                TiffTag.TIFF_TAG_ROWS_PER_STRIP.tag,
                FieldTypeLong, 1, rowsPerStripValue
            )

            add(rowsPerStripField)
        }

        removeFieldIfPresent(TiffTag.TIFF_TAG_TILE_OFFSETS)
        removeFieldIfPresent(TiffTag.TIFF_TAG_TILE_BYTE_COUNTS)

        val result = mutableListOf<TiffOutputItem>()

        result.add(this)

        for (field in fields.sorted()) {

            if (field.isLocalValue)
                continue

            val item = field.separateValue!!

            result.add(item)
        }

        if (thumbnailBytes != null) {

            val item: TiffOutputItem = TiffOutputValue(
                "thumbnailImageDataElement",
                thumbnailBytes!!
            )

            result.add(item)

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(item, thumbnailOffsetField!!))
        }

        if (tiffImageBytes != null) {

            val item: TiffOutputItem = TiffOutputValue(
                "tiffImageDataElement",
                tiffImageBytes!!
            )

            result.add(item)

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(item, stripOffsetField!!))
        }

        return result
    }

    override fun toString(): String =
        description(type)
}
