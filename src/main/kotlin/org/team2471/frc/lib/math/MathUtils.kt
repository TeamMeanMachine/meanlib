package org.team2471.frc.lib.math

import java.lang.Math.floor
import java.lang.Math.pow

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

fun average(vararg x: Double) = x.sum() / x.size

fun lerp(min: Double, max: Double, k: Double) = min + (max - min) * k

fun Double.deadband(tolerance: Double) = if(Math.abs(this) < tolerance) {
    0.0
} else {
    (this - Math.copySign(tolerance, this)) / (1.0 - tolerance)
}

infix fun Double.floorMod (n: Double) = this - n * floor(this / n)
