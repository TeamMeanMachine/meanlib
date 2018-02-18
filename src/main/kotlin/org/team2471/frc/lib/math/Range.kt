package org.team2471.frc.lib.math

typealias DoubleRange = ClosedFloatingPointRange<Double>

fun DoubleRange.clamp(value: Double) = when {
    value < start -> start
    value > endInclusive -> endInclusive
    else -> value
}