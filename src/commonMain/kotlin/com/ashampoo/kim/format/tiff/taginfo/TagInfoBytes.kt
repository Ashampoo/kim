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
package com.ashampoo.kim.format.tiff.taginfo

import com.ashampoo.kim.format.tiff.constant.TiffDirectoryType
import com.ashampoo.kim.format.tiff.fieldtype.FieldType

open class TagInfoBytes : TagInfo {

    constructor(
        name: String,
        tag: Int,
        length: Int,
        directoryType: TiffDirectoryType?
    ) : super(name, tag, FieldType.BYTE, length, directoryType)

    constructor(
        name: String,
        tag: Int,
        fieldTypes: List<FieldType>,
        length: Int,
        directoryType: TiffDirectoryType?
    ) : super(name, tag, fieldTypes, length, directoryType)

    constructor(
        name: String,
        tag: Int,
        fieldType: FieldType,
        length: Int,
        directoryType: TiffDirectoryType?
    ) : super(name, tag, fieldType, length, directoryType)
}