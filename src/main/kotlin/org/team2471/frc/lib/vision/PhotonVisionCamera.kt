package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.StructPublisher
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians


class PhotonVisionCamera(
    private val inputTable: NetworkTable,
    outputTable: NetworkTable,
    val name: String,
    private val robotToCamera: Transform3d,
    private val aprilTagFieldLayout: AprilTagFieldLayout,
    private val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    private val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
) : CameraIO {

    var photonCam: PhotonCamera = PhotonCamera(name)

    private val cameraResultEntry: NetworkTableEntry = outputTable.getEntry("CameraResult $name")
    private val posePublisher: StructPublisher<Pose2d> =
        outputTable.getStructTopic("Pose $name", Pose2d.struct).publish()
    private val stdDevEntry: NetworkTableEntry = outputTable.getEntry("stdDev $name")
    private val isConnectedEntry: NetworkTableEntry = outputTable.getEntry("isConnected $name")

    private var poseEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        singleTagStrategy,
        robotToCamera
    )

    override var latestResults: MutableList<CameraResult> = mutableListOf()
    override var latestGlobalPoses: MutableList<GlobalPose> = mutableListOf()

    private var referencePose: Pose2d = Pose2d()

    override fun reset(inputs: CameraIO.CameraIOInputs) {
        if (!inputs.isConnected) {
            try {
                if (inputTable.containsSubTable(name)) {
                    poseEstimator = PhotonPoseEstimator(
                        aprilTagFieldLayout,
                        multiTagStrategy,
                        robotToCamera
                    )
                    poseEstimator.setMultiTagFallbackStrategy(singleTagStrategy)
                } else {
                    println("Cam $name not found")
                }
            } catch (ex: Exception) {
                println("Error resetting cam $name: $ex")
            }
        } else {
            println("$name already found, skipping reset")
        }
    }

    override fun update(
        inputs: CameraIO.CameraIOInputs,
        currentPos: Vector2L,
        currentHeading: Angle,
        headingRate: Angle
    ) {
        referencePose = currentPos.asMeters.toPose2d(currentHeading.asRadians)
        updateInputs(inputs)

        if (inputs.isConnected) {

            val tempResults: MutableList<CameraResult> = mutableListOf()
            val tempGlobalPoses: MutableList<GlobalPose> = mutableListOf()

            if (inputs.cameraResults.isNotEmpty()) {
                for (cameraResult in inputs.cameraResults) {
                    if (!cameraResult.isEmpty && cameraResult.numTags > 0 && cameraResult.pose.isOnField()) {
                        // TODO: Change this to reflect average tag area. needs testing on field
                        var stdDev = 0.01


                        if (cameraResult.numTags < 2) stdDev *= 3.0

                        stdDev.coerceIn(0.000001, 1000.0)

//                        cameraResultEntry.setCameraResult(cameraResult)
//                        CameraResult.recordOutput("$name/CameraResult", cameraResult)

                        val estimatedPose = cameraResult.toGlobalPose(stdDev)

                        posePublisher.set(estimatedPose.pose)
//                        MeanLogger.recordOutput("$name/pose", estimatedPose.pose)

                        stdDevEntry.setDouble(estimatedPose.stdDev)
//                        MeanLogger.recordOutput("$name/stdDev", estimatedPose.stdDev)

                        tempResults.add(cameraResult)
                        tempGlobalPoses.add(estimatedPose)
                    }
                }

            }

            latestResults = tempResults
            latestGlobalPoses = tempGlobalPoses

            if (latestGlobalPoses.isNotEmpty()) {
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
        try {
            inputs.isConnected = photonCam.isConnected
            if (inputs.isConnected) {
                poseEstimator.setReferencePose(referencePose)
                val unreadResults = photonCam.allUnreadResults.map { poseEstimator.update(it).get().toCameraResult() }
                inputs.cameraResults = unreadResults as ArrayList<CameraResult>
            }
        } catch (_: Exception) {
        }
    }
}