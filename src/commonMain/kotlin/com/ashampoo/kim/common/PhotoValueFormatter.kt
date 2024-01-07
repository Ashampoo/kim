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

import kotlin.math.round

object PhotoValueFormatter {

    const val MEGA_PIXEL_COUNT: Int = 1_000_000

    private const val BYTES_PER_KB = 1000.0

    private const val FUJI = "Fujifilm"
    private const val NIKON = "Nikon"
    private const val OLYMPUS = "Olympus"
    private const val SAMSUNG = "Samsung"
    private const val PENTAX = "Pentax"

    /**
     * Replacements for ugly manufacturer names found in EXIF information.
     */
    private val makerNameReplacements = mutableMapOf<String, String>().apply {

        /*
         * Fujifilm
         */
        this["FUJI PHOTO FILM CO., LTD."] = FUJI
        this["FUJIFILM"] = FUJI

        /*
         * Nikon
         */
        this["NIKON CORPORATION"] = NIKON
        this["NIKON"] = NIKON

        /*
         * Olympus
         */
        this["OLYMPUS IMAGING CORP."] = OLYMPUS
        this["OLYMPUS CORPORATION"] = OLYMPUS
        this["OLYMPUS"] = OLYMPUS

        /*
         * Sony
         */
        this["SONY"] = "Sony"

        /*
         * Samsung
         */
        this["SAMSUNG TECHWIN CO., LTD."] = SAMSUNG
        this["SAMSUNG TECHWIN Co."] = SAMSUNG
        this["SAMSUNG"] = SAMSUNG
        this["samsung"] = SAMSUNG

        /*
         * Leica
         */
        this["Leica Camera AG"] = "Leica"
        this["LEICA CAMERA AG"] = "Leica"

        /*
         * Pentax
         */
        this["PENTAX Corporation"] = PENTAX
        this["RICOH IMAGING COMPANY, LTD."] = PENTAX
        this["PENTAX RICOH IMAGING"] = PENTAX
        this["PENTAX"] = PENTAX

        /*
         * LG
         */
        this["LG Electronics"] = "LG"

        /*
         * Motorola
         */
        this["motorola"] = "Motorola"

        /*
         * Google
         */
        this["google"] = "Google"

        /*
         * Asus
         */
        this["asus"] = "Asus"
        this["ASUS"] = "Asus"

        /*
         * Epson
         */
        this["SEIKO EPSON CORP."] = "Epson"
        this["EPSON"] = "Epson"

        /*
         * XIAOMI
         */
        this["XIAOMI"] = "Xiaomi"

        /*
         * NULL values
         */
        this["MAKER NAME"] = ""
    }

    /**
     * Formats the file length and shows fractions above kilobytes.
     * This function tries to format byte sizes the Apple way.
     */
    fun formatFileLength(bytes: Long): String {

        if (bytes < BYTES_PER_KB)
            return "$bytes B"

        val kiloBytes = bytes / BYTES_PER_KB

        /*
         * Show no fractions for KB
         */
        if (kiloBytes < BYTES_PER_KB)
            return "${round(kiloBytes).toInt()} KB"

        val megaBytes = kiloBytes / BYTES_PER_KB

        if (megaBytes < BYTES_PER_KB)
            return "${formatFileLengthInternal(megaBytes)} MB"

        val gigaBytes = megaBytes / BYTES_PER_KB

        return "${formatFileLengthInternal(gigaBytes)} GB"
    }

    @Suppress("MagicNumber")
    private fun formatFileLengthInternal(fileLength: Double): String {

        val roundedSize = round(fileLength * 10) / 10

        if (roundedSize % 1.0 == 0.0)
            return roundedSize.toInt().toString()

        return roundedSize.toString()
    }

    /**
     * This method tries to create a pretty name out of
     * make and model for both camera and lens information.
     */
    fun createCameraOrLensName(make: String?, model: String?): String? {

        /*
         * Apparently real EXIF data has surprisingly a lot of
         * trailing white spaces we want to get rid of.
         */
        var makeMod = make?.trim()
        var modelMod = model?.trim()

        if (makeMod?.isEmpty() == true)
            makeMod = null

        if (modelMod?.isEmpty() == true)
            modelMod = null

        if (makeMod != null) {

            for (entry in makerNameReplacements.entries)
                makeMod = makeMod?.replace(entry.key, entry.value, ignoreCase = false)

            /* Trim again, just to be safe. */
            makeMod = makeMod?.trim()
        }

        if (modelMod != null) {

            for (entry in makerNameReplacements.entries)
                modelMod = modelMod?.replace(entry.key, entry.value, ignoreCase = false)

            /* Trim again, just to be safe. */
            modelMod = modelMod?.trim()
        }

        /**
         * If the name of the manufacturer/make is repeated in the model name
         * we don't want to double it. This is a typical issue with "Canon".
         */
        if (makeMod != null && modelMod?.startsWith(makeMod) == true)
            makeMod = null

        return if (makeMod != null && modelMod != null)
            "$makeMod $modelMod"
        else
            modelMod ?: makeMod
    }

    fun createModifiedLensName(
        cameraName: String?,
        lensName: String?
    ): String? {

        return if (cameraName != null && lensName != null) {

            /**
             * iPhone Lens Names start with the full camera name.
             * This takes a lot of space.
             */
            lensName.replaceFirst(cameraName, "").trim()

        } else {
            lensName
        }
    }

    fun createCameraAndLensName(
        cameraName: String?,
        lensName: String?
    ): String? {

        return if (cameraName != null && lensName != null) {

            /**
             * iPhone Lens Names start with the full camera name.
             * This takes a lot of space.
             */
            val modLensName = lensName.replaceFirst(cameraName, "").trim()

            "$cameraName | $modLensName"

        } else {
            cameraName ?: lensName
        }
    }

    fun formatIso(iso: Int): String = "ISO $iso"

    @Suppress("MagicNumber")
    fun formatExposureTime(seconds: Double): String {

        if (seconds < 1.0)
            return "1/" + (0.5 + 1 / seconds).toInt() + " s"

        val roundedSeconds = seconds.toInt()

        val fractionSeconds = seconds - roundedSeconds

        if (fractionSeconds > 0.0001)
            return "$roundedSeconds'' 1/" + (0.5 + 1 / fractionSeconds).toInt() + " s"

        return "$roundedSeconds'' s"
    }

    fun formatFNumber(fNumber: Double): String {

        return if (fNumber % 1.0 == 0.0)
            "ƒ${fNumber.toInt()}"
        else
            "ƒ$fNumber"
    }

    /*
     * Focal length is almost every time a round integer
     * number like "18mm" and should be formatted like that,
     * but in case of an iPhone is actually can be "4.2mm".
     */
    fun formatFocalLength(focalLength: Double): String {

        return if (focalLength % 1.0 == 0.0)
            "${focalLength.toInt()} mm"
        else
            "$focalLength mm"
    }
}
