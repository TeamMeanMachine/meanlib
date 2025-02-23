package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.networktables.*
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asRadians
import org.team2471.frc.lib.util.MeanLogger
import org.team2471.frc.lib.util.length
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

    private val isConnectedPub: BooleanPublisher = outputTable.getBooleanTopic("$name/Is Connected?").publish()
    private val posePub: StructPublisher<Pose2d> = outputTable.getStructTopic("$name/Pose", Pose2d.struct).publish()
    private val stdDevPub: DoublePublisher = outputTable.getDoubleTopic("$name/Standard Deviation").publish()
    private val avgDistPub: DoublePublisher = outputTable.getDoubleTopic("$name/Average Distance").publish()

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
//        if (!inputs.isConnected) {
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
//        } else {
//            println("$name already found, skipping reset")
//        }
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
                        val stdDev = photonStdDevCalculator.calculateStdDev(cameraResult.avgTagDistM, cameraResult.numTags)

                        stdDev.coerceIn(0.000001, 1000.0)

                        val estimatedPose = cameraResult.toGlobalPose(stdDev)

                        tempResults.add(cameraResult)
                        tempGlobalPoses.add(estimatedPose)
                    }
                }

            }

            latestResults = tempResults
            latestGlobalPoses = tempGlobalPoses

            isConnectedPub.set(inputs.isConnected)
            if (latestGlobalPoses.isNotEmpty()) {
                val latestPose = latestGlobalPoses.last()
                posePub.set(latestPose.pose)
                stdDevPub.set(latestPose.stdDev)
            } else {
                posePub.set(Pose2d())
                stdDevPub.set(0.0)
            }

            if (latestResults.isNotEmpty()) {
                avgDistPub.set(latestResults.last().avgTagDistM)
            } else {
//                avgDistPub.set(0.0)
            }

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
                } else {
                    MeanLogger.recordOutput("Cameras/$name/Raw Corners", *arrayOf<Translation2d>())
                }
            }
        } catch (_: Exception) {
        }
    }
}