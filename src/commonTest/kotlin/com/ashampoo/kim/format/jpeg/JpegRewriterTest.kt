/*
 * Copyright 2023 Ashampoo GmbH & Co. KG
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

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.writeBytes
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcMetadata
import com.ashampoo.kim.format.jpeg.iptc.IptcRecord
import com.ashampoo.kim.format.jpeg.iptc.IptcTypes
import com.ashampoo.kim.format.tiff.TiffContents
import com.ashampoo.kim.format.tiff.constants.ExifTag
import com.ashampoo.kim.format.tiff.constants.TiffTag
import com.ashampoo.kim.format.tiff.write.TiffOutputSet
import com.ashampoo.kim.input.ByteArrayByteReader
import com.ashampoo.kim.model.GpsCoordinates
import com.ashampoo.kim.output.ByteArrayByteWriter
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.sink
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class JpegRewriterTest {

    private val newDate = "2023:05:10 13:37:42"

    private val keywordWithUmlauts = "Umlauts: äöüß"

    private val crashBuildingGps = GpsCoordinates(
        53.219391,
        8.239661
    )

    private val newXmp = """
        <?xpacket begin="﻿" id="W5M0MpCehiHzreSzNTczkc9d"?>
            <x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 6.1.10">
              <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                <rdf:Description rdf:about=""
                    xmlns:xmp="http://ns.adobe.com/xap/1.0/"
                  xmp:Rating="3"/>
              </rdf:RDF>
            </x:xmpmeta>
        <?xpacket end="w"?>
    """.trimIndent()

    @BeforeTest
    fun setUp() {
        Kim.underUnitTesting = true
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testChangeMetadata() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            // FIXME Problematic files
            if (index in 21..22 || index == 50)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val metadata = Kim.readMetadata(bytes)

            val exif: TiffContents? = metadata?.exif

            val outputSet: TiffOutputSet = exif?.createOutputSet() ?: TiffOutputSet()

            val rootDirectory = outputSet.getOrCreateRootDirectory()
            val exifDirectory = outputSet.getOrCreateExifDirectory()

            /* Rotate by 180 degrees */

            rootDirectory.removeField(TiffTag.TIFF_TAG_ORIENTATION)
            rootDirectory.add(TiffTag.TIFF_TAG_ORIENTATION, 8)

            /* Set new date */

            rootDirectory.removeField(TiffTag.TIFF_TAG_DATE_TIME)
            rootDirectory.add(TiffTag.TIFF_TAG_DATE_TIME, newDate)

            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL, newDate)

            exifDirectory.removeField(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED)
            exifDirectory.add(ExifTag.EXIF_TAG_DATE_TIME_DIGITIZED, newDate)

            /* Set GPS */

            outputSet.setGpsCoordinates(crashBuildingGps)

            /* IPTC */

            val iptcMetadata = metadata?.iptc

            val newBlocks = iptcMetadata?.nonIptcBlocks ?: emptyList()
            val oldRecords = iptcMetadata?.records ?: emptyList()

            val newRecords = mutableListOf<IptcRecord>()
            newRecords.addAll(oldRecords)

            newRecords.add(IptcRecord(IptcTypes.KEYWORDS, keywordWithUmlauts))

            val newPhotoshopData = IptcMetadata(newRecords, newBlocks)

            /* Write end result */

            val exifWriter = ByteArrayByteWriter()

            JpegRewriter.updateExifMetadataLossless(
                ByteArrayByteReader(bytes), exifWriter, outputSet
            )

            val newExifBytes = exifWriter.toByteArray()

            val iptcWriter = ByteArrayByteWriter()

            JpegRewriter.writeIPTC(ByteArrayByteReader(newExifBytes), iptcWriter, newPhotoshopData)

            val iptcBytes = iptcWriter.toByteArray()

            val xmpWriter = ByteArrayByteWriter()

            JpegRewriter.updateXmpXml(ByteArrayByteReader(iptcBytes), xmpWriter, newXmp)

            val actualMetadataBytes = xmpWriter.toByteArray()

            val expectedMetadataBytes = KimTestData.getModifiedBytesOf(index)

            val equals = expectedMetadataBytes.contentEquals(actualMetadataBytes)

            if (!equals) {

                Path("build/photo_${index}_modified.jpg")
                    .writeBytes(actualMetadataBytes)

                fail("Photo $index has not the expected bytes!")
            }
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    @Suppress("LoopWithTooManyJumpStatements", "LongMethod", "NestedBlockDepth")
    fun testRewriteMetadataUnchanged() {

        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            // TODO Handle broken file (bad IFD1)
            if (index == 21)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val expectedMetadata = Kim.readMetadata(bytes) as ImageMetadata

            val expectedOutputSet = expectedMetadata.exif?.createOutputSet() ?: continue

            val byteWriter = ByteArrayByteWriter()

            JpegRewriter.updateExifMetadataLossless(ByteArrayByteReader(bytes), byteWriter, expectedOutputSet)

            val newBytes = byteWriter.toByteArray()

            val actualMetadata = Kim.readMetadata(newBytes)

            assertNotNull(actualMetadata)

            val actualOutputSet = actualMetadata.exif?.createOutputSet() ?: continue

            assertEquals(
                expectedOutputSet.getDirectories().size,
                actualOutputSet.getDirectories().size,
                "Different directory count."
            )

            for (directoryIndex in expectedOutputSet.getDirectories().indices) {

                val expectedDirectory = expectedOutputSet.getDirectories()[directoryIndex]
                val actualDirectory = actualOutputSet.getDirectories()[directoryIndex]

                val fieldCountMatches = expectedDirectory.getFields().size == actualDirectory.getFields().size

                if (!fieldCountMatches) {

                    val expectedTagInfos = expectedDirectory.getFields().map { it.tagInfo.tag }.sorted()
                    val actualTagInfos = actualDirectory.getFields().map { it.tagInfo.tag }.sorted()

                    val missingTagInfos = expectedTagInfos - actualTagInfos.toSet() -
                        /* For some reason this offsets disappear, even if they are written. */
                        // FIXME Find out, why
                        ExifTag.EXIF_TAG_EXIF_OFFSET.tag -
                        ExifTag.EXIF_TAG_GPSINFO.tag -
                        ExifTag.EXIF_TAG_INTEROP_OFFSET.tag -
                        TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT.tag

                    if (missingTagInfos.isNotEmpty())
                        fail(
                            "For file $index, expected ${expectedTagInfos.size} tags, " +
                                "but got ${actualTagInfos.size} tags. Missing: $missingTagInfos." +
                                "Expected: $expectedTagInfos. Actual: $actualTagInfos"
                        )
                }

                val expectedFields = expectedDirectory.getFields()

                @Suppress("LoopWithTooManyJumpStatements")
                for (expectedField in expectedFields) {

                    val actualField = actualDirectory.findField(expectedField.tagInfo)

                    /* Ignore missing offset fields that may not be needed after rewrite. */
                    @Suppress("ComplexCondition")
                    if (expectedField.tagInfo == ExifTag.EXIF_TAG_EXIF_OFFSET ||
                        expectedField.tagInfo == ExifTag.EXIF_TAG_GPSINFO ||
                        expectedField.tagInfo == ExifTag.EXIF_TAG_INTEROP_OFFSET ||
                        expectedField.tagInfo == TiffTag.TIFF_TAG_JPEG_INTERCHANGE_FORMAT
                    )
                        continue

                    /* Why is there this tag? */
                    if (expectedField.tagInfo.tag == -1)
                        continue

                    /* JPEGInterchangeFormatLength != JpgFromRawLength */
                    if (expectedField.tagInfo.tag == 514)
                        continue

                    assertNotNull(
                        actualField,
                        "Image $index, value for ${expectedField.tagInfo} not found."
                    )

                    assertEquals(
                        expectedField.tag,
                        actualField.tag,
                        "Tag mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.fieldType,
                        actualField.fieldType,
                        "Field type mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.tagInfo,
                        actualField.tagInfo,
                        "Tag info mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.count,
                        actualField.count,
                        "Count mismatch for image #$index."
                    )

                    assertEquals(
                        expectedField.count,
                        actualField.count,
                        "Count mismatch for image #$index."
                    )

                    /* Value of offsets is expected to change due to rewrites. */
                    if (
                        expectedField.tag == exifOffsetTag ||
                        expectedField.tag == interopOffsetTag ||
                        expectedField.tag == gpsInfoTag
                    )
                        continue

                    val expectedValue = expectedMetadata.findTiffField(expectedField.tagInfo)?.value
                    val actualValue = actualMetadata.findTiffField(actualField.tagInfo)?.value

                    if (!isEquals(expectedValue, actualValue)) {

                        val expectedValueString = if (expectedValue is Array<*>)
                            expectedValue.contentToString()
                        else
                            expectedValue?.toString()

                        val actualValueString = if (actualValue is Array<*>)
                            actualValue.contentToString()
                        else
                            actualValue?.toString()

                        fail(
                            "Value mismatch for image #$index and field ${expectedField.tagInfo}: " +
                                "$expectedValueString != $actualValueString"
                        )
                    }
                }
            }
        }
    }

    /**
     * Regression test based on a fixed small set of test files.
     */
    @Test
    fun testUpdateXmp() {

        @Suppress("LoopWithTooManyJumpStatements")
        for (index in 1..KimTestData.HIGHEST_JPEG_INDEX) {

            // TODO Handle broken file (bad IFD1)
            if (index == 21)
                continue

            /* Skip files without embedded XMP */
            if (index == 2 || index == 20 || index == 48)
                continue

            val bytes = KimTestData.getBytesOf(index)

            val originalXmp = Kim.readMetadata(bytes)?.xmp

            val newXmp = KimTestData.getFormattedXmp(index)

            assertNotEquals(originalXmp, newXmp)

            val byteWriter = ByteArrayByteWriter()

            JpegRewriter.updateXmpXml(ByteArrayByteReader(bytes), byteWriter, newXmp)

            val newBytes = byteWriter.toByteArray()

            val newBytesXmp = Kim.readMetadata(newBytes)?.xmp

            assertEquals(newXmp, newBytesXmp)
        }
    }

    private fun isEquals(first: Any?, second: Any?): Boolean =
        if (first is Array<*> && second is Array<*>)
            first.contentEquals(second)
        else if (first is ByteArray && second is ByteArray)
            first.contentEquals(second)
        else if (first is ShortArray && second is ShortArray)
            first.contentEquals(second)
        else
            first == second

    companion object {

        private const val exifOffsetTag = 0x8769
        private const val interopOffsetTag = 0xa005
        private const val gpsInfoTag = 0x8825
    }
}
