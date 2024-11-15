package org.team2471.frc.lib.vision

import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.photonvision.EstimatedRobotPose
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.toVector2L
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.length

data class CameraResult(
    val pos: Vector2L,
    val heading: Angle,
    val latencyMs: Double,
    val numTags: Int,
    val avgDist: Length,
    // In % of image (check if 0-100 or 0-1)
    val avgTagArea: Double,
    val cameraType: CameraType,
    val isEmpty: Boolean = false
) {
    fun toArray(): DoubleArray {
        return if (isEmpty) doubleArrayOf() else doubleArrayOf(this.pos.x.asMeters, this.pos.y.asMeters, this.heading.asRadians, this.latencyMs, this.numTags.toDouble(), this.avgDist.asMeters, this.avgTagArea, this.cameraType.toDouble())
    }

    fun getGlobalPose(stDev: Double): GlobalPose {
        return if (isEmpty) {
//            println("hello error...")
            GlobalPose.EmptyGlobalPose
        } else {
            GlobalPose(
                this.pos,
                this.heading,
                stDev,
                Timer.getFPGATimestamp() - (this.latencyMs / 1000)
            )
        }
    }

    companion object {
        val EmptyCameraResult = CameraResult(Vector2L.Zeros, 0.0.radians, 0.0, 0, 0.0.inches, 0.0, CameraType.LIMELIGHT, true)

        fun fromArray(array: DoubleArray): CameraResult {
            if (array.size != 8) return EmptyCameraResult
            return CameraResult(
                pos = Vector2L(array[0].meters, array[1].meters),
                heading = array[2].radians,
                latencyMs = array[3],
                numTags = array[4].toInt(),
                avgDist = array[5].meters,
                avgTagArea = array[6],
                cameraType = CameraType.fromDouble(array[7])
            )
        }

        fun fromLLTable(llTable: NetworkTable): CameraResult {
            val llArray = llTable.getEntry("botpose_orb_wpiblue").getDoubleArray(DoubleArray(11))
            val pos = Vector2L(llArray[0].meters, llArray[1].meters)
            return if (pos == Vector2L.Zeros) EmptyCameraResult else CameraResult(
                pos = pos,
                heading = llArray[5].radians,
                latencyMs = llArray[6],
                numTags = llArray[7].toInt(),
                avgDist = llArray[9].meters,
                avgTagArea = llArray[10],
                cameraType = CameraType.LIMELIGHT
            )
        }

        fun recordOutput(key: String, value: CameraResult) {
            Logger.recordOutput("$key/Position/X", value.pos.x.asMeters)
            Logger.recordOutput("$key/Position/Y", value.pos.y.asMeters)
            Logger.recordOutput("$key/Heading", value.heading.asDegrees)
            Logger.recordOutput("$key/Latency (ms)", value.latencyMs)
            Logger.recordOutput("$key/Tag Number", value.numTags.toDouble())
            Logger.recordOutput("$key/Average Tag Distance", value.avgTagArea)
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


    return CameraResult(
        this.toVector2L(),
        this.estimatedPose.rotation.angle.radians,
        (Timer.getFPGATimestamp() - this.timestampSeconds) * 1000,
        targetSize,
        avgDist,
        avgArea,
        CameraType.PHOTONVISION
    )
}

fun NetworkTableEntry.setCameraResult(cameraResult: CameraResult) {
    this.setDoubleArray(cameraResult.toArray())
}