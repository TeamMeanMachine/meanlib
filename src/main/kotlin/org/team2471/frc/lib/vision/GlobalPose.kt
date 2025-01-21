package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.networktables.StructPublisher

@JvmRecord
data class GlobalPose (
    val pose: Pose2d,
    val stdDev: Double,
    val timestampSeconds: Double,
    val tagNumber: Int
) {
    companion object {
        val EmptyGlobalPose = GlobalPose(Pose2d(), Double.POSITIVE_INFINITY, 0.0, 0)
    }
}

fun StructPublisher<Pose2d>.setGlobalPose(globalPose: GlobalPose) {
    this.set(globalPose.pose)
}