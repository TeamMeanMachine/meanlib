package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians
import org.team2471.frc.lib.units.radians

data class GlobalPose (
    var pos: Vector2L,
    val rotation: Angle,
    val stDev: Double,
    val timestampSeconds: Double
) {
    val pose2d: Pose2d
        get() = Pose2d(pos.asMeters.toTranslation2d(), Rotation2d(this.rotation.asRadians))

    fun latencyAdjustedPose(currentDrivePos: Vector2L, lookupPose: (Double) -> SwerveDrive.Pose?): Vector2L {
        return pos + currentDrivePos - (lookupPose(timestampSeconds)?.position ?: currentDrivePos.asFeet).feet
    }

    companion object {
        val EmptyGlobalPose = GlobalPose(Vector2L.Zeros, 0.0.radians, Double.POSITIVE_INFINITY, 0.0)
    }
}