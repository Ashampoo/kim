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
package com.ashampoo.kim.android

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.common.ImageWriteException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.AndroidInputStreamByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.ByteWriter
import com.ashampoo.kim.output.OutputStreamByteWriter
import java.io.File
import java.io.InputStream

/**
 * Extra object to have a nicer API for Java projects
 */
public object KimAndroid {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
        Kim.readMetadata(
            byteReader = AndroidInputStreamByteReader(
                inputStream = inputStream,
                contentLength = length
            )
        )

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): ImageMetadata? =
        readMetadata(File(path))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(file: File): ImageMetadata? {

        check(file.exists()) { "File does not exist: $file" }

        return readMetadata(
            inputStream = file.inputStream().buffered(),
            length = file.length()
        )
    }

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        context: Context,
        uri: String,
        length: Long? = null
    ): ImageMetadata? =
        Kim.readMetadata(
            byteReader = createByteReader(
                contentResolver = context.contentResolver,
                uri = uri,
                length = length
            )
        )

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(
        contentResolver: ContentResolver,
        uri: String,
        length: Long? = null
    ): ImageMetadata? =
        Kim.readMetadata(
            byteReader = createByteReader(
                contentResolver = contentResolver,
                uri = uri,
                length = length
            )
        )

    @Throws(ImageReadException::class)
    public fun createByteReader(
        contentResolver: ContentResolver,
        uri: String,
        length: Long? = null
    ): ByteReader {

        val androidUri = Uri.parse(uri)

        /*
         * On Android 10 (API 29) and above, we must use ContentResolver
         * due to Scoped Storage restrictions. For older versions, we can
         * directly access the file system using file paths.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            /*
             * If a length was provided we use that,
             * otherwise we receive it from the contentResolver.
             */
            val contentLength = length ?: contentResolver.getFileSize(androidUri)

            if (contentLength == null)
                throw ImageReadException("Unable to get file size for URI $uri")

            val inputStream = contentResolver.openInputStream(androidUri)

            if (inputStream == null)
                throw ImageReadException("Unable to open input stream for URI $uri")

            return AndroidInputStreamByteReader(inputStream, contentLength)
        }

        /*
         * Fall back to the old way
         */

        val pathname = androidUri.path

        if (pathname == null)
            throw ImageReadException("Unable to find path for URI $uri")

        val file = File(pathname)

        return AndroidInputStreamByteReader(
            inputStream = file.inputStream(),
            contentLength = length ?: file.length()
        )
    }

    @Throws(ImageWriteException::class)
    public fun createByteWriter(
        contentResolver: ContentResolver,
        uri: String
    ): ByteWriter {

        val androidUri = Uri.parse(uri)

        /*
         * On Android 10 (API 29) and above, we must use ContentResolver
         * due to Scoped Storage restrictions. For older versions, we can
         * directly access the file system using file paths.
         */

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val outputStream = contentResolver.openOutputStream(androidUri)

            if (outputStream == null)
                throw ImageWriteException("Unable to open ouput stream for URI $uri")

            return OutputStreamByteWriter(outputStream)
        }

        /*
         * Fall back to the old way
         */

        val pathname = androidUri.path

        if (pathname == null)
            throw ImageWriteException("Unable to find path for URI $uri")

        val file = File(pathname)

        return OutputStreamByteWriter(file.outputStream())
    }

    private fun ContentResolver.getFileSize(
        uri: Uri
    ): Long? {

        val cursor = query(
            uri,
            null,
            null,
            null,
            null
        )

        return cursor?.let {

            it.moveToFirst()

            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

            val size = it.getLong(sizeIndex)

            it.close()

            size
        }
    }
}

@Throws(ImageReadException::class)
public fun Kim.readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
    KimAndroid.readMetadata(inputStream, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(path: String): ImageMetadata? =
    KimAndroid.readMetadata(path)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(file: File): ImageMetadata? =
    KimAndroid.readMetadata(file)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(
    context: Context,
    uri: String,
    length: Long? = null
): ImageMetadata? =
    KimAndroid.readMetadata(context, uri, length)

@Throws(ImageReadException::class)
public fun Kim.readMetadata(
    contentResolver: ContentResolver,
    uri: String,
    length: Long? = null
): ImageMetadata? =
    KimAndroid.readMetadata(contentResolver, uri, length)
