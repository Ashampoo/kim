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
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns

private val sizeProjection = arrayOf(
    MediaStore.Images.Media.SIZE
)

private val sizeAndDateProjection = arrayOf(
    MediaStore.Images.Media.SIZE,
    MediaStore.Images.Media.DATE_MODIFIED
)

private val emptySelection: String? = null
private val emptySelectionArgs: Array<String>? = null
private val emptySortOrder: String? = null

public fun ContentResolver.getFileSize(
    uri: Uri
): Long? {

    val cursor = query(
        uri,
        sizeProjection,
        emptySelection,
        emptySelectionArgs,
        emptySortOrder
    )

    if (cursor == null)
        return null

    cursor.use {

        if (!it.moveToFirst())
            return null

        val sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)

        val size = it.getLong(sizeIndex)

        return size
    }
}

public fun ContentResolver.getSizeAndModificationDate(
    uri: Uri
): Pair<Long, Long>? {

    val cursor = query(
        uri,
        sizeAndDateProjection,
        emptySelection,
        emptySelectionArgs,
        emptySortOrder
    )

    if (cursor == null)
        return null

    cursor.use {

        if (!it.moveToFirst())
            return null

        val sizeIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val dateIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

        val size = cursor.getLong(sizeIndex)
        val date = cursor.getLong(dateIndex) * 1000

        return size to date
    }
}
