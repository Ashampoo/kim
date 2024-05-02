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
package com.ashampoo.kim.common

public open class ImageException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

public class ImageReadException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

public open class ImageWriteException(message: String? = null, cause: Throwable? = null) :
    ImageException(message, cause)

/**
 * We need to ensure that every Exception that can occur is wrapped
 * into an ImageReadException, because on Kotlin/Native this is the expected exception type.
 */
internal inline fun <R> tryWithImageReadException(block: () -> R): R =
    try {
        block()
    } catch (ex: ImageReadException) {
        /* Don't wrap another ImageReadException. */
        throw ex
    } catch (ex: Throwable) {
        throw ImageReadException("Failed to read image.", ex)
    }

/**
 * We need to ensure that everything is wrapped into an ImageWriteException,
 * because on Kotlin/Native this is the expected exception type.
 */
internal inline fun <R> tryWithImageWriteException(block: () -> R): R =
    try {
        block()
    } catch (ex: ImageWriteException) {
        /* Don't wrap another ImageWriteException. */
        throw ex
    } catch (ex: Throwable) {
        throw ImageWriteException("Failed to write image.", ex)
    }
