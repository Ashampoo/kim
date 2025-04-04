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

import android.R.attr.path
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.ImageReadException
import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.input.AndroidInputStreamByteReader
import com.ashampoo.kim.input.ByteReader
import com.ashampoo.kim.output.AndroidOutputStreamByteWriter
import com.ashampoo.kim.output.ByteWriter
import java.io.File
import java.io.InputStream

/**
 * Extra object to have a nicer API for Java projects
 */
public object KimAndroid {

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(inputStream: InputStream, length: Long): ImageMetadata? =
        Kim.readMetadata(AndroidInputStreamByteReader(inputStream, length))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(path: String): ImageMetadata? =
        readMetadata(File(path))

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(context: Context, uri: String): ImageMetadata? =
        getByteReader(context = context, uri = uri)?.let {
            Kim.readMetadata(it)
        }

    @JvmStatic
    @Throws(ImageReadException::class)
    public fun readMetadata(file: File): ImageMetadata? {

        check(file.exists()) { "File does not exist: $file" }

        return readMetadata(file.inputStream().buffered(), file.length())
    }

    public fun getByteReader(context: Context, uri: String): ByteReader? =
        Uri.parse(uri).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.getFileSize(this)?.let { size ->
                    context.contentResolver.openInputStream(this)?.let {
                        AndroidInputStreamByteReader(it, size)
                    }
                }
            } else {
                path?.let {
                    val file = File(it)
                    AndroidInputStreamByteReader(file.inputStream(), file.length())
                }
            }
        }

    public fun getByteWriter(context: Context, uri: String): ByteWriter? =
        Uri.parse(uri).run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver.openOutputStream(this)?.let {
                    AndroidOutputStreamByteWriter(it)
                }
            } else {
                path?.let {
                    val file = File(it)
                    AndroidOutputStreamByteWriter(file.outputStream())
                }
            }
        }

    private fun ContentResolver.getFileSize(
        uri: Uri,
    ): Long? {
        val cursor = query(uri, null, null, null, null)
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
