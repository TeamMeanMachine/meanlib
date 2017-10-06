package org.team2471.frc.lib.math

data class Point2D(val x: Double, val y: Double) {
    companion object {
        @JvmStatic
        val origin: Point2D = Point2D(0.0, 0.0)
    }

    operator fun unaryPlus() = this

    operator fun unaryMinus() = Point2D(-x, -y)

    operator fun plus(b: Point2D) = Point2D(x + b.x, y + b.y)

    operator fun plus(vec: Vector2D) = Point2D(x + vec.x, y + vec.y)

    operator fun minus(b: Point2D) = Point2D(x - b.x, y - b.y)

    operator fun minus(vec: Vector2D) = Point2D(x - vec.x, y - vec.y)

    operator fun times(scalar: Double) = Point2D(x * scalar, y * scalar)

    operator fun div(scalar: Double) = Point2D(x / scalar, y / scalar)

    fun distance(b: Point2D): Double = Math.sqrt(
            Math.pow(b.x - this.x, 2.0) + Math.pow(b.y - this.y, 2.0)
    )
}
