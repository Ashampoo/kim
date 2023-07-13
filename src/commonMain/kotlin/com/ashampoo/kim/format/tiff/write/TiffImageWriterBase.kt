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
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.TiffConstants
import com.ashampoo.kim.format.tiff.constants.TiffConstants.DEFAULT_TIFF_BYTE_ORDER
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_HEADER_SIZE
import com.ashampoo.kim.format.tiff.constants.TiffConstants.TIFF_VERSION
import com.ashampoo.kim.output.BinaryByteWriter
import com.ashampoo.kim.output.ByteWriter

abstract class TiffImageWriterBase(
    val byteOrder: ByteOrder = DEFAULT_TIFF_BYTE_ORDER
) {

    abstract fun write(byteWriter: ByteWriter, outputSet: TiffOutputSet)

    protected fun validateDirectories(outputSet: TiffOutputSet): TiffOutputSummary {

        val directories = outputSet.getDirectories()

        if (directories.isEmpty())
            throw ImageWriteException("No directories.")

        var exifDirectory: TiffOutputDirectory? = null
        var gpsDirectory: TiffOutputDirectory? = null
        var interoperabilityDirectory: TiffOutputDirectory? = null
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

                    TiffConstants.DIRECTORY_TYPE_EXIF -> {

                        if (exifDirectory != null)
                            throw ImageWriteException("More than one EXIF directory.")

                        exifDirectory = directory
                    }

                    TiffConstants.DIRECTORY_TYPE_GPS -> {

                        if (gpsDirectory != null)
                            throw ImageWriteException("More than one GPS directory.")

                        gpsDirectory = directory
                    }

                    TiffConstants.DIRECTORY_TYPE_INTEROPERABILITY -> {

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

            val fields = directory.getFields()

            for (field in fields) {

                if (fieldTags.contains(field.tag))
                    throw ImageWriteException("Tag ${field.tagInfo} appears twice in directory.")

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

        val rootDirectory = directoryTypeMap[TiffConstants.DIRECTORY_TYPE_ROOT]

        if (rootDirectory == null)
            throw ImageWriteException("Root directory is missing.")

        /* Prepare results */
        val result = TiffOutputSummary(byteOrder, rootDirectory, directoryTypeMap)

        if (interoperabilityDirectory == null && interoperabilityDirectoryOffsetField != null)
            throw ImageWriteException(
                "Output set has interoperability dir offset field, but no interoperability dir"
            )

        if (interoperabilityDirectory != null) {

            if (exifDirectory == null)
                exifDirectory = outputSet.addExifDirectory()

            /*
             * Create offset
             */
            if (interoperabilityDirectoryOffsetField == null) {

                interoperabilityDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_INTEROP_OFFSET, byteOrder)

                exifDirectory.add(interoperabilityDirectoryOffsetField)
            }

            result.add(interoperabilityDirectory, interoperabilityDirectoryOffsetField)
        }

        /* Make sure offset fields and offset'd directories correspond. */
        if (exifDirectory == null && exifDirectoryOffsetField != null)
            throw ImageWriteException("Output set has Exif Directory Offset field, but no Exif Directory")

        if (exifDirectory != null) {

            if (exifDirectoryOffsetField == null) {

                exifDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_EXIF_OFFSET, byteOrder)

                rootDirectory.add(exifDirectoryOffsetField)
            }

            result.add(exifDirectory, exifDirectoryOffsetField)
        }

        if (gpsDirectory == null && gpsDirectoryOffsetField != null)
            throw ImageWriteException("Output set has GPS Directory Offset field, but no GPS Directory")

        if (gpsDirectory != null) {

            if (gpsDirectoryOffsetField == null) {

                gpsDirectoryOffsetField =
                    TiffOutputField.createOffsetField(ExifTag.EXIF_TAG_GPSINFO, byteOrder)

                rootDirectory.add(gpsDirectoryOffsetField)
            }

            result.add(gpsDirectory, gpsDirectoryOffsetField)
        }

        return result
    }

    protected fun writeImageFileHeader(
        bos: BinaryByteWriter,
        offsetToFirstIFD: Long = TIFF_HEADER_SIZE.toLong()
    ) {

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            bos.write('I'.code)
            bos.write('I'.code)
        } else {
            bos.write('M'.code)
            bos.write('M'.code)
        }

        bos.write2Bytes(TIFF_VERSION)
        bos.write4Bytes(offsetToFirstIFD.toInt())
    }
}
