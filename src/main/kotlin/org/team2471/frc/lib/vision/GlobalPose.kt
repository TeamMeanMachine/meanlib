package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.networktables.StructPublisher
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians
import org.team2471.frc.lib.units.radians

@JvmRecord
data class GlobalPose (
    val pose: Pose2d,
    val stdDev: Double,
    val timestampSeconds: Double
) {
    companion object {
        val EmptyGlobalPose = GlobalPose(Pose2d(), Double.POSITIVE_INFINITY, 0.0)
    }
}

fun StructPublisher<Pose2d>.setGlobalPose(globalPose: GlobalPose) {
    this.set(globalPose.pose)
}