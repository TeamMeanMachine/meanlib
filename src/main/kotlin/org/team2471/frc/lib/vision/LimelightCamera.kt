package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.StructPublisher
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.vision.CameraIO.CameraIOInputs

// Note: the MT result array contains X,Y,Z, Roll,Pitch,Yaw, total latency (cl + tl), tag count, tag span, avg distance of tag from camera, average tag area (% of image)

class LimelightCamera(
    private val inputTable: NetworkTable,
    private val outputTable: NetworkTable,
    val name: String,
    robotToCamera: Transform3d,
) : CameraIO {

    private val isConnectedEntry: NetworkTableEntry = outputTable.getEntry("isConnected $name")
    private val posePublisher: StructPublisher<Pose2d> =
        outputTable.getStructTopic("Pose $name", Pose2d.struct).publish()
    private val stdDevEntry: NetworkTableEntry = outputTable.getEntry("stdDev $name")

    override var latestResults: MutableList<CameraResult> = mutableListOf()
    override var latestGlobalPoses: MutableList<GlobalPose> = mutableListOf()

    init {
        LimelightHelpers.setCameraPose_RobotSpace(
            name,
            -robotToCamera.x,
            robotToCamera.y,
            robotToCamera.z,
            robotToCamera.rotation.x,
            robotToCamera.rotation.y,
            robotToCamera.rotation.z
        )
    }

    // TODO(Unsure how limelight handles disconnects)
    val isConnected: Boolean
        get() = inputTable.containsSubTable("TODO")

    // TODO("Do we even need to reset?")
    override fun reset(inputs: CameraIO.CameraIOInputs) {}


    override fun update(inputs: CameraIOInputs, currentPose: Pose2d, headingRatePerSecond: Rotation2d) {
        LimelightHelpers.SetRobotOrientation(
            name,
            currentPose.rotation.degrees,
            headingRatePerSecond.degrees,
            0.0,
            0.0,
            0.0,
            0.0
        )

        updateInputs(inputs)

        if (headingRatePerSecond.degrees <= 45.0 && inputs.isConnected) {

            val latestResult = inputs.cameraResults.last()

            // copied from photon with small changes
            var stdDev = 0.1


            stdDev *= 0.25 / latestResult.avgTagArea
            //            println("area: ${cameraResult.avgTagArea}")


            if (latestResult.numTags < 2) stdDev *= 3.0

//         with the way our weighted average sensor fusion algorithm works it doesn't like large/small numbers
            stdDev.coerceIn(0.000001, 1000.0)

            val globalPose = latestResult.toGlobalPose(stdDev)

            latestResults = inputs.cameraResults
            latestGlobalPoses = arrayListOf(globalPose)


            if (inputs.isConnected) {
                posePublisher.setGlobalPose(latestGlobalPoses.last())
                stdDevEntry.setDouble(latestGlobalPoses.last().stdDev)
            } else {
                posePublisher.setEmptyPose()
                stdDevEntry.setDouble(0.0)
            }

            isConnectedEntry.setBoolean(inputs.isConnected)

        }
    }

    override fun updateInputs(inputs: CameraIO.CameraIOInputs) {
        inputs.isConnected = true
        val poseEstimate = LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(name)
        inputs.cameraResults = arrayListOf(poseEstimate.toCameraResult())
    }
}