package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*

//
class LimelightCamera(
    networkTable: NetworkTable,
    name: String,
    robotToCamera: Transform3d,
    ): Camera(networkTable, name) {

    init {
        LimelightHelpers.setCameraPose_RobotSpace(name, robotToCamera.x, robotToCamera.y, robotToCamera.z, robotToCamera.rotation.x, robotToCamera.rotation.y, robotToCamera.rotation.z)
    }

    // TODO(Unsure how limelight handles disconnects)
    override val isConnected: Boolean = true
//        get() = limelightTable exists

    // TODO("Do we even need to reset?")
    override fun reset() {
        try {
            println("Implement reset")
        } catch (ex: Exception) {
            println("Error resetting cam $name: $ex")
        }
    }


    override fun getEstimatedGlobalPose(
        currentPos: Vector2L,
        currentHeading: Angle,
        lookupPose: (Double) -> SwerveDrive.Pose?
    ): GlobalPose? {
//                                               i think this is radians               atm, everything else is in beta
        LimelightHelpers.SetRobotOrientation(name, currentHeading.asRadians, 0.0, 0.0, 0.0, 0.0, 0.0);

        val mt2 = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(name)
        if (mt2 != null) {
            var estimatedPose = Vector2L(mt2.pose.x.meters, mt2.pose.x.meters)

            if (estimatedPose == Vector2L(
                    0.0.meters,
                    0.0.meters
                )
            ) return null // estimatedPose returns origin if no tags seen

//                                                   // degrees ?
            val globalPose = GlobalPose(estimatedPose, mt2.pose.rotation.asAngle, 0.1, mt2.timestampSeconds)

            advantagePoseEntry.setAdvantagePose(estimatedPose, mt2.pose.rotation.asAngle)

            return globalPose
        }

        return null
    }
}