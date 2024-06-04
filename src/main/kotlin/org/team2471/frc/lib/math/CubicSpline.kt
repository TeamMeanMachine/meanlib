package org.team2471.frc.lib.math

import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.radians
import kotlin.math.pow

// math can be found here if you want to bore yourself:
// https://math.stackexchange.com/questions/1522439/generalised-formula-for-fitting-a-cubic-between-two-points-with-specified-slopes

// Creates a spline between two points. The spline begins and ends with the two specified slopes (m1 & m2).
class CubicSpline(
    var x1: Double,
    var y1: Double,
    var x2: Double,
    var y2: Double,
    var m1: Double = 0.0,
    var m2: Double = 0.0
) {

    var a = 0.0
    var b = 0.0
    var c = 0.0
    var d = 0.0

    init {
        a = (m1 + m2 - 2.0 * (y2 - y1) / (x2 - x1)) / (x1 - x2).pow(2)
        b = (m2 - m1) / (2.0 * (x2 - x1)) - (3.0 / 2.0) * (x1 + x2) * a
        c = m1 - 3.0 * x1.pow(2) * a - 2.0 * x1 * b
        d = y1 - x1.pow(3) * a - x1.pow(2) * b - x1 * c
    }


    fun getY(x: Double): Double {
        return a * x.pow(3) + b * x.pow(2) + c * x + d
    }

    override fun toString(): String {
        return "${a}x^3 + ${b}x^2 + ${c}x + $d"
    }
}

// Other Constructors

fun CubicSpline(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    angle1: Angle,
    angle2: Angle
): CubicSpline {
    require(angle1 != 90.0.degrees && angle2 != 90.0.degrees)

    return CubicSpline(x1, y1, x2, y2, angle1.tan(), angle2.tan())
}

fun CubicSpline(
    point1: Vector2,
    point2: Vector2,
    slope1: Double = 0.0,
    slope2: Double = 0.0
): CubicSpline {
    return CubicSpline(point1.x, point1.y, point2.x, point2.y, slope1, slope2)
}

fun CubicSpline(
    point1: Vector2,
    point2: Vector2,
    angle1: Angle = 0.0.radians,
    angle2: Angle = 0.0.radians
): CubicSpline {
    require(angle1 != 90.0.degrees && angle2 != 90.0.degrees)

    return CubicSpline(point1.x, point1.y, point2.x, point2.y, angle1.tan(), angle2.tan())
}