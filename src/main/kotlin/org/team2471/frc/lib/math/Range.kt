package org.team2471.frc.lib.math

class DoubleRange(override val start: Double, override val endInclusive: Double): ClosedFloatingPointRange<Double> {
        override fun lessThanOrEquals(a: Double, b: Double): Boolean = a <= b
}

fun DoubleRange.intersects(other: DoubleRange): Boolean =
        start in other || endInclusive in other