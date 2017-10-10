package org.team2471.frc.lib.math

data class Vector2D(val x: Double, val y: Double) {
    val length: Double get() = Math.sqrt(dot(this))

    val angle: Double get() = Math.atan2(x, y)

    operator fun unaryPlus() = this * 1.0

    operator fun unaryMinus() = this * -1.0

    operator fun plus(b: Vector2D) = Vector2D(x + b.x, y + b.y)

    operator fun minus(b: Vector2D) = Vector2D(x - b.x, y - b.y)

    operator fun times(scalar: Double) = Vector2D(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Vector2D(x / scalar, y / scalar)

    fun dot(b: Vector2D) = (x * b.x) + (y * b.y)

    fun normalize() = this / length

    fun perpendicular() = Vector2D(y, -x)
}
