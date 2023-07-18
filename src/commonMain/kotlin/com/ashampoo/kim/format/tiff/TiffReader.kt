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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.common.BinaryFileParser
import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.toInt
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.TiffConstants
import com.ashampoo.kim.format.tiff.constants.TiffConstants.DIRECTORY_TYPE_SUB
import com.ashampoo.kim.format.tiff.constants.TiffConstants.EXIF_SUB_IFD1
import com.ashampoo.kim.format.tiff.constants.TiffConstants.EXIF_SUB_IFD2
import com.ashampoo.kim.format.tiff.constants.TiffConstants.EXIF_SUB_IFD3
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType
import com.ashampoo.kim.format.tiff.fieldtypes.FieldType.Companion.getFieldType
import com.ashampoo.kim.format.tiff.taginfos.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfos.TagInfoLongs
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.RandomAccessByteReader

class TiffReader : BinaryFileParser() {

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

    fun getTiffByteOrder(byteOrderByte: Int): ByteOrder =
        when (byteOrderByte) {
            'I'.code -> ByteOrder.LITTLE_ENDIAN
            'M'.code -> ByteOrder.BIG_ENDIAN
            else -> throw ImageReadException("Invalid TIFF byte order ${byteOrderByte.toUInt()}")
        }

    fun read(byteReader: RandomAccessByteReader): TiffContents {

        val collector = TiffReaderCollector()

        val tiffHeader = readTiffHeader(byteReader)

        collector.tiffHeader = tiffHeader

        byteReader.reset()

        readDirectory(
            byteReader = byteReader,
            directoryOffset = tiffHeader.offsetToFirstIFD,
            dirType = TiffConstants.DIRECTORY_TYPE_ROOT,
            collector = collector,
            visitedOffsets = mutableListOf<Number>()
        )

        val contents = collector.getContents()

        if (contents.directories.isEmpty())
            throw ImageReadException("Image did not contain any directories.")

        return contents
    }

    private fun readTiffHeader(byteReader: ByteReader): TiffHeader {

        val byteOrder1 = byteReader.readByte("Byte order: First byte").toInt()
        val byteOrder2 = byteReader.readByte("Byte Order: Second byte").toInt()

        if (byteOrder1 != byteOrder2)
            throw ImageReadException("Byte Order bytes don't match ($byteOrder1, $byteOrder2).")

        byteOrder = getTiffByteOrder(byteOrder1)

        val tiffVersion = byteReader.read2BytesAsInt("TIFF version", byteOrder)

        val offsetToFirstIFD =
            0xFFFFFFFFL and byteReader.read4BytesAsInt("Offset to first IFD", byteOrder).toLong()

        byteReader.skipBytes("skip bytes to first IFD", offsetToFirstIFD - 8)

        return TiffHeader(byteOrder, tiffVersion, offsetToFirstIFD)
    }

    private fun readDirectory(
        byteReader: RandomAccessByteReader,
        directoryOffset: Long,
        dirType: Int,
        collector: TiffReaderCollector,
        visitedOffsets: MutableList<Number>
    ): Boolean {

        if (visitedOffsets.contains(directoryOffset))
            return false

        visitedOffsets.add(directoryOffset)

        byteReader.reset()

        if (directoryOffset >= byteReader.getLength())
            return true

        byteReader.skipBytes("Directory offset", directoryOffset)

        val fields = mutableListOf<TiffField>()

        val entryCount: Int

        entryCount = try {
            byteReader.read2BytesAsInt("entrycount", byteOrder)
        } catch (ignore: ImageReadException) {
            return true
        }

        repeat(entryCount) { entryIndex ->

            val tag = byteReader.read2BytesAsInt("Entry $entryIndex: 'tag'", byteOrder)
            val type = byteReader.read2BytesAsInt("Entry $entryIndex: 'type'", byteOrder)

            val count =
                0xFFFFFFFFL and
                    byteReader.read4BytesAsInt("Entry $entryIndex: 'count'", byteOrder).toLong()

            val offsetBytes = byteReader.readBytes("Entry $entryIndex: 'offset'", 4)

            val offset = 0xFFFFFFFFL and offsetBytes.toInt(byteOrder).toLong()

            /*
             * Skip invalid fields.
             * These are seen very rarely, but can have invalid value lengths,
             * which can cause OOM problems.
             */
            if (tag == 0)
                return@repeat

            val fieldType: FieldType = try {
                getFieldType(type)
            } catch (ignore: ImageReadException) {
                /*
                 * Skip over unknown field types, since we can't calculate
                 * their size without knowing their type
                 */
                return@repeat
            }

            val valueLength = count * fieldType.size

            val valueBytes: ByteArray = if (valueLength > TIFF_ENTRY_MAX_VALUE_LENGTH) {

                /* Ignore corrupt offsets */
                if (offset < 0 || offset + valueLength > byteReader.getLength())
                    return@repeat

                byteReader.readBytes(offset.toInt(), valueLength.toInt())

            } else
                offsetBytes

            val field = TiffField(tag, dirType, fieldType, count, offset, valueBytes, byteOrder, entryIndex)

            fields.add(field)
        }

        val nextDirectoryOffset = 0xFFFFFFFFL and
            byteReader.read4BytesAsInt("Next directory offset", byteOrder).toLong()

        val directory = TiffDirectory(dirType, fields, directoryOffset, nextDirectoryOffset, byteOrder)

        if (directory.hasJpegImageData())
            directory.jpegImageData = getJpegRawImageData(byteReader, directory)

        collector.directories.add(directory)

        /* Read offset directories */
        for (offsetField in offsetFields) {

            val field = directory.findField(offsetField)

            if (field != null) {

                val subDirOffsets: IntArray = when (offsetField) {
                    is TagInfoLong -> intArrayOf(directory.getFieldValue(offsetField))
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
                            directoryOffset = subDirOffset.toLong(),
                            dirType = subDirectoryType,
                            collector = collector,
                            visitedOffsets = visitedOffsets
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

        if (directory.nextDirectoryOffset > 0)
            readDirectory(
                byteReader = byteReader,
                directoryOffset = directory.nextDirectoryOffset,
                dirType = dirType + 1,
                collector = collector,
                visitedOffsets = visitedOffsets
            )

        return true
    }

    private fun getJpegRawImageData(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): JpegImageData {

        val element = directory.getJpegRawImageDataElement()

        val offset = element.offset
        var length = element.length

        /*
         * If the length is not correct (going beyond the file size), we adjust it.
         */
        if (offset + length > byteReader.getLength())
            length = (byteReader.getLength() - offset).toInt()

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

        return JpegImageData(offset, length, data)
    }

    private class TiffReaderCollector {

        var tiffHeader: TiffHeader? = null
        val directories = mutableListOf<TiffDirectory>()

        fun getContents(): TiffContents =
            TiffContents(requireNotNull(tiffHeader), directories)
    }
}
