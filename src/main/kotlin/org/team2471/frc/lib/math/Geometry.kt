package org.team2471.frc.lib.math

import java.lang.Math.*
import java.util.Arrays.asList




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

    fun closestPoint(firstPoint: Point2D, vararg additionalPoints: Point2D): Point2D =
            additionalPoints.fold(firstPoint) { result, next ->
                if (distance(next) < distance(result)) next
                else result
            }
}

data class Line2D(val pointA: Point2D, val pointB: Point2D) {
    val slope = (pointB.y - pointA.y) / (pointB.x - pointA.x)
    val intercept = -slope * pointA.x + pointA.y

    operator fun get(x: Double): Double = slope * x + intercept

    operator fun plus(vec: Vector2D) = Line2D(pointA + vec, pointB + vec)

    operator fun minus(vec: Vector2D) = Line2D(pointA - vec, pointB - vec)

    fun pointInLine(point: Point2D): Boolean = point.y == this[point.x]

    fun pointInSegment(point: Point2D): Boolean =
            pointInLine(point) && point.distance(pointA) + point.distance(pointB) == pointA.distance(pointB)
}

data class Circle(val center: Point2D, val radius: Double) {
    companion object {
        @JvmStatic
        val unit = Circle(Point2D.origin, 1.0)
    }

    // adapted from: https://stackoverflow.com/a/13055116
    fun intersectingPoints(line: Line2D): Array<Point2D> {
        val (pointA, pointB) = line

        val baX = pointB.x - pointA.x
        val baY = pointB.y - pointA.y
        val caX = center.x - pointA.x
        val caY = center.y - pointA.y

        val a = baX * baX + baY * baY
        val bBy2 = baX * caX + baY * caY
        val c = caX * caX + caY * caY - radius * radius

        val pBy2 = bBy2 / a
        val q = c / a

        val disc = pBy2 * pBy2 - q
        if (disc < 0) {
            return emptyArray()
        }
        // if disc == 0 ... dealt with later
        val tmpSqrt = Math.sqrt(disc)
        val abScalingFactor1 = -pBy2 + tmpSqrt
        val abScalingFactor2 = -pBy2 - tmpSqrt

        val p1 = Point2D(pointA.x - baX * abScalingFactor1, pointA.y - baY * abScalingFactor1)
        if (disc == 0.0) { // abScalingFactor1 == abScalingFactor2
            return arrayOf(p1)
        }
        val p2 = Point2D(pointA.x - baX * abScalingFactor2, pointA.y - baY * abScalingFactor2)
        return arrayOf(p1, p2)
    }

    operator fun plus(vec: Vector2D) = Circle(center + vec, radius)

    operator fun minus(vec: Vector2D) = Circle(center - vec, radius)

    operator fun times(scalar: Double) = Circle(center, radius * scalar)

    operator fun div(scalar: Double) = Circle(center, radius / scalar)
}
