package org.team2471.frc.lib.vision

import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.LogTable
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
        val EmptyCameraResult = CameraResult(Vector2L.Zeros, 0.0.radians, 0.0, 0, 0.0.inches, 0.0, CameraType.LIMELIGHT, false)

        fun fromArray(array: DoubleArray): CameraResult {
            if (array.size != 8) return EmptyCameraResult
            return CameraResult(
                Vector2L(array[0].meters, array[1].meters),
                array[2].radians,
                array[3],
                array[4].toInt(),
                array[5].meters,
                array[6],
                CameraType.fromDouble(array[7])
            )
        }

        fun fromLLTable(llTable: NetworkTable): CameraResult {
            val llArray = llTable.getEntry("botpose_wpiblue").getDoubleArray(DoubleArray(11))
            val pos = Vector2L(llArray[0].meters, llArray[1].meters)
            return if (pos == Vector2L.Zeros) EmptyCameraResult else CameraResult(
                pos,
                llArray[5].radians,
                llArray[6],
                llArray[7].toInt(),
                llArray[9].meters,
                llArray[10],
                CameraType.LIMELIGHT
            )
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


