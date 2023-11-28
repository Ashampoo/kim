/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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
import com.ashampoo.kim.format.tiff.JpegImageData
import com.ashampoo.kim.format.tiff.TiffDirectory.Companion.description
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_DIRECTORY_FOOTER_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_DIRECTORY_HEADER_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_ENTRY_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import com.ashampoo.kim.format.tiff.taginfos.TagInfo
import com.ashampoo.kim.format.tiff.taginfos.TagInfoAscii
import com.ashampoo.kim.format.tiff.taginfos.TagInfoAsciiOrByte
import com.ashampoo.kim.format.tiff.taginfos.TagInfoAsciiOrRational
import com.ashampoo.kim.format.tiff.taginfos.TagInfoByte
import com.ashampoo.kim.format.tiff.taginfos.TagInfoByteOrShort
import com.ashampoo.kim.format.tiff.taginfos.TagInfoBytes
import com.ashampoo.kim.format.tiff.taginfos.TagInfoDouble
import com.ashampoo.kim.format.tiff.taginfos.TagInfoDoubles
import com.ashampoo.kim.format.tiff.taginfos.TagInfoFloat
import com.ashampoo.kim.format.tiff.taginfos.TagInfoFloats
import com.ashampoo.kim.format.tiff.taginfos.TagInfoGpsText
import com.ashampoo.kim.format.tiff.taginfos.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfos.TagInfoLongs
import com.ashampoo.kim.format.tiff.taginfos.TagInfoRational
import com.ashampoo.kim.format.tiff.taginfos.TagInfoRationals
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSByte
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSBytes
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSLong
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSLongs
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSRational
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSRationals
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSShort
import com.ashampoo.kim.format.tiff.taginfos.TagInfoSShorts
import com.ashampoo.kim.format.tiff.taginfos.TagInfoShort
import com.ashampoo.kim.format.tiff.taginfos.TagInfoShortOrLong
import com.ashampoo.kim.format.tiff.taginfos.TagInfoShortOrLongOrRational
import com.ashampoo.kim.format.tiff.taginfos.TagInfoShortOrRational
import com.ashampoo.kim.format.tiff.taginfos.TagInfoShorts
import com.ashampoo.kim.format.tiff.write.TiffOutputItem.Companion.UNDEFINED_VALUE
import com.ashampoo.kim.output.BinaryByteWriter

class TiffOutputDirectory(val type: Int, private val byteOrder: ByteOrder) : TiffOutputItem {

    private val fields = mutableListOf<TiffOutputField>()

    private var nextDirectory: TiffOutputDirectory? = null

    override var offset: Long = UNDEFINED_VALUE

    var rawJpegImageData: JpegImageData? = null
        private set

    fun setNextDirectory(nextDirectory: TiffOutputDirectory?) {
        this.nextDirectory = nextDirectory
    }

    private fun checkMatchingLength(tagInfo: TagInfo, length: Int) {

        if (tagInfo.length > 0 && tagInfo.length != length)
            throw ImageWriteException("Tag length is ${tagInfo.length}, parameter length was $length")
    }

    fun add(tagInfo: TagInfoByte, value: Byte) {

        val bytes = tagInfo.encodeValue(value)

        add(TiffOutputField(tagInfo.tag, tagInfo, FieldType.BYTE, bytes.size, bytes))
    }

    fun add(tagInfo: TagInfoBytes, bytes: ByteArray) {

        checkMatchingLength(tagInfo, bytes.size)

        add(TiffOutputField(tagInfo.tag, tagInfo, FieldType.BYTE, bytes.size, bytes))
    }

    fun add(tagInfo: TagInfoAscii, value: String) {

        val bytes = tagInfo.encodeValue(byteOrder, listOf(value))

        checkMatchingLength(tagInfo, bytes.size)

        add(TiffOutputField(tagInfo.tag, tagInfo, FieldType.ASCII, bytes.size, bytes))
    }

