package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Timer
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
    private val inputs = LimelightCameraIO.LimelightCameraInputs("cameras/Limelights$name")

    // Contains X,Y,Z, Roll,Pitch,Yaw, total latency (cl + tl), tag count, tag span, avg distance of tag from camera, average tag area (% of image)
    var latestMt2Result: DoubleArray = DoubleArray(11)

    init {
        LimelightHelpers.setCameraPose_RobotSpace(name, robotToCamera.x, robotToCamera.y, robotToCamera.z, robotToCamera.rotation.x, robotToCamera.rotation.y, robotToCamera.rotation.z)
    }

    // TODO(Unsure how limelight handles disconnects)
    override val     isConnected: Boolean = true
//        get() = limelightTable exists

    // TODO("Do we even need to reset?")
    override fun reset() {
        try {
            println("Implement reset")
        } catch (ex: Exception) {
            println("Error resetting cam $name: $ex")
        }
    }



// THIS NEEDS TO BE CALLED EVERY FRAME OR EVERYTHING WILL BREAK AAAAAA!!!!!!!!!!!!!!!
    override fun getEstimatedGlobalPose(
        currentPos: Vector2L,
        currentHeading: Angle,
        lookupPose: (Double) -> SwerveDrive.Pose?
    ): GlobalPose? {

        LimelightHelpers.SetRobotOrientation(name, currentHeading.asRadians, 0.0, 0.0, 0.0, 0.0, 0.0)

        io.updateInputs(inputs)

        Logger.processInputs(name, inputs)

        latestMt2Result = inputs.mt2Result

        val mt2Result = latestMt2Result

        var estimatedPose = Vector2L(mt2Result[0].meters, mt2Result[1].meters)

        if (estimatedPose == Vector2L(
                0.0.meters,
                0.0.meters
            )
        ) return null // estimatedPose returns origin if no tags seen

    //                                                   // degrees ?
        val globalPose = GlobalPose(estimatedPose, mt2Result[5].radians, 0.1, Timer.getFPGATimestamp() - mt2Result[6])

        advantagePoseEntry.setAdvantagePose(globalPose.latencyAdjustedPose(currentPos, lookupPose), mt2Result[5].radians)

        return globalPose

    }

    override fun updateInputs(inputs: LimelightCameraIO.LimelightCameraInputs) {
        inputs.mt2Result = networkTable.getEntry("botpose_orb_wpiblue").getDoubleArray(DoubleArray(11));
    }
}