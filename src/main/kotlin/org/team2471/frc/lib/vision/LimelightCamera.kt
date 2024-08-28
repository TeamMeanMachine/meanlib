package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asMeters
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.math.toTranslation2d
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.*

//
// Note: the MT result array contains X,Y,Z, Roll,Pitch,Yaw, total latency (cl + tl), tag count, tag span, avg distance of tag from camera, average tag area (% of image)

class LimelightCamera(
    private val inputTable: NetworkTable,
    private val outputTable: NetworkTable,
    val name: String,
    robotToCamera: Transform3d,
    ): CameraIO {

    val advantagePoseEntry = outputTable.getEntry("April Advantage Pos $name")
    val stDevEntry = outputTable.getEntry("stDev $name")

    val lastPos: GlobalPose = GlobalPose.EmptyGlobalPose


    init {
        LimelightHelpers.setCameraPose_RobotSpace(
            name,
            robotToCamera.x,
            robotToCamera.y,
            robotToCamera.z,
            robotToCamera.rotation.x,
            robotToCamera.rotation.y,
            robotToCamera.rotation.z
        )
    }

    // TODO(Unsure how limelight handles disconnects)
    val isConnected: Boolean = true
//        get() = limelightTable exists6

    // TODO("Do we even need to reset?")
    override fun reset(inputs: CameraIO.CameraIOInputs) {}


//  Should be called every frame
    override fun getEstimatedGlobalPose(
    inputs: CameraIO.CameraIOInputs,
    currentPos: Vector2L,
    currentHeading: Angle,
    lookupPose: (Double) -> SwerveDrive.Pose?
    ): GlobalPose {

//        LimelightHelpers.SetRobotOrientation(name, currentHeading.asRadians, 0.0, 0.0, 0.0, 0.0, 0.0)

        val latestResult = inputs.cameraResult

        if (latestResult == CameraResult.EmptyCameraResult) {
            return GlobalPose.EmptyGlobalPose
        }
                                                  // needs to be dynamic
        val globalPose = latestResult.getGlobalPose(0.1)

        val latencyAdjustedPose = globalPose.latencyAdjustedPose(currentPos, lookupPose)

        advantagePoseEntry.setAdvantagePose(latencyAdjustedPose, currentHeading)
        Logger.recordOutput("$name/pose", Pose2d(latencyAdjustedPose.asMeters.toTranslation2d(), Rotation2d(currentHeading.asDegrees)))

        stDevEntry.setDouble(globalPose.stDev)
        Logger.recordOutput("$name/stDev", globalPose.stDev)


        return globalPose

    }

    override fun updateInputs(inputs: CameraIO.CameraIOInputs) {
        inputs.isConnected = true
        inputs.cameraResult = CameraResult.fromLLTable(inputTable)
    }
}