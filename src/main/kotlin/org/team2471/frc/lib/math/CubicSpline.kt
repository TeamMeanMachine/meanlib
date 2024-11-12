package org.team2471.frc.lib.math

import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.radians
import kotlin.math.pow

// math can be found here if you want to bore yourself:
// https://math.stackexchange.com/questions/1522439/generalised-formula-for-fitting-a-cubic-between-two-points-with-specified-slopes
// I derived the rest of it.

/**
 * Creates a cubic spline from two points and two slopes.
 *
 * @author Thatcher Moore
 *
 * @param x1 The x coordinate of the first point
 * @param y1 The y coordinate of the first point
 * @param x2 The x coordinate of the second point
 * @param y2 The y coordinate of the second point
 * @param m1 The slope at the first point. If null, will automatically find the best slope
 * @param m2 The slope at the second point. If null, will automatically fund the best slope
 */
class CubicSpline(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    m1: Double? = 0.0,
    m2: Double? = 0.0
) {

    // Initializes a, b, c, and d
    var a = 0.0
    var b = 0.0
    var c = 0.0
    var d = 0.0

    init {
        // Does null checks on m1 and m2 and calculates a, b, c, and d

        // If both slopes are null just calculate a line
        if (m1 == null && m2 == null) {
            a = 0.0
            b = 0.0
            c = (y2 - y1) / (x2 - x1)
            d = y1 - c * x1
        // Otherwise automatically calculate slopes or just pass in given slopes
        } else {
            val startSlope: Double = if (m1 == null && m2 != null) (2.0 * (y2 - y1) / (x2 - x1)) - m2 else m1!!
            val endSlope: Double = if (m1 != null && m2 == null) (2.0 * (y2 - y1) / (x2 - x1)) - m1 else m2!!
            a = (startSlope + endSlope - 2.0 * (y2 - y1) / (x2 - x1)) / (x1 - x2).pow(2)
            b = (endSlope - startSlope) / (2.0 * (x2 - x1)) - (3.0 / 2.0) * (x1 + x2) * a
            c = startSlope - 3.0 * x1.pow(2) * a - 2.0 * x1 * b
            d = y1 - x1.pow(3) * a - x1.pow(2) * b - x1 * c
        }

    }


    /**
     * Gets the y coordinate of the spline at the given x coordinate.
     *
     * @author Thatcher Moore
     *
     * @param x the x coordinate
     * @return The y coordinate of the spline at the given x coordinate.
     */
    fun getY(x: Double): Double {
        return a * x.pow(3) + b * x.pow(2) + c * x + d
    }

    /**
     * Turns the spline into a string. Mainly for debugging
     *
     * @author Thatcher Moore
     *
     * @return A string containing the equation of the spline in standard form.
     */
    override fun toString(): String {
        return "${a}x^3 + ${b}x^2 + ${c}x + $d"
    }
}

// Other Constructors for things such as Vectors and Angles

/**
 * Creates a cubic spline from two points and two slopes.
 *
 * @author Thatcher Moore
 *
 * @param x1 The x coordinate of the first point
 * @param y1 The y coordinate of the first point
 * @param x2 The x coordinate of the second point
 * @param y2 The y coordinate of the second point
 * @param angle1 The angle at the first point. If null, will automatically find the best slope
 * @param angle2 The angle at the second point. If null, will automatically fund the best slope
 *
 * @return The calculated CubicSpline
 */
fun CubicSpline(
    x1: Double,
    y1: Double,
    x2: Double,
    y2: Double,
    angle1: Angle? = 0.0.radians,
    angle2: Angle? = 0.0.radians
): CubicSpline {
    require(angle1 != 90.0.degrees && angle2 != 90.0.degrees)

        return CubicSpline(x1, y1, x2, y2, angle1?.tan(), angle2?.tan())
}

/**
 * Creates a cubic spline from two points and two slopes.
 *
 * @author Thatcher Moore
 *
 * @param point1 The first point
 * @param point2 The second point
 * @param slope1 The slope at the first point. If null, will automatically find the best slope
 * @param slope2 The slope at the second point. If null, will automatically fund the best slope
 *
 * @return The calculated CubicSpline
 */
fun CubicSpline(
    point1: Vector2,
    point2: Vector2,
    slope1: Double? = 0.0,
    slope2: Double? = 0.0
): CubicSpline {
    return CubicSpline(point1.x, point1.y, point2.x, point2.y, slope1, slope2)
}

/**
 * Creates a cubic spline from two points and two slopes.
 *
 * @author Thatcher Moore
 *
 * @param point1 The first point
 * @param point2 The second point
 * @param angle1 The angle at the first point. If null, will automatically find the best slope
 * @param angle2 The angle at the second point. If null, will automatically fund the best slope
 *
 * @return The calculated CubicSpline
 */
fun CubicSpline(
    point1: Vector2,
    point2: Vector2,
    angle1: Angle? = 0.0.radians,
    angle2: Angle? = 0.0.radians
): CubicSpline {
    require(angle1 != 90.0.degrees && angle2 != 90.0.degrees)

    return CubicSpline(point1.x, point1.y, point2.x, point2.y, angle1?.tan(), angle2?.tan())
}