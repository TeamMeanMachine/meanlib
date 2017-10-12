package org.team2471.frc.lib.math

import java.lang.Math.*


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

    fun distance(b: Point2D): Double = sqrt(pow(b.x - this.x, 2.0) + pow(b.y - this.y, 2.0))

    fun vectorTo(b: Point2D): Vector2D = Vector2D(b.x - this.x, b.y - this.y)
}

data class Line2D(val pointA: Point2D, val pointB: Point2D) {
    val slope = (pointB.y - pointA.y) / (pointB.x - pointA.x)
    val intercept = -slope * pointA.x + pointA.y

    fun get(x: Double): Double = slope * x + intercept
}

data class Circle(val center: Point2D, val radius: Double) {
    // see: http://mathworld.wolfram.com/Circle-LineIntersection.html
    fun intersectingPoints(line: Line2D): List<Point2D> {
        val (point1, point2) = line
        val (dx, dy) = point2 - point1
        val dr = sqrt(square(dx) + square(dy))

        val determinate = point1.x * point2.y + point2.x * point1.y
        val discriminant = square(radius) * square(dr) - square(determinate)

        fun sgn(x: Double): Double = if (x < 0) -1.0 else 1.0
        return when {
            discriminant > 0.0 -> // two intersections
                listOf(
                        Point2D((determinate * dy + sgn(dy) * dx * sqrt(discriminant)) / square(dr),
                                (-determinate * dx + abs(dy) * sqrt(discriminant)) / square(dr)),
                        Point2D((determinate * dy - sgn(dy) * dx * sqrt(discriminant)) / square(dr),
                                (-determinate * dx - abs(dy) * sqrt(discriminant)) / square(dr)))
            discriminant == 0.0 -> // tangent line, one intersection
                listOf(Point2D(
                        (determinate * dy + sgn(dy)) / square(dr),
                        (-determinate * dx + abs(dy)) / square(dr)))
            else -> emptyList() // no intersections
        }
    }
}
