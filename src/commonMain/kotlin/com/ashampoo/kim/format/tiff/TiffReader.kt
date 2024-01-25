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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.head
import com.ashampoo.kim.common.toInt
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffConstants.DIRECTORY_TYPE_SUB
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD1
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD2
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD3
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.format.tiff.fieldtype.FieldType.Companion.getFieldType
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLongs
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.RandomAccessByteReader

object TiffReader {

    private val offsetFields = listOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET,
        ExifTag.EXIF_TAG_GPSINFO,
        ExifTag.EXIF_TAG_INTEROP_OFFSET,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET
    )

    private val directoryTypeMap = mapOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET to TiffConstants.TIFF_EXIF_IFD,
        ExifTag.EXIF_TAG_GPSINFO to TiffConstants.TIFF_GPS,
        ExifTag.EXIF_TAG_INTEROP_OFFSET to TiffConstants.TIFF_INTEROP_IFD,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET to TiffConstants.DIRECTORY_TYPE_SUB
    )

    /**
     * Convenience method for calls with short byte array like
     * the EXIF bytes in JPG, which are limited to 64 KB.
     */
    fun read(exifBytes: ByteArray): TiffContents =
        read(ByteArrayByteReader(exifBytes))

    fun read(byteReader: RandomAccessByteReader): TiffContents {

        val tiffHeader = readTiffHeader(byteReader)

        byteReader.reset()

        val directories = mutableListOf<TiffDirectory>()

        readDirectory(
            byteReader = byteReader,
            byteOrder = tiffHeader.byteOrder,
            directoryOffset = tiffHeader.offsetToFirstIFD,
            directoryType = TiffConstants.DIRECTORY_TYPE_ROOT,
            visitedOffsets = mutableListOf<Int>(),
            addDirectory = {
                directories.add(it)
            }
        )

        /**
         * Inspect if MakerNotes are present and could be added as
         * TiffDirectory. This is true for almost all manufacturers.
         */

        val makerNoteField = TiffDirectory.findTiffField(
            directories,
            ExifTag.EXIF_TAG_MAKER_NOTE
        )

        if (makerNoteField != null && makerNoteField.valueOffset != null) {

            val make = TiffDirectory.findTiffField(
                directories, TiffTag.TIFF_TAG_MAKE
            )?.valueDescription

            try {

                createMakerNoteDirectory(
                    byteReader,
                    makerNoteField.valueOffset,
                    make,
                    byteOrder = tiffHeader.byteOrder,
                    addDirectory = {
                        directories.add(it)
                    }
                )

            } catch (ignore: Exception) {

                /* Interpreting the Maker Note is optional. */
                @Suppress("PrintStackTrace")
                ignore.printStackTrace()
            }
        }

        if (directories.isEmpty())
            throw ImageReadException("Image did not contain any directories.")

        return TiffContents(tiffHeader, directories)
    }

    private fun createMakerNoteDirectory(
        byteReader: RandomAccessByteReader,
        makerNoteValueOffset: Int,
        make: String?,
        byteOrder: ByteOrder,
        addDirectory: (TiffDirectory) -> Unit
    ) {

        if (make != null && make == "Canon") {

            readDirectory(
                byteReader = byteReader,
                byteOrder = byteOrder,
                directoryOffset = makerNoteValueOffset,
                directoryType = TiffConstants.TIFF_MAKER_NOTE_CANON,
                visitedOffsets = mutableListOf<Int>(),
                addDirectory = addDirectory
            )
        }

//                if (make.startsWith("nikon")) {
//
//                    /* Should start with "Nikon" */
//                    byteReader.readAndVerifyBytes(
//                        "Nikon signaure",
//                        "Nikon".encodeToByteArray()
//                    )
//
//                    byteReader.skipBytes("Terminator", 1)
//
//                    readDirectory(
//                        byteReader = byteReader,
//                        byteOrder = byteOrder,
//                        directoryOffset = makerNoteField.valueOffset,
//                        directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON,
//                        visitedOffsets = mutableListOf<Int>(),
//                        addDirectory = {
//                            directories.add(it)
//                        }
//                    )
//                }
    }

    fun readTiffHeader(byteReader: ByteReader): TiffHeader {

        val byteOrder1 = byteReader.readByte("Byte order: First byte")
        val byteOrder2 = byteReader.readByte("Byte Order: Second byte")

        if (byteOrder1 != byteOrder2)
            throw ImageReadException("Byte Order bytes don't match ($byteOrder1, $byteOrder2).")

        val byteOrder = getTiffByteOrder(byteOrder1)

        val tiffVersion = byteReader.read2BytesAsInt("TIFF version", byteOrder)

        val offsetToFirstIFD =
            byteReader.read4BytesAsInt("Offset to first IFD", byteOrder)

        return TiffHeader(byteOrder, tiffVersion, offsetToFirstIFD)
    }

    private fun getTiffByteOrder(byteOrderByte: Byte): ByteOrder =
        when (byteOrderByte.toInt()) {
            'I'.code -> ByteOrder.LITTLE_ENDIAN
            'M'.code -> ByteOrder.BIG_ENDIAN
            else -> throw ImageReadException("Invalid TIFF byte order ${byteOrderByte.toUInt()}")
        }

    private fun readDirectory(
        byteReader: RandomAccessByteReader,
        byteOrder: ByteOrder,
        directoryOffset: Int,
        directoryType: Int,
        visitedOffsets: MutableList<Int>,
        addDirectory: (TiffDirectory) -> Unit
    ): Boolean {

        /* We don't want to visit a directory twice. */
        if (visitedOffsets.contains(directoryOffset))
            return false

        visitedOffsets.add(directoryOffset)

        byteReader.reset()

        /*
         * Sometimes TIFF offsets are greater than the file itself.
         * We ignore such corruptions.
         */
        if (directoryOffset >= byteReader.contentLength)
            return true

        val fields = try {

            byteReader.skipBytes("Directory offset", directoryOffset)

            val entryCount = byteReader.read2BytesAsInt("entrycount", byteOrder)

            readTiffFields(
                byteReader = byteReader,
                fieldsOffset = directoryOffset + 2,
                entryCount = entryCount,
                byteOrder = byteOrder,
                directoryType = directoryType
            )

        } catch (ex: Exception) {

            /*
             * Check if it's just the thumbnail directory and if so, ignore this error.
             * Thumbnails are not essential and can be re-created anytime.
             */

            val isThumbnailDirectory = directoryType == TiffConstants.TIFF_IFD1

            if (isThumbnailDirectory)
                return true

            throw ex
        }

        val nextDirectoryOffset =
            byteReader.read4BytesAsInt("Next directory offset", byteOrder)

        val directory = TiffDirectory(
            type = directoryType,
            entries = fields,
            offset = directoryOffset,
            nextDirectoryOffset = nextDirectoryOffset,
            byteOrder = byteOrder
        )

        if (directory.hasJpegImageData())
            directory.jpegImageDataElement = getJpegRawImageData(byteReader, directory)

        addDirectory(directory)

        /* Read offset directories */
        for (offsetField in offsetFields) {

            val field = directory.findField(offsetField)

            if (field != null) {

                val subDirOffsets: IntArray = when (offsetField) {
                    is TagInfoLong -> intArrayOf(directory.getFieldValue(offsetField)!!)
                    is TagInfoLongs -> directory.getFieldValue(offsetField)
                    else -> error("Unknown type: $offsetField")
                }

                for ((index, subDirOffset) in subDirOffsets.withIndex()) {

                    var subDirectoryRead = false

                    try {

                        val subIfdOffsets = field.tag == ExifTag.EXIF_TAG_SUB_IFDS_OFFSET.tag

                        val subDirectoryType = if (subIfdOffsets)
                            when (index) {
                                1 -> EXIF_SUB_IFD1
                                2 -> EXIF_SUB_IFD2
                                3 -> EXIF_SUB_IFD3
                                else -> DIRECTORY_TYPE_SUB
                            }
                        else
                            directoryTypeMap.get(offsetField)!!

                        subDirectoryRead = readDirectory(
                            byteReader = byteReader,
                            byteOrder = byteOrder,
                            directoryOffset = subDirOffset,
                            directoryType = subDirectoryType,
                            visitedOffsets = visitedOffsets,
                            addDirectory = addDirectory
                        )

                    } catch (ignore: ImageReadException) {
                        /*
                         * If the subdirectory is broken we remove the field.
                         */
                    }

                    if (!subDirectoryRead)
                        fields.remove(field)
                }
            }
        }

        if (nextDirectoryOffset > 0)
            readDirectory(
                byteReader = byteReader,
                byteOrder = byteOrder,
                directoryOffset = directory.nextDirectoryOffset,
                directoryType = directoryType + 1,
                visitedOffsets = visitedOffsets,
                addDirectory = addDirectory
            )

        return true
    }

    private fun readTiffFields(
        byteReader: RandomAccessByteReader,
        fieldsOffset: Int,
        entryCount: Int,
        byteOrder: ByteOrder,
        directoryType: Int
    ): MutableList<TiffField> {

        val fields = mutableListOf<TiffField>()

        for (entryIndex in 0 until entryCount) {

            val offset = fieldsOffset + entryIndex * TiffConstants.TIFF_ENTRY_LENGTH

            val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", byteOrder)
            val type = byteReader.read2BytesAsInt("Entry $entryIndex: 'type'", byteOrder)
            val count = byteReader.read4BytesAsInt("Entry $entryIndex: 'count'", byteOrder)

            /*
             * These bytes represent either the value for fields like orientation or
             * an offset to the value for fields like OriginalDateTime that
             * cannot be accommodated within 4 bytes.
             */
            val valueOrOffsetBytes: ByteArray =
                byteReader.readBytes("Entry $entryIndex: 'offset'", 4)

            val valueOrOffset: Int = valueOrOffsetBytes.toInt(byteOrder)

            /*
             * Skip invalid fields.
             *
             * These are seen very rarely, but can have invalid value lengths,
             * which can cause OOM problems.
             *
             * Except for the GPS directory where GPSVersionID is indeed zero,
             * but a valid field. So we shouldn't skip it.
             */
            if (tag == 0 && directoryType != TiffConstants.TIFF_GPS)
                continue

            val fieldType = try {
                getFieldType(type)
            } catch (ignore: ImageReadException) {
                /*
                 * Skip over unknown field types, since we can't calculate
                 * their size without knowing their type
                 */
                continue
            }

            val valueLength = count * fieldType.size

            val isLocalValue: Boolean =
                count * fieldType.size <= TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH

            val valueBytes: ByteArray = if (!isLocalValue) {

                /* Ignore corrupt offsets */
                if (valueOrOffset < 0 || valueOrOffset + valueLength > byteReader.contentLength)
                    continue

                byteReader.readBytes(valueOrOffset.toInt(), valueLength.toInt())

            } else {

                valueOrOffsetBytes.head(valueLength)
            }

            fields.add(
                TiffField(
                    offset = offset,
                    tag = tag,
                    directoryType = directoryType,
                    fieldType = fieldType,
                    count = count,
                    localValue = if (isLocalValue) valueOrOffset else null,
                    valueOffset = if (!isLocalValue) valueOrOffset else null,
                    valueBytes = valueBytes,
                    byteOrder = byteOrder,
                    sortHint = entryIndex
                )
            )
        }

        return fields
    }

    private fun getJpegRawImageData(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): JpegImageDataElement {

        val element = directory.getJpegRawImageDataElement()

        val offset = element.offset
        var length = element.length

        /*
         * If the length is not correct (going beyond the file size), we adjust it.
         */
        if (offset + length > byteReader.contentLength)
            length = (byteReader.contentLength - offset).toInt()

        val data = byteReader.readBytes(offset.toInt(), length)

        if (data.size != length)
            throw ImageReadException("Unexpected length: Wanted $length, but got ${data.size}")

        /*
         * Note: Apache Commons Imaging has a validation check here to ensure that
         * the embedded thumbnail ends with DD F9, as it should.
         * However, during tests, it was discovered that OOC JPEGs from a Canon 60D
         * have an incorrect length specified for the thumbnail bytes, and after DD 99,
         * there are some random bytes present.
         */

        return JpegImageDataElement(offset, length, data)
    }
}
