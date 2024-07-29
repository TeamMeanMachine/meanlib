package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.photonvision.EstimatedRobotPose
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion.following.lookupPose
import org.team2471.frc.lib.motion.following.poseDiff
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians

data class GlobalPose (
    var pose: Vector2L,
    val rotation: Angle,
    val stDev: Double,
    val timestampSeconds: Double
) {
    val pose2d: Pose2d
        get() = Pose2d(pose.asMeters.toTranslation2d(), Rotation2d(this.rotation.asRadians))

    fun latencyAdjustedPose(currentPos: Vector2L, lookupPose: (Double) -> SwerveDrive.Pose?): Vector2L {
        return pose + currentPos - (lookupPose(timestampSeconds)?.position ?: currentPos.asFeet).feet
    }
}