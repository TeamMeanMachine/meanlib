package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.*

//
class LimelightCamera(
    networkTable: NetworkTable,
    name: String,
    robotToCamera: Transform3d,
    ): Camera(networkTable, name), LimelightCameraIO {

    private val io = object: LimelightCameraIO {}
    private val inputs = LimelightCameraIO.LimelightCameraInputs()

    var latestMt2Result: LimelightHelpers.PoseEstimate? = null

    init {
        LimelightHelpers.setCameraPose_RobotSpace(name, robotToCamera.x, robotToCamera.y, robotToCamera.z, robotToCamera.rotation.x, robotToCamera.rotation.y, robotToCamera.rotation.z)
        GlobalScope.launch {
            periodic(0.02) {
                io.updateInputs(inputs)
                Logger.processInputs(name, inputs)

                latestMt2Result = inputs.mt2Result
            }
        }
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

        val mt2Result = latestMt2Result
//                                               i think this is radians               atm, everything else is in beta
        LimelightHelpers.SetRobotOrientation(name, currentHeading.asRadians, 0.0, 0.0, 0.0, 0.0, 0.0);

        if (mt2Result != null) {
            var estimatedPose = Vector2L(mt2Result.pose.x.meters, mt2Result.pose.x.meters)

            if (estimatedPose == Vector2L(
                    0.0.meters,
                    0.0.meters
                )
            ) return null // estimatedPose returns origin if no tags seen

//                                                   // degrees ?
            val globalPose = GlobalPose(estimatedPose, mt2Result.pose.rotation.asAngle, 0.1, mt2Result.timestampSeconds)

            advantagePoseEntry.setAdvantagePose(estimatedPose, mt2Result.pose.rotation.asAngle)

            return globalPose
        }

        return null
    }

    override fun updateInputs(inputs: LimelightCameraIO.LimelightCameraInputs) {
        inputs.mt2Result = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(name)
    }
}