    fun add(tagInfo: TagInfoAscii, values: List<String>) {

        val bytes = tagInfo.encodeValue(byteOrder, values)

        checkMatchingLength(tagInfo, bytes.size)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.ASCII, bytes.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShort, value: Short) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.SHORT, 1, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.SHORT, values.size, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoLong, value: Int) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.LONG, 1, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.LONG, values.size, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoRational, value: RationalNumber) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.RATIONAL, 1, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoRationals, values: Array<RationalNumber>) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.RATIONAL, values.size, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSByte, value: Byte) {

        val bytes = tagInfo.encodeValue(value)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.SBYTE, 1, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSBytes, values: ByteArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SBYTE,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSShort, value: Short) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(tagInfo.tag, tagInfo, FieldType.SSHORT, 1, bytes)

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSShorts, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SSHORT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSLong, value: Int) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SLONG, 1, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSLongs, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SLONG,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSRational, value: RationalNumber) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SRATIONAL, 1, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoSRationals, values: Array<RationalNumber>) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SRATIONAL,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoFloat, value: Float) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.FLOAT, 1, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoFloats, values: FloatArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.FLOAT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoDouble, value: Double) {

        val bytes = tagInfo.encodeValue(byteOrder, value)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.DOUBLE, 1, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoDoubles, values: DoubleArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.DOUBLE,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoByteOrShort, values: ByteArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.BYTE, values.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoByteOrShort, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SHORT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrLong, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SHORT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrLong, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.LONG, values.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrLongOrRational, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SHORT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrLongOrRational, values: IntArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.LONG, values.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrLongOrRational, values: Array<RationalNumber>) {

        checkMatchingLength(tagInfo, values.size)

        val bytes: ByteArray = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.RATIONAL,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrRational, values: ShortArray) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.SHORT,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoShortOrRational, values: Array<RationalNumber>) {

        checkMatchingLength(tagInfo, values.size)

        val bytes: ByteArray = tagInfo.encodeValue(byteOrder, values)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.RATIONAL,
            values.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoGpsText, value: String) {

        val bytes = tagInfo.encodeValue(FieldType.UNDEFINED, value, byteOrder)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, tagInfo.dataTypes[0], bytes.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoAsciiOrByte, values: Array<String>) {

        val bytes = tagInfo.encodeValue(FieldType.ASCII, values, byteOrder)

        checkMatchingLength(tagInfo, bytes.size)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.ASCII, bytes.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoAsciiOrRational, values: Array<String>) {

        val bytes = tagInfo.encodeValue(FieldType.ASCII, values, byteOrder)

        checkMatchingLength(tagInfo, bytes.size)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.ASCII, bytes.size,
            bytes
        )

        add(tiffOutputField)
    }

    fun add(tagInfo: TagInfoAsciiOrRational, values: Array<RationalNumber>) {

        checkMatchingLength(tagInfo, values.size)

        val bytes = tagInfo.encodeValue(FieldType.RATIONAL, values, byteOrder)

        val tiffOutputField = TiffOutputField(
            tagInfo.tag,
            tagInfo, FieldType.RATIONAL,
            bytes.size, bytes
        )

        add(tiffOutputField)
    }

    fun add(field: TiffOutputField) {
        fields.add(field)
    }

    fun getFields(): List<TiffOutputField> = fields

    fun removeField(tagInfo: TagInfo) =
        removeField(tagInfo.tag)

    fun removeField(tag: Int) {

        val matches = mutableListOf<TiffOutputField>()

        for (field in fields)
            if (field.tag == tag)
                matches.add(field)

        fields.removeAll(matches)
    }

    fun findField(tagInfo: TagInfo): TiffOutputField? =
        findField(tagInfo.tag)

    fun findField(tag: Int): TiffOutputField? {

        for (field in fields)
            if (field.tag == tag)
                return field

        return null
    }

    fun sortFields() {

        val comparator = Comparator { e1: TiffOutputField, e2: TiffOutputField ->

            if (e1.tag != e2.tag)
                return@Comparator e1.tag - e2.tag

            e1.sortHint - e2.sortHint
        }

        fields.sortWith(comparator)
    }

    override fun writeItem(binaryByteWriter: BinaryByteWriter) {

        /* Write directory field count. */
        binaryByteWriter.write2Bytes(fields.size)

        for (field in fields)
            field.writeField(binaryByteWriter)

        var nextDirectoryOffset: Long = 0

        if (nextDirectory != null)
            nextDirectoryOffset = nextDirectory!!.offset

        if (nextDirectoryOffset == UNDEFINED_VALUE)
            binaryByteWriter.write4Bytes(0)
        else
            binaryByteWriter.write4Bytes(nextDirectoryOffset.toInt())
    }

    /* Internal, because callers should use setThumbnailBytes() */
    internal fun setJpegImageData(rawJpegImageData: JpegImageData?) {
        this.rawJpegImageData = rawJpegImageData
    }

    override fun getItemLength(): Int =
        TIFF_ENTRY_LENGTH * fields.size + TIFF_DIRECTORY_HEADER_LENGTH + TIFF_DIRECTORY_FOOTER_LENGTH

    private fun removeFieldIfPresent(tagInfo: TagInfo) =
        findField(tagInfo)?.let { field -> fields.remove(field) }

    fun getOutputItems(
        outputSummary: TiffOutputSummary
    ): List<TiffOutputItem> {

        /* First validate directory fields. */
        removeFieldIfPresent(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT)
        removeFieldIfPresent(TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)

        var jpegOffsetField: TiffOutputField? = null

        if (rawJpegImageData != null) {

            jpegOffsetField = TiffOutputField(
                TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT,
                FieldType.LONG, 1, ByteArray(TIFF_ENTRY_MAX_VALUE_LENGTH)
            )

            add(jpegOffsetField)

            val lengthValue = FieldType.LONG.writeData(rawJpegImageData!!.length, outputSummary.byteOrder)

            val jpegLengthField = TiffOutputField(
                TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT_LENGTH,
                FieldType.LONG, 1, lengthValue
            )

            add(jpegLengthField)
        }

        removeFieldIfPresent(TiffTag.TIFF_TAG_STRIP_OFFSETS)
        removeFieldIfPresent(TiffTag.TIFF_TAG_STRIP_BYTE_COUNTS)
        removeFieldIfPresent(TiffTag.TIFF_TAG_TILE_OFFSETS)
        removeFieldIfPresent(TiffTag.TIFF_TAG_TILE_BYTE_COUNTS)

        val result = mutableListOf<TiffOutputItem>()

        result.add(this)
        sortFields()

        for (field in fields) {

            if (field.isLocalValue)
                continue

            val item = field.separateValue!!

            result.add(item)
        }

        if (rawJpegImageData != null) {

            val item: TiffOutputItem = TiffOutputValue(
                "rawJpegImageData",
                rawJpegImageData!!.bytes
            )

            result.add(item)

            outputSummary.add(item, jpegOffsetField!!)
        }

        return result
    }

    override fun toString(): String =
        description(type)
}
