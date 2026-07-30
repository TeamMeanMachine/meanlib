package org.team2471.frc.lib.motion_profiling

data class DoublePair(var x: Double, var y: Double) {
    operator fun plus(b: DoublePair) = DoublePair(x + b.x, y + b.y)

    operator fun minus(b: DoublePair) = DoublePair(x - b.x, y - b.y)

    operator fun times(scalar: Double) = DoublePair(x * scalar, y * scalar)

    operator fun div(scalar: Double) = DoublePair(x / scalar, y / scalar)

    fun set(X: Double, Y: Double) {
        x = X
        y = Y
    }
}