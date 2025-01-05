package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.util.struct.StructSerializable
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.Logger
import org.photonvision.EstimatedRobotPose
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.toVector2L
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.getRealFPGATimestamp
import org.team2471.frc.lib.util.length
import org.team2471.frc.lib.vision.CameraResult.Companion.EmptyCameraResult

@JvmRecord
data class CameraResult(
    val pose: Pose2d,
    // Seconds
    val timeStampSeconds: Double,
    val numTags: Int,
    // In % of image (check if 0-100 or 0-1)
    val avgTagArea: Double,
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
                this.timeStampSeconds
            )
        }
    }

    companion object {
        val EmptyCameraResult = CameraResult(Pose2d(), 0.0, 0, 0.0, CameraType.LIMELIGHT, true)

        fun fromLLTable(llTable: NetworkTable): CameraResult {
            val llArray = llTable.getEntry("botpose_orb_wpiblue").getDoubleArray(DoubleArray(11))
            val pos = Vector2L(llArray[0].meters, llArray[1].meters)
            return if (pos == Vector2L.Zeros) EmptyCameraResult else CameraResult(
                pose = Pose2d(Translation2d(llArray[0], llArray[1]), Rotation2d.fromRadians(llArray[5])),
                timeStampSeconds = getRealFPGATimestamp() - (llArray[6] / 1000),
                numTags = llArray[7].toInt(),
                avgTagArea = llArray[10],
                cameraType = CameraType.LIMELIGHT
            )
        }

        fun recordOutput(key: String, value: CameraResult) {
            Logger.recordOutput("$key/Pose", value.pose)
            Logger.recordOutput("$key/Timestamp (s)", value.timeStampSeconds)
            Logger.recordOutput("$key/Tag Number", value.numTags.toDouble())
            Logger.recordOutput("$key/Average Tag Area", value.avgTagArea)
        }
    }
}

fun EstimatedRobotPose.toCameraResult(): CameraResult {
    val targets = this.targetsUsed
    val targetSize = targets.size

    var avgDist = 0.0.inches
    var avgArea = 0.0

    for (target in targets) {
        avgDist += target.bestCameraToTarget.translation.length.meters
        avgArea += target.area
    }

    avgDist /= targetSize.toDouble()
    avgArea /= targetSize.toDouble()
    this.targetsUsed.first().toTarget2D()


    return CameraResult(
        this.estimatedPose.toPose2d(),
        this.timestampSeconds,
        targetSize,
        avgArea,
        CameraType.PHOTONVISION
    )
}