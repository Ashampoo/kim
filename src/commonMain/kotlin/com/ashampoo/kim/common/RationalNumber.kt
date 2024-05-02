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
package com.ashampoo.kim.common

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Rational number, as used by the TIFF image format.
 *
 * The TIFF format specifies two data types for rational numbers based on
 * a pair of 32-bit integers. Rational is based on unsigned 32-bit integers
 * and SRational is based on signed 32-bit integers. This treatment is
 * problematic in Java because Java does not support unsigned types.
 * To address this challenge, this class stores the numerator and divisor
 * in long (64-bit) integers, applying masks as necessary for the unsigned type.
 */
public class RationalNumber {

    public val numerator: Long
    public val divisor: Long

    /*
     * The TIFF and EXIF specifications call for the use
     * of 32 bit unsigned integers. Since Java does not have an
     * unsigned type, this class widens the type to long in order
     * to avoid unintended negative numbers.
     */
    public val unsignedType: Boolean

    /**
     * Constructs an instance based on signed integers
     *
     * @param numerator a 32-bit signed integer
     * @param divisor   a non-zero 32-bit signed integer
     */
    constructor(numerator: Int, divisor: Int) {
        this.numerator = numerator.toLong()
        this.divisor = divisor.toLong()
        unsignedType = false
    }

    /**
     * Constructs an instance supports either signed or unsigned integers.
     *
     * @param numerator    a numerator in the indicated form (signed or unsigned)
     * @param divisor      a non-zero divisor in the indicated form (signed or unsigned)
     * @param unsignedType indicates whether the input values are to be treated as unsigned.
     */
    @Suppress("MagicNumber")
    constructor(numerator: Int, divisor: Int, unsignedType: Boolean) {

        this.unsignedType = unsignedType

        if (unsignedType) {
            this.numerator = numerator.toLong() and 0xFFFFFFFFL
            this.divisor = divisor.toLong() and 0xFFFFFFFFL
        } else {
            this.numerator = numerator.toLong()
            this.divisor = divisor.toLong()
        }
    }

    /**
     * A private constructor for methods such as negate() that create instances
     * of this class using the content of the current instance.
     *
     * @param numerator    a valid numerator
     * @param divisor      a valid denominator
     * @param unsignedType indicates how numerator and divisor values are to be interpreted.
     */
    private constructor(numerator: Long, divisor: Long, unsignedType: Boolean) {
        this.numerator = numerator
        this.divisor = divisor
        this.unsignedType = unsignedType
    }

    public fun doubleValue(): Double = numerator.toDouble() / divisor.toDouble()

    /**
     * Negates the value of the RationalNumber. If the numerator of this
     * instance has its high-order bit set, then its value is too large
     * to be treated as a Java 32-bit signed integer. In such a case, the
     * only way that a RationalNumber instance can be negated is to
     * divide both terms by a common divisor, if a non-zero common divisor exists.
     * However, if no such divisor exists, there is no numerically correct
     * way to perform the negation. When a negation cannot be performed correctly,
     * this method throws an unchecked exception.
     */
    public fun negate(): RationalNumber {

        val isUnsignedType = unsignedType
        val negatedNumerator = -numerator
        val commonDivisor = greatestCommonDivisor(numerator, divisor)

        /*
         * An instance of an unsigned type can be negated if and only if
         * its high -order bit (the sign bit) is clear. If the bit is set,
         * the value will be too large to convert to a signed type.
         * In such a case it is necessary to adjust the numerator and denominator
         * by their greatest common divisor(gcd), if one exists.
         * If no non - zero common divisor exists, an exception is thrown.
         */
        if (isUnsignedType && negatedNumerator < 0) {

            if (commonDivisor == 0L)
                throw NumberFormatException("Unsigned numerator is too large to negate: $numerator")

            return RationalNumber(
                numerator = -(numerator / commonDivisor),
                divisor = divisor / commonDivisor,
                unsignedType = false
            )
        }

        return RationalNumber(negatedNumerator, divisor, false)
    }

    override fun toString(): String {

        if (divisor == 0L)
            return "Invalid rational ($numerator/$divisor)"

        /* Display a rounded number to avoid different results on different platforms. */
        return "$numerator/$divisor (${doubleValue().roundTo(TO_STRING_DOUBLE_ROUND_FRACTION_DIGITS)})"
    }

    private fun Double.roundTo(numFractionDigits: Int): Double {
        val factor = 10.0.pow(numFractionDigits.toDouble())
        return (this * factor).roundToLong() / factor
    }

