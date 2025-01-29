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
package com.ashampoo.kim.model

/**
 * Iptc4xmpExt:LocationShown
 */
public data class LocationShown(

    /**
     * Descriptive name like "Times Square" or "//CRASH"
     */
    val name: String?,

    /**
     * Often a street adress like "Schafjückenweg 2"
     *
     * Also known as "Sublocation".
     */
    val street: String?,

    /**
     * The city, for example "Rastede"
     */
    val city: String?,

    /**
     * The state, for example "Niedersachsen"
     */
    val state: String?,

    /**
     * The city, for example "Deutschland"
     */
    val country: String?
) {

    val displayString: String? =
        when {

            name != null && country != null -> "$name, $country"
            name != null -> name

            city != null && country != null -> "$city, $country"
            city != null -> city

            state != null && country != null -> "$state, $country"
            state != null -> state

            else -> country
        }
}
