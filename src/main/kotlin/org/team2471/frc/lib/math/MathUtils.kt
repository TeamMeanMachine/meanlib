package org.team2471.frc.lib.math

import java.lang.Math.pow
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

fun Double.log10() = Math.log10(this)

fun average(vararg x: Double) = x.sum() / x.size

fun lerp(min: Double, max: Double, k: Double) = min + (max - min) * k

fun Double.deadband(tolerance: Double) = if (Math.abs(this) < tolerance) {
    0.0
} else {
    (this - Math.copySign(tolerance, this)) / (1.0 - tolerance)
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
    return (inAlpha-inLo) / (inHi-inLo) * (outHi-outLo) + outLo
}

fun cubicMap(inLo: Double, inHi: Double, outLo: Double, outHi: Double, inAlpha: Double): Double {
    val x = (inAlpha-inLo) / (inHi-inLo)
    val cubic = (3 - 2 * x) * x * x
    return cubic * (outHi-outLo) + outLo
}
