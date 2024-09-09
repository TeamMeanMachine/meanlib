package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode

class PhotonVisionCamera(
    private val inputTable: NetworkTable,
    private val outputTable: NetworkTable,
    val name: String,
    private val robotToCamera: Transform3d,
    private val aprilTagFieldLayout: AprilTagFieldLayout,
    private val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    private val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR,
): CameraIO {

    private var photonCam: PhotonCamera = PhotonCamera(name)

    private val cameraResultEntry: NetworkTableEntry = outputTable.getEntry("CameraResult $name")
    private val advantagePoseEntry: NetworkTableEntry = outputTable.getEntry("April Advantage Pos $name")
    private val stDevEntry: NetworkTableEntry = outputTable.getEntry("stDev $name")
    private val isConnectedEntry: NetworkTableEntry = outputTable.getEntry("isConnected $name")

    private var lastPose: GlobalPose = GlobalPose.EmptyGlobalPose

    private var poseEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        singleTagStrategy,
        photonCam,
        robotToCamera
    )

    private val photonDistCurve = MotionCurve()

    init {
        photonDistCurve.setMarkBeginOrEndKeysToZeroSlope(false)

        // dist in meters
        photonDistCurve.storeValue(1.5, 0.00075)
        photonDistCurve.storeValue(1.9, 0.00125)
        photonDistCurve.storeValue(2.5, 0.003)
        photonDistCurve.storeValue(3.0, 0.0065)
        photonDistCurve.storeValue(3.5, 0.0023)
        photonDistCurve.storeValue(4.0, 0.014)
        photonDistCurve.storeValue(4.5, 0.025) //photonvision says not to trust after 15 feet  old: 0.0165
        photonDistCurve.storeValue(5.0, 0.03) // 0.02
        photonDistCurve.storeValue(6.0, 0.04) // 0.03
    }



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


        if (!cameraResult.isEmpty && cameraResult.numTags > 0) {

            if (cameraResult.avgDist > 6.0.meters) {/*println("too far");*/ return GlobalPose.EmptyGlobalPose}

            var stDev = photonDistCurve.getValue(cameraResult.avgDist.asMeters) / 5
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
            estimatedPose.pose.coerceIn(Vector2L.Zeros, Vector2L(1654.0.cm, 821.0.cm))

            val latencyAdjustedPose = estimatedPose.latencyAdjustedPose(currentPos, lookupPose)

            advantagePoseEntry.setAdvantagePose(latencyAdjustedPose, estimatedPose.rotation)
            Logger.recordOutput("$name/pose", Pose2d(latencyAdjustedPose.asMeters.toTranslation2d(), Rotation2d(currentHeading.asDegrees)))

            stDevEntry.setDouble(estimatedPose.stDev)
            Logger.recordOutput("$name/stDev", estimatedPose.stDev)


            lastPose = estimatedPose

            return estimatedPose
        } else {
//            println("Empty :(")
            return GlobalPose.EmptyGlobalPose
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