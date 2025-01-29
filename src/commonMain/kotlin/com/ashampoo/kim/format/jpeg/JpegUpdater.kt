/*
 * Copyright 2025 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.jpeg

import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.common.startsWith
import com.ashampoo.kim.common.tryWithImageWriteException
import com.ashampoo.kim.format.ImageFormatMagicNumbers
import com.ashampoo.kim.format.MetadataUpdater
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.format.xmp.XmpWriter
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.input.readRemainingBytes
import com.ashampoo.kim.model.MetadataUpdate
import com.ashampoo.kim.model.TiffOrientation
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.xmp.XMPMeta
import com.ashampoo.xmp.XMPMetaFactory

internal object JpegUpdater : MetadataUpdater {

    @Throws(ImageWriteException::class)
    override fun update(
        byteReader: ByteReader,
        byteWriter: ByteWriter,
        update: MetadataUpdate
    ) = tryWithImageWriteException {

        /*
         * TODO Avoid the read all bytes and stream instead.
         *  This will require the implementation of single-shot updates to all fields.
         */
        val bytes = byteReader.readRemainingBytes()

        if (!bytes.startsWith(ImageFormatMagicNumbers.jpeg))
            throw ImageWriteException("Provided input bytes are not JPEG!")

        val kimMetadata = JpegImageParser.parseMetadata(
            ByteArrayByteReader(bytes)
        )

        val xmpUpdatedBytes = updateXmp(bytes, kimMetadata.xmp, update)

        val exifUpdatedBytes = updateExif(xmpUpdatedBytes, kimMetadata.exif, update)

        val iptcUpdatedBytes = updateIptc(exifUpdatedBytes, kimMetadata.iptc, update)

        byteWriter.write(iptcUpdatedBytes)
    }

    @Throws(ImageWriteException::class)
    override fun updateThumbnail(
        bytes: ByteArray,
        thumbnailBytes: ByteArray
    ): ByteArray = tryWithImageWriteException {

        if (!bytes.startsWith(ImageFormatMagicNumbers.jpeg))
            throw ImageWriteException("Provided input bytes are not JPEG!")

        val metadata = JpegImageParser.parseMetadata(ByteArrayByteReader(bytes))

        val outputSet = metadata.exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.setThumbnailBytes(thumbnailBytes)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(
            byteReader = ByteArrayByteReader(bytes),
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        return byteWriter.toByteArray()
    }

    private fun updateXmp(inputBytes: ByteArray, xmp: String?, update: MetadataUpdate): ByteArray {

        val xmpMeta: XMPMeta = if (xmp != null)
            XMPMetaFactory.parseFromString(xmp)
        else
            XMPMetaFactory.create()

        val updatedXmp = XmpWriter.updateXmp(xmpMeta, update, true)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateXmpXml(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            xmpXml = updatedXmp
        )

        return byteWriter.toByteArray()
    }

    private fun updateExif(
        inputBytes: ByteArray,
        exif: TiffContents?,
        update: MetadataUpdate
    ): ByteArray {

        /*
         * Filter out all updates we can perform on EXIF.
         */
        @Suppress("ComplexCondition")
        if (
            update !is MetadataUpdate.Orientation &&
            update !is MetadataUpdate.TakenDate &&
            update !is MetadataUpdate.Description &&
            update !is MetadataUpdate.GpsCoordinates &&
            update !is MetadataUpdate.GpsCoordinatesAndLocationShown
        )
            return inputBytes

        /*
         * Verify if it's possible to perform a lossless update by making byte modifications.
         * For orientation changes, it's feasible to achieve this with a single byte swap.
         */
        if (update is MetadataUpdate.Orientation) {

            val updated = tryLosslessOrientationUpdate(inputBytes, update.tiffOrientation)

            if (updated)
                return inputBytes
        }

        val outputSet = exif?.createOutputSet() ?: TiffOutputSet()

        outputSet.applyUpdate(update)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.updateExifMetadataLossless(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            outputSet = outputSet
        )

        return byteWriter.toByteArray()
    }

    private fun tryLosslessOrientationUpdate(
        inputBytes: ByteArray,
        tiffOrientation: TiffOrientation
    ): Boolean {

        val byteReader = ByteArrayByteReader(inputBytes)

        val orientationOffset = JpegOrientationOffsetFinder.findOrientationOffset(byteReader)

        if (orientationOffset != null) {

            inputBytes[orientationOffset.toInt()] = tiffOrientation.value.toByte()

            return true
        }

        return false
    }

    private fun updateIptc(
        inputBytes: ByteArray,
        iptc: IptcMetadata?,
        update: MetadataUpdate
    ): ByteArray {

        /*
         * Filter out all updates we can perform on IPTC.
         */
        @Suppress("ComplexCondition")
        if (
            update !is MetadataUpdate.Title &&
            update !is MetadataUpdate.Description &&
            update !is MetadataUpdate.LocationShown &&
            update !is MetadataUpdate.GpsCoordinatesAndLocationShown &&
            update !is MetadataUpdate.Keywords
        )
            return inputBytes

        /* Update IPTC keywords */

        val newBlocks = iptc?.nonIptcBlocks ?: emptyList()
        val oldRecords = iptc?.records ?: emptyList()

        val newRecords = mutableListOf<IptcRecord>()

        if (update is MetadataUpdate.Title) {

            newRecords.addAll(oldRecords.filter { it.iptcType != IptcTypes.OBJECT_NAME })

            update.title?.let { title ->
                newRecords.add(IptcRecord(IptcTypes.OBJECT_NAME, title))
            }
        }

        if (update is MetadataUpdate.Description) {

            newRecords.addAll(oldRecords.filter { it.iptcType != IptcTypes.CAPTION_ABSTRACT })

            update.description?.let { description ->
                newRecords.add(IptcRecord(IptcTypes.CAPTION_ABSTRACT, description))
            }
        }

        if (update is MetadataUpdate.LocationShown) {

            newRecords.addAll(
                oldRecords.filter {
                    it.iptcType != IptcTypes.SUBLOCATION &&
                        it.iptcType != IptcTypes.CITY &&
                        it.iptcType != IptcTypes.PROVINCE_STATE &&
                        it.iptcType != IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME
                }
            )

            if (update.locationShown != null) {

                update.locationShown.street?.let { location ->
                    newRecords.add(IptcRecord(IptcTypes.SUBLOCATION, location))
                }

                update.locationShown.city?.let { city ->
                    newRecords.add(IptcRecord(IptcTypes.CITY, city))
                }

                update.locationShown.state?.let { state ->
                    newRecords.add(IptcRecord(IptcTypes.PROVINCE_STATE, state))
                }

                update.locationShown.country?.let { country ->
                    newRecords.add(IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME, country))
                }
            }
        }

        if (update is MetadataUpdate.GpsCoordinatesAndLocationShown) {

            newRecords.addAll(
                oldRecords.filter {
                    it.iptcType != IptcTypes.SUBLOCATION &&
                    it.iptcType != IptcTypes.CITY &&
                        it.iptcType != IptcTypes.PROVINCE_STATE &&
                        it.iptcType != IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME
                }
            )

            if (update.locationShown != null) {

                update.locationShown.street?.let { location ->
                    newRecords.add(IptcRecord(IptcTypes.SUBLOCATION, location))
                }

                update.locationShown.city?.let { city ->
                    newRecords.add(IptcRecord(IptcTypes.CITY, city))
                }

                update.locationShown.state?.let { state ->
                    newRecords.add(IptcRecord(IptcTypes.PROVINCE_STATE, state))
                }

                update.locationShown.country?.let { country ->
                    newRecords.add(IptcRecord(IptcTypes.COUNTRY_PRIMARY_LOCATION_NAME, country))
                }
            }
        }

        if (update is MetadataUpdate.Keywords) {

            newRecords.addAll(oldRecords.filter { it.iptcType != IptcTypes.KEYWORDS })

            for (keyword in update.keywords.sorted())
                newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keyword))
        }

        val newIptc = IptcMetadata(newRecords, newBlocks)

        val byteWriter = ByteArrayByteWriter()

        JpegRewriter.writeIPTC(
            byteReader = ByteArrayByteReader(inputBytes),
            byteWriter = byteWriter,
            metadata = newIptc
        )

        return byteWriter.toByteArray()
    }
}
