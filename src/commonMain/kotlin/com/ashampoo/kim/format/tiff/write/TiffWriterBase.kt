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
import com.ashampoo.kim.format.tiff.constant.ExifTag
import com.ashampoo.kim.format.tiff.constant.TiffConstants
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.format.tiff.constant.TiffConstants.TIFF_VERSION
import com.ashampoo.kim.output.BinaryByteWriter
import com.ashampoo.kim.output.ByteWriter

abstract class TiffWriterBase(
    val byteOrder: ByteOrder
) {

    abstract fun write(byteWriter: ByteWriter, outputSet: TiffOutputSet)

    internal fun createOffsetItems(outputSet: TiffOutputSet): TiffOffsetItems {

        val directories = outputSet.getDirectories()

        if (directories.isEmpty())
            throw ImageWriteException("No directories.")

        /* Directories */
        var exifDirectory: TiffOutputDirectory? = null
        var gpsDirectory: TiffOutputDirectory? = null
        var interoperabilityDirectory: TiffOutputDirectory? = null

        /* Offsets */
        var exifDirectoryOffsetField: TiffOutputField? = null
        var gpsDirectoryOffsetField: TiffOutputField? = null
        var interoperabilityDirectoryOffsetField: TiffOutputField? = null

        val directoryIndices = mutableListOf<Int>()
        val directoryTypeMap = mutableMapOf<Int, TiffOutputDirectory>()

        for (directory in directories) {

            val dirType = directory.type

            directoryTypeMap[dirType] = directory

            if (dirType < 0) {

                when (dirType) {

                    TiffConstants.TIFF_DIRECTORY_EXIF -> {

                        if (exifDirectory != null)
                            throw ImageWriteException("More than one EXIF directory.")

                        exifDirectory = directory
                    }

                    TiffConstants.TIFF_DIRECTORY_GPS -> {

                        if (gpsDirectory != null)
                            throw ImageWriteException("More than one GPS directory.")

                        gpsDirectory = directory
                    }

                    TiffConstants.TIFF_DIRECTORY_INTEROP -> {

                        if (interoperabilityDirectory != null)
                            throw ImageWriteException("More than one Interoperability directory.")

                        interoperabilityDirectory = directory
                    }

                    else -> throw ImageWriteException("Unknown directory: " + dirType)
                }

            } else {

                if (directoryIndices.contains(dirType))
                    throw ImageWriteException("More than one directory with index: " + dirType + ".")

                directoryIndices.add(dirType)
            }

            val fieldTags = mutableSetOf<Int>()

            for (field in directory.getFields()) {

                if (fieldTags.contains(field.tag))
                    throw ImageWriteException("Tag ${field.tagFormatted} appears twice in directory.")

                fieldTags.add(field.tag)

                when (field.tag) {

                    ExifTag.EXIF_TAG_EXIF_OFFSET.tag -> {

                        if (exifDirectoryOffsetField != null)
                            throw ImageWriteException("More than one Exif directory offset field.")

                        exifDirectoryOffsetField = field
                    }

                    ExifTag.EXIF_TAG_INTEROP_OFFSET.tag -> {

                        if (interoperabilityDirectoryOffsetField != null)
                            throw ImageWriteException("More than one Interoperability dir offset field.")

                        interoperabilityDirectoryOffsetField = field
                    }

                    ExifTag.EXIF_TAG_GPSINFO.tag -> {

                        if (gpsDirectoryOffsetField != null)
                            throw ImageWriteException("More than one GPS directory offset field.")

                        gpsDirectoryOffsetField = field
                    }
                }
            }
        }

        if (directoryIndices.isEmpty())
            throw ImageWriteException("Missing root directory.")

        /*
         * "Normal" TIFF directories should have continous indices starting with 0
         * like 0, 1, 2... and so on.
         */
        directoryIndices.sort()

        var previousDirectory: TiffOutputDirectory? = null

        for (index in directoryIndices) {

            /* set up chain of directory references for "normal" directories. */
            val directory = directoryTypeMap[index]

            previousDirectory?.setNextDirectory(directory)

            previousDirectory = directory
        }

        val rootDirectory = directoryTypeMap[TiffConstants.TIFF_DIRECTORY_TYPE_IFD0]

        if (rootDirectory == null)
            throw ImageWriteException("Root directory is missing.")

        if (interoperabilityDirectory == null && interoperabilityDirectoryOffsetField != null)
            throw ImageWriteException(
                "Output set has interoperability dir offset field, but no interoperability dir"
            )

        val tiffOffsetItems = TiffOffsetItems(byteOrder)

        if (interoperabilityDirectory != null) {

            if (exifDirectory == null)
                exifDirectory = outputSet.addExifDirectory()

            /* Create offset if missing */
            if (interoperabilityDirectoryOffsetField == null) {

                interoperabilityDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_INTEROP_OFFSET, byteOrder)

                exifDirectory.add(interoperabilityDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(
                TiffOffsetItem(
                    interoperabilityDirectory,
                    interoperabilityDirectoryOffsetField
                )
            )
        }

        /* Make sure offset fields and offset directories correspond. */
        if (exifDirectory == null && exifDirectoryOffsetField != null)
            throw ImageWriteException("Output set has EXIF directory offset field, but no EXIF directory")

        if (exifDirectory != null) {

            /* Create offset if missing */
            if (exifDirectoryOffsetField == null) {

                exifDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_EXIF_OFFSET, byteOrder)

                rootDirectory.add(exifDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(exifDirectory, exifDirectoryOffsetField))
        }

        if (gpsDirectory == null && gpsDirectoryOffsetField != null)
            throw ImageWriteException("Output set has GPS directory offset field, but no GPS directory")

        if (gpsDirectory != null) {

            /* Create offset if missing */
            if (gpsDirectoryOffsetField == null) {

                gpsDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_GPSINFO, byteOrder)

                rootDirectory.add(gpsDirectoryOffsetField)
            }

            tiffOffsetItems.addOffsetItem(TiffOffsetItem(gpsDirectory, gpsDirectoryOffsetField))
        }

        return tiffOffsetItems
    }

    protected fun writeImageFileHeader(
        byteWriter: BinaryByteWriter,
        offsetToFirstIFD: Int = TIFF_HEADER_SIZE
    ) {

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byteWriter.write('I'.code)
            byteWriter.write('I'.code)
        } else {
            byteWriter.write('M'.code)
            byteWriter.write('M'.code)
        }

        byteWriter.write2Bytes(TIFF_VERSION)
        byteWriter.write4Bytes(offsetToFirstIFD.toInt())
    }

    companion object {

        /** Returns an appropriate TiffImageWriter instance. */
        fun createTiffWriter(
            byteOrder: ByteOrder,
            oldExifBytes: ByteArray?
        ): TiffWriterBase {

            return if (oldExifBytes != null)
                TiffWriterLossless(
                    byteOrder = byteOrder,
                    exifBytes = oldExifBytes
                )
            else
                TiffWriterLossy(
                    byteOrder = byteOrder
                )
        }
    }
}
