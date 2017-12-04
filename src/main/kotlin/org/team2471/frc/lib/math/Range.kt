package org.team2471.frc.lib.math

data class Range(val min: Double, val max: Double) {
    companion object {
        val CIRCULAR_RANGE = Range(0.0, 360.0)
        val COMPASS_RANGE = Range(-180.0, 180.0)
    }

    val delta = max - min

    fun fit(num: Double) = when {
        num < min -> min
        num > max -> max
        else -> num
    }

    fun wrap(num: Double) = (num - min) mod delta + min
}
