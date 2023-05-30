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
package com.ashampoo.kim.common

open class ImageException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

class ImageReadException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

open class ImageWriteException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

class ExifOverflowException(message: String) :
    ImageWriteException(message, null)
