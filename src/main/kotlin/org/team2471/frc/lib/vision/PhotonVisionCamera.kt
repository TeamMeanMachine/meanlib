package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTagFieldLayout
import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.photonvision.targeting.PhotonPipelineResult
import org.photonvision.targeting.PhotonTrackedTarget
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*
import kotlin.math.abs
import kotlin.math.pow

////Not used
//class PhotonVisionCamera(
//    networkTable: NetworkTable,
//    name: String,
//    robotToCamera: Transform3d,
//    val aprilTagFieldLayout: AprilTagFieldLayout,
//    val singleTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.CLOSEST_TO_REFERENCE_POSE,
//    val multiTagStrategy: PhotonPoseEstimator.PoseStrategy = PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR
//): Camera(networkTable, name, robotToCamera) {
//
//    val pvTable: NetworkTable = NetworkTableInstance.getDefault().getTable("photonvision")
//    override val isConnected: Boolean
//        get() = photonCam.isConnected
//
//    override var photonCam: PhotonCamera = PhotonCamera(name)
//
//    var closestReferenceEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
//        aprilTagFieldLayout,
//        singleTagStrategy,
//        photonCam,
//        robotToCamera
//    )
//    var coProcessorEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
//        aprilTagFieldLayout,
//        multiTagStrategy,
//        photonCam,
//        robotToCamera
//    )
//    var filterMultiTagEstimator: PhotonPoseEstimator = PhotonPoseEstimator(
//        aprilTagFieldLayout,
//        singleTagStrategy,
//        photonCam,
//        robotToCamera
//    )
//
//    override fun reset() {
//        if (!photonCam.isConnected) {
//            try {
//                if (pvTable.containsSubTable(name)) {
//                    photonCam = PhotonCamera(name)
//                    coProcessorEstimator = PhotonPoseEstimator(
//                        aprilTagFieldLayout,
//                        multiTagStrategy,
//                        photonCam,
//                        robotToCamera
//                    )
//                    closestReferenceEstimator = PhotonPoseEstimator(
//                        aprilTagFieldLayout,
//                        singleTagStrategy,
//                        photonCam,
//                        robotToCamera
//                    )
//                } else {
//                    println("Cam $name not found")
//                }
//            } catch (ex: Exception) {
//                println("Error resetting cam $name: $ex")
//            }
//        } else  {
//            println("$name already found, skipping reset")
//        }
//    }
//
//    override fun getEstimatedGlobalPose(): GlobalPose? {
//        if (!photonCam.isConnected) {
//            return null
//        }
//
//        val targets = photonCam.latestResult.targets
//        val validTargets: ArrayList<PhotonTrackedTarget> = arrayListOf()
//
//        targets ?: return null
//
//        for (target in targets) {
//            if (target.fiducialId < 16 && target.poseAmbiguity < 0.5 && /*target.area > 0.1 &&*/ abs(target.bestCameraToTarget.z - 90.0) > 5.0)  {
//                validTargets.add(target)
//            }
//        }
////
////        val numTargets = validTargets.count()
////        val newPose = if (targets.size == validTargets.size) {
////
////            coProcessorEstimator.setReferencePose(
////                referencePose
////            )
////            coProcessorEstimator.update()
////        } else {
////            closestReferenceEstimator.setReferencePose(
////
////                referencePose
////            )
////            closestReferenceEstimator.update(PhotonPipelineResult(photonCam.latestResult.latencyMillis,validTargets))
////        }
////
////
////        if (newPose.isPresent) {
////
////            val estimatedPose = Vector2L(newPose.get().estimatedPose.x.meters, newPose.get().estimatedPose.y.meters)
////
////            var avgDist = 0.0.inches
////            var avgAmbiguity = 0.0
////            var avgArea = 0.0
////            var targetPoses : ArrayList<Vector2L> = arrayListOf()
////            for (target in validTargets) {
////                val tagPose = aprilTagFieldLayout.getTagPose(target.fiducialId).get()
////                avgDist += Vector2L(tagPose.x.meters, tagPose.y.meters).distance(estimatedPose)
////                avgAmbiguity += target.poseAmbiguity
////                avgArea += target.area
////                val targetRelativePose = (target.bestCameraToTarget + robotToCamera).translation.toTranslation2d().rotateBy(
////                    referencePose.rotation
////                )
////
////                lastGlobalPose?.pose?.plus(Vector2L(targetRelativePose.x.meters, targetRelativePose.y.meters))
////                    ?.let { targetPoses += it }
////                val robotPose = Pose3d(
////                    referencePose
////                )
////                //val targetRelativePose = (target.bestCameraToTarget.translation - robotToCamera.translation).toTranslation2d().rotateBy(Rotation2d(Drive.heading.asRadians + robotToCamera.rotation.angle))
////                val visionTargetPosition = robotPose.transformBy(robotToCamera).transformBy(target.bestCameraToTarget)
////                targetPoses.add(Vector2L(visionTargetPosition.x.meters, visionTargetPosition.y.meters))
////                //targetPoses.add(Drive.combinedPosition.plus(Vector2L(targetRelativePose.x.meters, targetRelativePose.y.meters)))
////            }
////            targetPoseEntry.setAdvantagePoses(targetPoses.toTypedArray())
////            avgDist /= validTargets.size.toDouble()
////            avgAmbiguity /= validTargets.size.toDouble()
////
////            if (avgDist > 6.0.meters) return null
////
////            val stDev = distStDevCurve?.getValue(avgDist.asMeters) ?: 0.05
////
////            var stDevMultiplier = (10000.0.pow(avgAmbiguity)) * (1.0 / 2.0 * avgArea)
////
////            if (numTargets < 2) stDevMultiplier *= 10.0
////
////            estimatedPose.coerceIn(Vector2L(0.0.inches, 0.0.inches) + Vector2L(16.0.inches, 16.0.inches), Vector2L(1654.0.cm, 821.0.cm) - Vector2L(16.0.inches, 16.0.inches))
////
////            advantagePoseEntry.setAdvantagePose(estimatedPose, referencePose.rotation.degrees.degrees)
////
////            lastGlobalPose = GlobalPose(estimatedPose, referencePose.rotation.degrees.degrees, stDev * stDevMultiplier, newPose.get().timestampSeconds)
////
////            stDevEntry.setDouble(stDev * stDevMultiplier)
////            stDevMultiplierEntry.setDouble(stDevMultiplier)
//
//            return lastGlobalPose
////        } else {
////            return null
////        }
//    }
//}