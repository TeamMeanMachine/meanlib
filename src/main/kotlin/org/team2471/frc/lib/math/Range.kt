package org.team2471.frc.lib.math

typealias DoubleRange = ClosedFloatingPointRange<Double>

fun DoubleRange.intersects(other: DoubleRange): Boolean =
        start in other || endInclusive in other