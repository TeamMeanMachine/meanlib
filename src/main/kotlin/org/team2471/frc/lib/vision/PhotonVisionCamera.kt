package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.StructPublisher
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians
import org.team2471.frc.lib.util.MeanLogger
import org.team2471.frc.lib.vision.CameraIO.CameraIOInputs
import kotlin.math.pow


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

    private val isConnectedEntry: NetworkTableEntry = outputTable.getEntry("isConnected $name")

    private var poseEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        multiTagStrategy,
        robotToCamera
    ).apply {
        setMultiTagFallbackStrategy(singleTagStrategy)
    }

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
        inputs: CameraIOInputs, currentPose: Pose2d, headingRatePerSecond: Rotation2d
    ) {
        referencePose = currentPose
        updateInputs(inputs)

        if (inputs.isConnected) {

            val tempResults: MutableList<CameraResult> = mutableListOf()
            val tempGlobalPoses: MutableList<GlobalPose> = mutableListOf()

            if (inputs.cameraResults.isNotEmpty()) {
                for (cameraResult in inputs.cameraResults) {
                    if (!cameraResult.isEmpty && cameraResult.numTags > 0 && cameraResult.pose.isOnField()) {
                        val stdDev = if (cameraResult.numTags == 1) {
                            0.190319 * cameraResult.avgTagArea.pow(-1.16074)
                        } else {
                            0.0108089 * cameraResult.avgTagArea.pow(-0.996019)
                        }

                        stdDev.coerceIn(0.000001, 1000.0)

                        CameraResult.recordOutput("Cameras/$name/Camera Result", cameraResult)

                        val estimatedPose = cameraResult.toGlobalPose(stdDev)

                        tempResults.add(cameraResult)
                        tempGlobalPoses.add(estimatedPose)
                    }
                }

            }

            latestResults = tempResults
            latestGlobalPoses = tempGlobalPoses

            isConnectedEntry.setBoolean(inputs.isConnected)
        }
    }

    override fun updateInputs(inputs: CameraIO.CameraIOInputs) {
        try {
            inputs.isConnected = photonCam.isConnected
            if (inputs.isConnected) {
                poseEstimator.setReferencePose(referencePose)
                val unreadResults = photonCam.allUnreadResults.map { poseEstimator.update(it).get() }
                inputs.cameraResults = unreadResults.map {it.toCameraResult()} as ArrayList<CameraResult>
                if (unreadResults.isNotEmpty()) {
                    MeanLogger.recordOutput("Cameras/$name/Raw Corners", *unreadResults.last().targetsUsed.map { it.getDetectedCorners().map { Translation2d(it.x, it.y) } }.flatten().toTypedArray())
                }
            }
        } catch (_: Exception) {
        }
    }
}