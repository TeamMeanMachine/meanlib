package org.team2471.frc.lib.math

import java.lang.Math.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.fitToRange(min: Double, max: Double): Double = when {
    this < min -> min
    this > max -> max
    else -> this
}

fun Int.fitToRange(min: Int, max: Int): Int = when {
    this < min -> min
    this > max -> max
    else -> this
}

fun square(x: Double): Double = pow(x, 2.0)

fun Double.squareWithSign() = Math.copySign(this * this, this)

fun Double.cube() = this * this * this

fun Double.squareRootWithSign() = Math.copySign(Math.sqrt(Math.abs(this)), this)

fun Double.log10() = Math.log10(this)

fun average(vararg x: Double) = x.sum() / x.size

fun lerp(min: Double, max: Double, k: Double) = min + (max - min) * k

fun Double.deadband(tolerance: Double): Double = if (Math.abs(this) < tolerance) {
    0.0
} else {
    (this - copySign(tolerance, this)) / (1.0 - tolerance)
}

// doesn't work with negative values of n
infix fun Double.mod(n: Double) = if (this < 0) {
    (this % n + n) % n
} else {
    this % n
}

fun round(number: Double, digits: Int): Double {
    val modulo = Math.pow(10.0, digits.toDouble())
    return (number * modulo).roundToInt() / modulo
}

fun linearMap(inLo: Double, inHi: Double, outLo: Double, outHi: Double, inAlpha: Double): Double {
    return (inAlpha - inLo) / (inHi - inLo) * (outHi - outLo) + outLo
}

fun Double.linearMap(inRange: DoubleRange, outRange: DoubleRange): Double =
    linearMap(inRange.start, inRange.endInclusive, outRange.start, outRange.endInclusive, this)

fun cubicMap(inLo: Double, inHi: Double, outLo: Double, outHi: Double, inAlpha: Double): Double {
    val x = (inAlpha - inLo) / (inHi - inLo)
    val cubic = (3 - 2 * x) * x * x
    return cubic * (outHi - outLo) + outLo
}

fun windRelativeAngles(angle1: Double, angle2: Double): Double {
    val diff = angle1 - angle2
    val absDiff = abs(diff)
    return if (absDiff > 180.0) {
        angle2 + 360.0 * signum(diff) * floor((absDiff / 360.0) + 0.5)
    } else {
        angle2
    }
}
