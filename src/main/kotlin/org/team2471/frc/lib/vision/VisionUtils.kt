package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.simulation.SimCameraProperties
import org.team2471.frc.lib.interpolation.SplineInterpolator
import org.team2471.frc.lib.math.DoubleRange
import kotlin.math.ln
import kotlin.math.pow

//todo update
val fieldXRangeM = DoubleRange(0.0, 16.54)
val fieldYRangeM = DoubleRange(0.0, 8.21)

val photonStdDevCalculator: StdDevCalculator = StdDevCalculator(1.0, Pair(0.1, 0.001), Pair(2.5, 2.5))

fun AprilTagFieldLayout.removeTags(tagIDs: IntArray): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}

fun AprilTagFieldLayout.removeTags(tagIDs: Collection<Int>): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}

fun Pose2d.isOnField(): Boolean {
    return fieldXRangeM.contains(this.x) && fieldYRangeM.contains(this.y)
}


/**
 * Uses a curve and a multiplier to calculate standard deviation of a vision measurement.
 *
 * The formula used is m * f(d) / n
 * - m is a specified multiplier
 * - n is the number of tags seen
 * - f(d) is standard deviation as a function of distance (meters) and is of the form ad^b
 *
 * @param multiplier an arbitrary multiplier to scale stdDev calculation. Must be greater than 0
 * @param point1 An average distance in meters paired with a standard deviation. This point is meant to represent a close up, trusted result. Used to generate f(d). Both values must be less than the corresponding values in point2 and greater than 0.
 * @param point2 An average distance in meters paired with a standard deviation. This point is meant to represent a far away, untrustworthy result. Used to generate f(d). Both values must be greater than the corresponding values in point1 and greater than 0.
 *
 * @throws IllegalArgumentException if any of the arguments are outside of the specified bounds
 *
 * @author Thatcher Moore
 */
class StdDevCalculator(var multiplier: Double, point1: Pair<Double, Double>, point2: Pair<Double, Double>) {

    val b = ln(point1.second / point2.second) / ln(point1.first / point2.second)
    val a = point1.second / point1.first.pow(b)

    fun calculateStdDev(avgDistM: Double, numTagsSeen: Int) = multiplier * a * avgDistM.pow(b) / numTagsSeen

    init {
        require(multiplier > 0 && point1.first < point2.first && point1.second < point2.second && point1.first > 0.0 && point1.second > 0.0)
    }
}