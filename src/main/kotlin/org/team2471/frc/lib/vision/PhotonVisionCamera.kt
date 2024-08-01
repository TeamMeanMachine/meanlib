package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.filter.LinearFilter
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.Logger
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.targeting.PhotonPipelineResult
import org.photonvision.targeting.PhotonTrackedTarget
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*
import kotlin.math.abs
import kotlin.math.pow

class PhotonVisionCamera(
    networkTable: NetworkTable,
    name: String,
    val robotToCamera: Transform3d,
    val aprilTagFieldLayout: AprilTagFieldLayout,
    val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
    val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR
): Camera(networkTable, name, true), PhotonVisionCameraIO {

    private val io = object: PhotonVisionCameraIO {}
    private val inputs = PhotonVisionCameraIO.PhotonVisionCameraInputs("cameras/Limelights$name")

    val pvTable: NetworkTable = NetworkTableInstance.getDefault().getTable("photonvision")
    override val isConnected: Boolean = false

    override var photonCam: PhotonCamera = PhotonCamera(name)

    var camLatency = LinearFilter.movingAverage(5)

    private var lastPose: GlobalPose = GlobalPose(Vector2L(0.0.inches, 0.0.inches), 0.0.degrees, 0.0, 0.0)

    var poseEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
        aprilTagFieldLayout,
        singleTagStrategy,
        photonCam,
        robotToCamera
    )

    val validTagIDs: List<Int> = aprilTagFieldLayout.tags.map { it.ID }

    val photonDistCurve = MotionCurve()

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

    fun setInitialPose(pose: GlobalPose) {
        if (lastPose.timestampSeconds == 0.0) {
            lastPose = pose
        }
    }

    override fun reset() {
        if (!photonCam.isConnected) {
            try {
                if (pvTable.containsSubTable(name)) {
                    photonCam = PhotonCamera(name)
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

// THIS NEEDS TO BE CALLED EVERY FRAME OR EVERYTHING WILL BREAK AAAAAA!!!!!!!!!!!!!!!
    override fun getEstimatedGlobalPose(currentPos: Vector2L, currentHeading: Angle, lookupPose: (Double) -> SwerveDrive.Pose?): GlobalPose? {
        io.updateInputs(inputs)
        Logger.processInputs(name, inputs)
        if (!photonCam.isConnected) {
            return null
        }

        val targets = inputs.cameraResult.targets
        val validTargets: ArrayList<PhotonTrackedTarget> = arrayListOf()

        targets ?: return null

        for (target in targets) {
            if (target.fiducialId in validTagIDs && target.poseAmbiguity < 0.5 && /*target.area > 0.1 &&*/ abs(target.bestCameraToTarget.z - 90.0) > 5.0)  {
                validTargets.add(target)
            }
        }

        val numTargets = validTargets.size

        poseEstimator.setReferencePose(lastPose.pose2d)

        val newPose = poseEstimator.update()

        if (newPose.isPresent && numTargets > 0) {
            var estimatedPose = Vector2L(newPose.get().estimatedPose.x.meters, newPose.get().estimatedPose.y.meters)

            var avgDist = 0.0.inches
            var avgAmbiguity = 0.0
            var avgArea = 0.0
            var targetPoses : ArrayList<Vector2L> = arrayListOf()
            val currLatency = Timer.getFPGATimestamp() - newPose.get().timestampSeconds
            val avgLatency = camLatency.calculate(currLatency)
            //println("$name latency ${round(currLatency, 4)} avg: ${round(avgLatency, 4)}")
            for (target in validTargets) {
                val tagPose = aprilTagFieldLayout.getTagPose(target.fiducialId).get()
                avgDist += Vector2L(tagPose.x.meters, tagPose.y.meters).distance(estimatedPose)
                avgAmbiguity += target.poseAmbiguity
                avgArea += target.area

                val robotPose = Pose3d(
                    Translation3d(currentPos.x.asMeters, currentPos.y.asMeters, 0.0),
                    Rotation3d(0.0, 0.0, currentHeading.asRadians)
                )

                val visionTargetPosition = robotPose.transformBy(robotToCamera).transformBy(target.bestCameraToTarget)

                targetPoses.add(Vector2L(visionTargetPosition.x.meters, visionTargetPosition.y.meters))
            }

            targetPoseEntry.setAdvantagePoses(targetPoses.toTypedArray())
            if (validTargets.size.toDouble() > 0.0) {
                avgDist /= validTargets.size.toDouble()
                avgAmbiguity /= validTargets.size.toDouble()
            }

            if (avgDist > 6.0.meters) return null

            var stDev = photonDistCurve.getValue(avgDist.asMeters)



            stDev *= 5.0 * avgArea


            if (numTargets < 2) stDev *= 3.0

            stDev.coerceIn(0.000001, 1000.0)

            lastPose = GlobalPose(estimatedPose, newPose.get().estimatedPose.rotation.angle.radians, stDev, Timer.getFPGATimestamp())

            lastPose.pose.coerceIn(Vector2L(0.0.inches, 0.0.inches), Vector2L(1654.0.cm, 821.0.cm))

            advantagePoseEntry.setAdvantagePose(lastPose.latencyAdjustedPose(currentPos, lookupPose), newPose.get().estimatedPose.rotation.angle.radians)


            stDevEntry.setDouble(stDev)

            return lastPose
        } else {
            return null
        }
    }

    override fun updateInputs(inputs: PhotonVisionCameraIO.PhotonVisionCameraInputs) {
        inputs.cameraResult = photonCam.latestResult
        inputs.isConnected = photonCam.isConnected
    }
}