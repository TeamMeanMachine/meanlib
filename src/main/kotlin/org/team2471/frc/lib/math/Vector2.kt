package org.team2471.frc.lib.math

import com.team254.lib.util.Interpolable

data class Vector2(var x: Double, var y: Double) : Interpolable<Vector2> {
    val length: Double get() = Math.sqrt(dot(this))
    val angle: Double get() = Math.atan2(x, y)

    fun rotateRadians(radians: Double): Vector2 {
        val c = Math.cos(radians)
        val s = Math.sin(radians)
        return Vector2(x * c - y * s, x * s + y * c)
    }

    fun rotateDegrees(degrees: Double): Vector2 = rotateRadians(Math.toRadians(degrees))

    operator fun unaryPlus() = this * 1.0

    operator fun unaryMinus() = this * -1.0

    operator fun plus(b: Vector2) = Vector2(x + b.x, y + b.y)

    operator fun minus(b: Vector2) = Vector2(x - b.x, y - b.y)

    operator fun times(scalar: Double) = Vector2(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Vector2(x / scalar, y / scalar)

    fun dot(b: Vector2) = (x * b.x) + (y * b.y)

    fun normalize() = this / length

    fun perpendicular() = Vector2(y, -x)

    fun mirrorXAxis() = Vector2(-x, y)

    fun mirrorYAxis() = Vector2(x, -y)

    fun distance(other: Vector2) = Math.hypot(x - other.x, y - other.y)

    fun set(other: Vector2) {
        x = other.x
        y = other.y
    }

    fun set(X: Double, Y: Double) {
        x = X
        y = Y
    }

    override fun interpolate(other: Vector2, x: Double): Vector2 {
        return when {
            x <= 0.0 -> this
            x >= 1.0 -> other
            else -> Vector2(x * (other.x - this.x) + this.x, x * (other.y - this.y) + this.y)
        }
    }
}
