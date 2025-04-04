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
package com.ashampoo.kim.format.tiff

import com.ashampoo.kim.common.ByteOrder
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.head
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.toInt
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.GeoTiffTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD1
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD2
import com.ashampoo.kim.format.tiff.constant.TiffConstants.EXIF_SUB_IFD3
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
import com.ashampoo.kim.format.tiff.constant.TiffTag
import com.ashampoo.kim.format.tiff.fieldtype.FieldType.Companion.getFieldType
import com.ashampoo.kim.format.tiff.geotiff.GeoTiffDirectory
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLong
import com.ashampoo.kim.format.tiff.taginfo.TagInfoLongs
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.RandomAccessByteReader
import com.ashampoo.kim.input.read2BytesAsInt
import com.ashampoo.kim.input.read4BytesAsInt
import com.ashampoo.kim.input.readByte
import com.ashampoo.kim.input.readByteAsInt
import com.ashampoo.kim.input.readBytes
import com.ashampoo.kim.input.skipBytes
import com.ashampoo.kim.output.ByteArrayByteWriter
import kotlin.jvm.JvmStatic

@Suppress("TooManyFunctions")
public object TiffReader {

    internal const val NIKON_MAKER_NOTE_SIGNATURE = "Nikon\u0000"

    private val offsetFields = listOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET,
        ExifTag.EXIF_TAG_GPSINFO,
        ExifTag.EXIF_TAG_INTEROP_OFFSET,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET
    )

    private val directoryTypeMap = mapOf(
        ExifTag.EXIF_TAG_EXIF_OFFSET to TiffConstants.TIFF_DIRECTORY_EXIF,
        ExifTag.EXIF_TAG_GPSINFO to TiffConstants.TIFF_DIRECTORY_GPS,
        ExifTag.EXIF_TAG_INTEROP_OFFSET to TiffConstants.TIFF_DIRECTORY_INTEROP,
        ExifTag.EXIF_TAG_SUB_IFDS_OFFSET to TiffConstants.TIFF_DIRECTORY_TYPE_IFD1
    )

    /**
     * Convenience method for calls with short byte array like
     * the EXIF bytes in JPG, which are limited to 64 KB.
     */
    @JvmStatic
    public fun read(
        exifBytes: ByteArray,
        readTiffImageBytes: Boolean = false,
        directoryType: Int = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffContents =
        read(ByteArrayByteReader(exifBytes), readTiffImageBytes, directoryType)

    /**
     * Reads the TIFF file.
     *
     * @param byteReader The bytes source
     * @param readTiffImageBytes Flag to include strip bytes.
     *                           This should only set if a rewrite of the file is intended.
     *                           For normal reading of RAW metadata this consumes a lot of memory.
     */
    @JvmStatic
    public fun read(
        byteReader: RandomAccessByteReader,
        readTiffImageBytes: Boolean = false,
        directoryType: Int = TiffConstants.TIFF_DIRECTORY_TYPE_IFD0
    ): TiffContents {

        val tiffHeader = readTiffHeader(byteReader)

        byteReader.reset()

        val directories = mutableListOf<TiffDirectory>()

        readDirectory(
            byteReader = byteReader,
            byteOrder = tiffHeader.byteOrder,
            directoryOffset = tiffHeader.offsetToFirstIFD,
            directoryType = directoryType,
            visitedOffsets = mutableListOf(),
            readTiffImageBytes = readTiffImageBytes,
            addDirectory = {
                directories.add(it)
            }
        )

        if (directories.isEmpty())
            throw ImageReadException("Image did not contain any directories.")

        val makerNoteDirectory =
            tryToParseMakerNote(directories, byteReader, tiffHeader.byteOrder)

        val geoTiffDirectory = tryToParseGeoTiff(directories)

        return TiffContents(tiffHeader, directories, makerNoteDirectory, geoTiffDirectory)
    }

    internal fun readTiffHeader(byteReader: ByteReader): TiffHeader {

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
        readTiffImageBytes: Boolean,
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

        byteReader.skipBytes("Directory offset", directoryOffset)

        val fields = try {

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

            val isThumbnailDirectory = directoryType == TiffConstants.TIFF_DIRECTORY_TYPE_IFD1

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
            directory.thumbnailBytes = readThumbnailBytes(byteReader, directory)

        if (readTiffImageBytes && directory.hasStripImageData())
            directory.tiffImageBytes = readTiffImageBytes(byteReader, directory)

        addDirectory(directory)

        /* Read offset directories */
        for (offsetField in offsetFields) {

            val field = directory.findField(offsetField)

            if (field != null) {

                val subDirOffsets: IntArray = try {

                    when (offsetField) {
                        is TagInfoLong -> intArrayOf(directory.getFieldValue(offsetField)!!)
                        is TagInfoLongs -> directory.getFieldValue(offsetField)
                        else -> error("Unknown offset type: $offsetField")
                    }

                } catch (ignore: ImageReadException) {

                    /*
                     * If the offset field is broken we don't try
                     * to read the sub directory.
                     *
                     * We need to remove the field pointing to wrong
                     * data or else we won't be able to update the file.
                     */

                    fields.remove(field)

                    continue
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
                                else -> TIFF_DIRECTORY_TYPE_IFD1
                            }
                        else
                            directoryTypeMap.get(offsetField)!!

                        subDirectoryRead = readDirectory(
                            byteReader = byteReader,
                            byteOrder = byteOrder,
                            directoryOffset = subDirOffset,
                            directoryType = subDirectoryType,
                            visitedOffsets = visitedOffsets,
                            readTiffImageBytes = readTiffImageBytes,
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
                readTiffImageBytes = readTiffImageBytes,
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

        /*
         * We use an ArrayList to provide a capacity.
         * This shows a small performance improvement in the Profiler.
         */
        val fields = ArrayList<TiffField>(entryCount)

        @Suppress("LoopWithTooManyJumpStatements")
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
            if (tag == 0 && directoryType != TiffConstants.TIFF_DIRECTORY_GPS)
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

            val valueLength: Int = count * fieldType.size

            val isLocalValue: Boolean =
                count * fieldType.size <= TiffConstants.TIFF_ENTRY_MAX_VALUE_LENGTH

            val valueBytes: ByteArray = if (!isLocalValue) {

                val endPos = valueOrOffset + valueLength

                /*
                 * Ignore corrupt offsets.
                 *
                 * Note that the endPos may become negative if one value is too large for an int.
                 * That's why we need to check both offset and endPos for negativity.
                 */
                if (valueOrOffset < 0 || endPos < 0 || endPos > byteReader.contentLength)
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

    /**
     * Reads the thumbnail image if data is valid or returns NULL if a problem was found.
     *
     * Discarding corrupt thumbnails is not a big issue, so no exceptions will be thrown here.
     */
    private fun readThumbnailBytes(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): ByteArray? {

        val element = directory.getJpegImageDataElement() ?: return null

        val offset = element.offset
        var length = element.length

        /*
         * If the length is not correct (going beyond the file size) we need to adjust it.
         */
        if (offset + length > byteReader.contentLength)
            length = (byteReader.contentLength - offset).toInt()

        /*
         * If the new length is 0 or negative, ignore this element.
         */
        if (length <= 0)
            return null

        val bytes = byteReader.readBytes(offset.toInt(), length)

        if (bytes.size != length)
            return null

        /*
         * Ignore it if it's not a JPEG.
         * Some files have random garbage bytes here.
         */
        if (!bytes.startsWith(ImageFormatMagicNumbers.jpeg))
            return null

        /*
         * Note: Apache Commons Imaging has a validation check here to ensure that
         * the embedded thumbnail ends with DD F9, as it should.
         * However, during tests, it was discovered that OOC JPEGs from a Canon 60D
         * have an incorrect length specified for the thumbnail bytes, and after DD 99,
         * there are some random bytes present.
         */

        return bytes
    }

    private fun readTiffImageBytes(
        byteReader: RandomAccessByteReader,
        directory: TiffDirectory
    ): ByteArray? {

        val elements = directory.getStripImageDataElements() ?: return null

        val byteArrayByteWriter = ByteArrayByteWriter()

        for (element in elements) {

            val offset = element.offset
            var length = element.length

            /*
             * If the length is not correct (going beyond the file size) we need to adjust it.
             */
            if (offset + length > byteReader.contentLength)
                length = (byteReader.contentLength - offset).toInt()

            /*
             * If the new length is 0 or negative, ignore this element.
             */
            if (length <= 0)
                continue

            val bytes = byteReader.readBytes(offset.toInt(), length)

            /*
             * Break if something is wrong.
             */
            if (bytes.size != length)
                return null

            byteArrayByteWriter.write(bytes)
        }

        return byteArrayByteWriter.toByteArray()
    }

    /**
     * Inspect if MakerNotes are present and could be added as
     * TiffDirectory. This is true for almost all manufacturers.
     */
    private fun tryToParseMakerNote(
        directories: MutableList<TiffDirectory>,
        byteReader: RandomAccessByteReader,
        byteOrder: ByteOrder
    ): TiffDirectory? {

        val makerNoteField = TiffDirectory.findTiffField(
            directories,
            ExifTag.EXIF_TAG_MAKER_NOTE
        )

        if (makerNoteField != null && makerNoteField.valueOffset != null) {

            val make = TiffDirectory.findTiffField(
                directories, TiffTag.TIFF_TAG_MAKE
            )?.valueDescription

            try {

                var makerNoteDirectory: TiffDirectory? = null

                createMakerNoteDirectory(
                    byteReader = byteReader,
                    makerNoteValueOffset = makerNoteField.valueOffset,
                    make = make,
                    byteOrder = byteOrder,
                    addDirectory = {
                        makerNoteDirectory = it
                    }
                )

                return makerNoteDirectory

            } catch (ignore: Exception) {
                /*
                 * Be silent here.
                 * MakerNote support is experimental.
                 */
            }
        }

        return null
    }

    /**
     * Try to read MakerNote and add it as a directory.
     *
     * Note that this is experimental!
     *
     * See https://exiftool.org/makernote_types.html
     */
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
                readTiffImageBytes = false,
                addDirectory = addDirectory
            )
        }

        if (make != null && make.trim().lowercase().startsWith("nikon")) {

            byteReader.reset()
            byteReader.skipBytes("offset", makerNoteValueOffset)

            val nikonSignature = byteReader.readBytes(
                fieldName = "Nikon MakerNote signature",
                count = NIKON_MAKER_NOTE_SIGNATURE.length
            ).decodeToString()

            val nikonSignatureMatched = nikonSignature == NIKON_MAKER_NOTE_SIGNATURE

            if (!nikonSignatureMatched)
                return

            val type = byteReader.readByteAsInt()

            /* We only have test files for type 2 right now. */
            if (type != 2)
                return

            readDirectory(
                byteReader = byteReader,
                byteOrder = ByteOrder.LITTLE_ENDIAN,
                directoryOffset = makerNoteValueOffset + 18,
                directoryType = TiffConstants.TIFF_MAKER_NOTE_NIKON,
                visitedOffsets = mutableListOf<Int>(),
                readTiffImageBytes = false,
                addDirectory = addDirectory
            )
        }
    }

    /**
     * Inspect if MakerNotes are present and could be added as
     * TiffDirectory. This is true for almost all manufacturers.
     */
    private fun tryToParseGeoTiff(
        directories: MutableList<TiffDirectory>
    ): GeoTiffDirectory? {

        try {

            val geoTiffDirectoryField = TiffDirectory.findTiffField(
                directories,
                GeoTiffTag.EXIF_TAG_GEO_KEY_DIRECTORY_TAG
            ) ?: return null

            val shorts = geoTiffDirectoryField.value as? ShortArray

            if (shorts != null)
                return GeoTiffDirectory.parseFrom(shorts)

            return null

        } catch (ignore: Exception) {

            /*
             * Be silent here as GeoTiff interpretation is not essential.
             */

            return null
        }
    }
}
