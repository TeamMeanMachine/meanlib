package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.StructArrayPublisher
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.targeting.PhotonPipelineResult
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.*


class PhotonVisionCamera(
    private val inputTable: NetworkTable,
    private val outputTable: NetworkTable,
    val name: String,
    private val robotToCamera: Transform3d,
    private val aprilTagFieldLayout: AprilTagFieldLayout,
    private val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    private val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
): CameraIO {

    var photonCam: PhotonCamera = PhotonCamera(name)

    private val cameraResultEntry: NetworkTableEntry = outputTable.getEntry("CameraResult $name")
    private val advantagePoseEntry: NetworkTableEntry = outputTable.getEntry("April Advantage Pos $name")
    private val stDevEntry: NetworkTableEntry = outputTable.getEntry("stDev $name")
    private val isConnectedEntry: NetworkTableEntry = outputTable.getEntry("isConnected $name")

    private val testTrajPublisher: StructArrayPublisher<Pose2d> = outputTable.getStructArrayTopic("testTrajectory $name", Pose2d.struct).publish()

    private var lastPose: GlobalPose = GlobalPose.EmptyGlobalPose
    private var lastLatency = 0.0

    private var poseEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        singleTagStrategy,
        photonCam,
        robotToCamera
    )



    override fun reset(inputs: CameraIO.CameraIOInputs) {
        if (!inputs.isConnected) {
            try {
                if (inputTable.containsSubTable(name)) {
                    poseEstimator = PhotonPoseEstimator(
                        aprilTagFieldLayout,
                        multiTagStrategy,
                        photonCam,
                        robotToCamera
                    )
                    poseEstimator.setMultiTagFallbackStrategy(singleTagStrategy)
                } else {
                    println("Cam $name not found")
                }
            } catch (ex: Exception) {
                println("Error resetting cam $name: $ex")
            }
        } else  {
            println("$name already found, skipping reset")
        }
    }

    // This should be called every frame. Things may go wrong otherwise.
    override fun getEstimatedGlobalPose(
        inputs: CameraIO.CameraIOInputs,
        currentPos: Vector2L,
        currentHeading: Angle,
        headingRate: Angle,
        lookupPose: (Double) -> SwerveDrive.Pose?
    ): GlobalPose {
        isConnectedEntry.setBoolean(inputs.isConnected)
        advantagePoseEntry.setEmptyPose()
//        advantagePoseEntry.unpublish()
        stDevEntry.setDouble(0.0)
        if (!inputs.isConnected) {
//            println("Isn't connected")
            return GlobalPose.EmptyGlobalPose
        }

        val cameraResult = inputs.cameraResult

//        if (name == "CamSR") {
//            println("hazStuff: ${!cameraResult.isEmpty} numTags: ${cameraResult.numTags} time: ${cameraResult.latencyMs != lastLatency}")
//        }


        if (!cameraResult.isEmpty && cameraResult.numTags > 0 && cameraResult.latencyMs != lastLatency) {

            if (cameraResult.avgDist > 6.0.meters) {/*println("too far");*/ return GlobalPose.EmptyGlobalPose}

            var stDev = photonStDevDistCurve.getY(cameraResult.avgDist.asMeters) * 5.0
//            println("original stdev: $stDev")



            stDev *= 0.25 / cameraResult.avgTagArea
//            println("area: ${cameraResult.avgTagArea}")


            if (cameraResult.numTags < 2) stDev *= 3.0

//         with the way our weighted average sensor fusion algorithm works it doesn't like large/small numbers
            stDev.coerceIn(0.000001, 1000.0)


            cameraResultEntry.setCameraResult(cameraResult)
            CameraResult.recordOutput("$name/CameraResult", cameraResult)

            val estimatedPose = cameraResult.getGlobalPose(stDev)

            // make sure its on the field
            estimatedPose.pos.coerceIn(Vector2L.Zeros, Vector2L(1654.0.cm, 821.0.cm))

            val latencyAdjustedPose = estimatedPose.latencyAdjustedPose(currentPos, lookupPose)

            advantagePoseEntry.setAdvantagePose(latencyAdjustedPose, estimatedPose.rotation)
            Logger.recordOutput("$name/pose", Pose2d(latencyAdjustedPose.asMeters.toTranslation2d(), Rotation2d(currentHeading.asDegrees)))

            stDevEntry.setDouble(estimatedPose.stdDev)
            Logger.recordOutput("$name/stDev", estimatedPose.stdDev)


            lastPose = estimatedPose
            lastLatency = cameraResult.latencyMs

            return estimatedPose
        } else {
//            println("Empty :(")
            return GlobalPose.EmptyGlobalPose
        }
    }

    override fun get2DTarget(tagID: Int): Target2D {
        lateinit var latestResult: PhotonPipelineResult
        try {
            latestResult = photonCam.latestResult
        } catch (e: Exception) {
            println("Error with latest result")
            return Target2D.EmptyTarget2D
        }
        val validTargets = latestResult.targets.filter { it.fiducialId == tagID }
        if (validTargets.isNotEmpty()) {
            return validTargets[0].toTarget2D()
        } else {
            return Target2D.EmptyTarget2D
        }
    }

    override fun updateInputs(inputs: CameraIO.CameraIOInputs) {
        try {
            inputs.isConnected = photonCam.isConnected
            if (inputs.isConnected) {
                poseEstimator.setReferencePose(lastPose.pose2d)
                inputs.cameraResult = poseEstimator.update().get().toCameraResult()
            }
        } catch (_: Exception) {
        }
    }
}