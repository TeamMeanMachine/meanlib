package org.team2471.frc.lib.motion_profiling

data class BooleanPair(var x: Double, var y: Double) {
    operator fun plus(b: BooleanPair) = BooleanPair(x + b.x, y + b.y)

    operator fun minus(b: BooleanPair) = BooleanPair(x - b.x, y - b.y)

    operator fun times(scalar: Double) = BooleanPair(x * scalar, y * scalar)

    operator fun div(scalar: Double) = BooleanPair(x / scalar, y / scalar)

    fun set(X: Double, Y: Double) {
        x = X
        y = Y
    }
}