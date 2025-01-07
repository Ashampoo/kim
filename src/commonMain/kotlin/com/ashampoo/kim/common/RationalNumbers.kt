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
package com.ashampoo.kim.common

/**
 * To avoid rather unsafe "Array<*>" in instance checking we have this
 * extra class to represent a collection of rational numbers.
 */
public class RationalNumbers(
    public val values: Array<RationalNumber>
) {

    override fun toString(): String =
        values.contentToString()

    override fun hashCode(): Int =
        values.contentHashCode()

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other == null || this::class != other::class)
            return false

        other as RationalNumbers

        return values.contentEquals(other.values)
    }
}
