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
 * A curve representing the standard deviation of PhotonVision pose results as a function of average tag area (in percent).
 * @author Thatcher Moore
 */
val photonStDevAreaCurve: SplineInterpolator = SplineInterpolator(
    // dist in meters
    mutableMapOf(
        1.69212 to 0.0000648,
        0.89687 to 0.0000722,
        0.59865 to 0.0002967,
        0.15958 to 4.8117529,
        0.11406 to 0.0007479,
        0.11092 to 0.0026701
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