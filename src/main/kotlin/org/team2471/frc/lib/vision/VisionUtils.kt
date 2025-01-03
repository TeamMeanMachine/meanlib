package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.simulation.SimCameraProperties
import org.team2471.frc.lib.interpolation.SplineInterpolator

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

/**
 * The properties of the physical camera on the robot. Since we usually use one type, multiple variables aren't needed
 * @author Thatcher Moore
 */
val cameraProperties = SimCameraProperties().apply {
    setCalibration(1280, 720, Rotation2d.fromDegrees(90.0))
    setCalibError(0.5, 0.8) // Values from docs. Should change
    fps = 20.0 // Complete guess
    avgLatencyMs = 30.0 // complete guess
    latencyStdDevMs = 5.0 // another total guess
}

fun AprilTagFieldLayout.removeTags(tagIDs: IntArray): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}

fun AprilTagFieldLayout.removeTags(tagIDs: Collection<Int>): AprilTagFieldLayout {
    return AprilTagFieldLayout(this.tags.filter { it.ID !in tagIDs }, this.fieldLength, this.fieldWidth)
}