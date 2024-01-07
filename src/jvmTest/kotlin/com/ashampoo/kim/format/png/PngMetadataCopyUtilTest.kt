/*
 * Copyright 2024 Ashampoo GmbH & Co. KG
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
package com.ashampoo.kim.format.png

import com.ashampoo.kim.common.copyTo
import com.ashampoo.kim.common.exists
import com.ashampoo.kim.common.readBytes
import com.ashampoo.kim.testdata.KimTestData
import kotlinx.io.files.Path
import org.junit.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class PngMetadataCopyUtilTest {

    @Test
    fun testCopy() {

        val source = Path(KimTestData.getFullImageDiskPath(52))

        val destination = Path("build/copy_test.png")

        /* Copy test image to local folder. */
        Path(KimTestData.getFullImageDiskPath(51)).copyTo(destination)

        /* Check that the file was actually copied. */
        assertTrue(destination.exists(), "copy_test.png does not exist.")

        PngMetadataCopyUtil.copy(
            source = source,
            destination = destination
        )

        val expectedBytes =
            Path("src/commonTest/resources/com/ashampoo/kim/copy_test.png")
                .readBytes()

        val actualBytes = destination.readBytes()

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals)
            fail("copy_test.png has not the expected bytes!")
    }

    @Test
    fun testCopyByteArray() {

        val sourceBytes = Path(KimTestData.getFullImageDiskPath(52)).readBytes()

        val destinationBytes = Path(KimTestData.getFullImageDiskPath(51)).readBytes()

        val expectedBytes =
            Path("src/commonTest/resources/com/ashampoo/kim/copy_test.png")
                .readBytes()

        val actualBytes = PngMetadataCopyUtil.copy(
            source = sourceBytes,
            destination = destinationBytes
        )

        val equals = expectedBytes.contentEquals(actualBytes)

        if (!equals)
            fail("copy_test.png has not the expected bytes!")
    }
}
