package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.simulation.SimCameraProperties
import org.team2471.frc.lib.interpolation.SplineInterpolator
import org.team2471.frc.lib.math.DoubleRange

val fieldXRangeM = DoubleRange(0.0, 16.54)
val fieldYRangeM = DoubleRange(0.0, 8.21)

/**
 * A curve representing the standard deviation of PhotonVision pose results as a function of distance (in meters).
 * @author Thatcher Moore
 */
val photonStDevDistCurve: SplineInterpolator = SplineInterpolator(
    // dist in meters
    mutableMapOf(
        1.5 to 0.00075,
        1.9 to 0.00125,
        2.5 to 0.003,
        3.0 to 0.0065,
        3.5 to 0.0023,
        4.0 to 0.014,
        4.5 to 0.025, // artificially changed to trust distant tags left. Original: 0.0165
        5.0 to 0.03, // Original: 0.02
        6.0 to 0.04 // Original: 0.03
    )
)

val photonStdDevAreaCurve: SplineInterpolator = SplineInterpolator(
    mutableMapOf(
        0.0 to 0.0
    )
)

fun AprilTagFieldLayout.removeTags(tagIDs: IntArray): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}

fun AprilTagFieldLayout.removeTags(tagIDs: Collection<Int>): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}

fun Pose2d.isOnField(): Boolean {
    return fieldXRangeM.contains(this.x) && fieldYRangeM.contains(this.y)
}