package org.team2471.frc.lib.math

import java.lang.Math.pow

fun fitToRange(value: Double, min: Double, max: Double): Double = when {
    value < min -> min
    value > max -> max
    else -> value
}

fun fitToRange(value: Int, min: Int, max: Int): Int = when {
    value < min -> min
    value > max -> max
    else -> value
}

fun square(x: Double): Double = pow(x, 2.0)

fun average(vararg x: Double) = x.sum() / x.size

fun lerp(min: Double, max: Double, k: Double) = min + (max - min) * k
