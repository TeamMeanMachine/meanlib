package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import org.photonvision.EstimatedRobotPose
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.length

@JvmRecord
data class CameraResult(
    val pose: Pose2d,
    // Seconds
    val timeStampSeconds: Double,
    val numTags: Int,
    // In % of image
    val avgTagDistM: Double,
    val cameraType: CameraType,
    val isEmpty: Boolean = false
) {

    fun toGlobalPose(stdDev: Double): GlobalPose {
        return if (isEmpty) {
//            println("hello error...")
            GlobalPose.EmptyGlobalPose
        } else {
            GlobalPose(
                this.pose,
                stdDev,
                this.timeStampSeconds,
                this.numTags
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CameraResult

        if (timeStampSeconds != other.timeStampSeconds) return false
        if (avgTagDistM != other.avgTagDistM) return false
        if (isEmpty != other.isEmpty) return false
        if (pose != other.pose) return false
        if (numTags != other.numTags) return false
        if (cameraType != other.cameraType) return false

        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    companion object {
        val EmptyCameraResult = CameraResult(Pose2d(), 0.0, 0, 0.0, CameraType.LIMELIGHT, true)

        fun recordOutput(key: String, value: CameraResult) {
            MeanLogger.recordOutput("$key/Pose", value.pose)
            MeanLogger.recordOutput("$key/Timestamp (s)", value.timeStampSeconds)
            MeanLogger.recordOutput("$key/Tag Number", value.numTags.toDouble())
            MeanLogger.recordOutput("$key/Average Tag Area", value.avgTagDistM)
        }
    }
}

fun EstimatedRobotPose.toCameraResult(): CameraResult {
    val targets = this.targetsUsed
    val numTargets = targets.size

    var avgDist = 0.0.inches

    for (target in targets) {
        avgDist += target.bestCameraToTarget.translation.length.meters
    }

    avgDist /= numTargets.toDouble()
    this.targetsUsed.first().toTarget2D()


    return CameraResult(
        this.estimatedPose.toPose2d(),
        this.timestampSeconds,
        numTargets,
        avgDist.asMeters,
        CameraType.PHOTONVISION
    )
}

fun LimelightHelpers.PoseEstimate.toCameraResult(): CameraResult {
    return CameraResult(
        this.pose,
        this.timestampSeconds,
        this.tagCount,
        this.avgTagArea,
        CameraType.LIMELIGHT,
        false
    )
}