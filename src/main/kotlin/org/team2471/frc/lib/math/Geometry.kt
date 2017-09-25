package org.team2471.frc.lib.math

data class Point2D(val x: Double, val y: Double)

operator fun Point2D.unaryMinus() = Point2D(-x, -y)

operator fun Point2D.plus(b: Point2D) = Point2D(x + b.x, y + b.y)

operator fun Point2D.minus(b: Point2D) = Point2D(x - b.x, y - b.y)

operator fun Point2D.times(scalar: Double) = Point2D(x * scalar, y * scalar)

operator fun Point2D.div(scalar: Double) = Point2D(x / scalar, y / scalar)
