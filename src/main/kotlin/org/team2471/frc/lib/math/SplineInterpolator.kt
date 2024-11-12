package org.team2471.frc.lib.interpolation

import org.team2471.frc.lib.math.CubicSpline

/**
 * An interpolator made as an alternative to MotionCurves. This takes points in the form (x, y) and generates a piecewise function made up of cubic splines that hits all of those points. Made for things that are directly functions of each other, like a standard deviation as a function of distance or shooter angle as a function of distance from goal.
 *
 * @author Thatcher Moore
 *
 * @param points a map containing the points to interpolate from, arranged as <x, y>
 * @param startSlope The desired slope at the first point. If null, will calculate the optimal slope
 * @param endSlope The desired slope at the last point. If null, will calculate the optimal slope
 */
class SplineInterpolator(
    points: MutableMap<Double, Double> = emptyMap<Double, Double>().toMutableMap(),
    startSlope: Double? = null,
    endSlope: Double? = null
) {
    var points: MutableMap<Double, Double> = points.toSortedMap()
        set(value)
        {
            field = value.toSortedMap()

            keys = field.keys

            this.updateSpline()
        }

    var startSlope: Double? = startSlope
        set(value)
        {
            field = value

            this.updateSpline()
        }

    var endSlope: Double? = endSlope
        set(value)
        {
            field = value

            this.updateSpline()
        }

    var keys = points.keys

    var minX = keys.min()

    var maxX = keys.max()

    private var splines: ArrayList<CubicSpline> = arrayListOf()

    private var ranges: ArrayList<Pair<Double, Double>> = arrayListOf()

    init
    {
        this.points = points

        updateSpline()
    }

    override fun toString(): String {
        return splines.toString()
    }

    fun addPoint(x: Double, y: Double) {
        points[x] = y
        points = points
    }

    fun updateSpline()
    {

        minX = keys.min()

        maxX = keys.max()

        splines.clear()

        var point1Slope: Double?
        var point2Slope: Double?

        for (pointIndex in keys.indices)
        {

            val point1X = keys.elementAt(pointIndex)

            if (point1X == maxX) {
                break
            }

            val point1Y = points[point1X] ?: 0.0
            val point2X = keys.elementAt(pointIndex + 1)
            val point2Y = points[point2X] ?: 0.0

            point1Slope = when (point1X)
            {
                minX -> startSlope
                maxX -> endSlope
                else -> {
                    val prevX = keys.elementAt(pointIndex - 1)
                    (point2Y - (points[prevX] ?: 0.0)) / (point2X - prevX)
                    }
            }

            point2Slope = when (point2X)
            {
                minX -> startSlope
                maxX -> endSlope
                else -> {
                    val nextX = keys.elementAt(pointIndex + 2)
                    (point1Y - (points[nextX] ?: 0.0)) / (point1X - nextX)
                }
            }

            splines.add(CubicSpline(point1X, point1Y, point2X, point2Y, point1Slope, point2Slope))
            ranges.add(Pair(point1X, point2X))
        }
    }

    fun getY(x: Double): Double {
        var splineNum = 0
        for (range in ranges) {
            if (range.first <= x && x <= range.second)
            {
                break
            }
            splineNum++
        }
        return splines[splineNum].getY(x)
    }
}