    override fun equals(other: Any?): Boolean {

        if (this === other)
            return true

        if (other !is RationalNumber)
            return false

        if (numerator != other.numerator)
            return false

        if (divisor != other.divisor)
            return false

        if (unsignedType != other.unsignedType)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = numerator.hashCode()
        result = 31 * result + divisor.hashCode()
        result = 31 * result + unsignedType.hashCode()
        return result
    }

    public companion object {

        private const val TO_STRING_DOUBLE_ROUND_FRACTION_DIGITS = 6

        private const val INT_PRECISION_TOLERANCE = 1E-8
        private const val MAX_ITERATIONS = 100

        public fun create(numerator: Long, divisor: Long): RationalNumber {

            val normalizedFraction = normalizeFraction(numerator, divisor)

            val gcd = greatestCommonDivisor(normalizedFraction.first, normalizedFraction.second)

            val reducedNumerator = normalizedFraction.first / gcd
            val reducedDivisor = normalizedFraction.second / gcd

            return RationalNumber(reducedNumerator.toInt(), reducedDivisor.toInt())
        }

        private fun normalizeFraction(numerator: Long, divisor: Long): Pair<Long, Long> {

            val normalizedNumerator = normalizeValue(numerator)
            val normalizedDivisor = normalizeValue(divisor)

            if (normalizedDivisor == 0L)
                error("Invalid value, numerator: $normalizedNumerator, divisor: $normalizedDivisor")

            return normalizedNumerator to normalizedDivisor
        }

        private fun normalizeValue(value: Long): Long {

            val intValue = value.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())

            var normalizedValue = intValue

            while (normalizedValue != value && abs(normalizedValue) > 1)
                normalizedValue = normalizedValue shr 1

            return normalizedValue
        }

        private fun greatestCommonDivisor(a: Long, b: Long): Long {

            var number1 = a
            var number2 = b

            while (number2 != 0L)
                number1 = number2.also { number2 = number1 % number2 }

            return number1.absoluteValue
        }

        private class Option private constructor(
            val rationalNumber: RationalNumber,
            val error: Double
        ) {

            override fun toString(): String = rationalNumber.toString()

            companion object {

                fun create(rationalNumber: RationalNumber, value: Double): Option =
                    Option(rationalNumber, abs(rationalNumber.doubleValue() - value))
            }
        }

        /**
         * Calculate rational number using successive approximations.
         */
        fun valueOf(value: Double): RationalNumber {

            if (value >= Int.MAX_VALUE)
                return RationalNumber(Int.MAX_VALUE, 1)

            if (value <= -Int.MAX_VALUE)
                return RationalNumber(-Int.MAX_VALUE, 1)

            val isNegative = value < 0
            val absValue = abs(value)

            if (absValue == 0.0)
                return RationalNumber(0, 1)

            val low: RationalNumber
            val high: RationalNumber

            if (absValue >= 1) {

                val approx = absValue.toInt()

                if (approx < absValue)
                    low = RationalNumber(approx, 1)
                else
                    low = RationalNumber(approx - 1, 1)

                high = RationalNumber(approx + 1, 1)

            } else {

                val approx = (1.0 / absValue).toInt()

                if (1.0 / approx < absValue)
                    low = RationalNumber(1, approx)
                else
                    low = RationalNumber(1, approx + 1)

                high = RationalNumber(1, approx - 1)
            }

            var lowOption = Option.create(low, absValue)
            var highOption = Option.create(high, absValue)
            var bestOption = if (lowOption.error < highOption.error) lowOption else highOption
            var iteration = 0

            @Suppress("LoopWithTooManyJumpStatements")
            while (bestOption.error > INT_PRECISION_TOLERANCE && iteration < MAX_ITERATIONS) {

                val mediant = create(
                    lowOption.rationalNumber.numerator + highOption.rationalNumber.numerator,
                    lowOption.rationalNumber.divisor + highOption.rationalNumber.divisor
                )

                val mediantOption = Option.create(mediant, absValue)

                if (absValue < mediant.doubleValue()) {

                    if (highOption.error <= mediantOption.error)
                        break

                    highOption = mediantOption

                } else {

                    if (lowOption.error <= mediantOption.error)
                        break

                    lowOption = mediantOption
                }

                if (mediantOption.error < bestOption.error)
                    bestOption = mediantOption

                iteration++
            }

            return if (isNegative)
                bestOption.rationalNumber.negate()
            else
                bestOption.rationalNumber
        }
    }
}
