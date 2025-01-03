package org.team2471.frc.lib.vision

import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.Logger
import org.photonvision.EstimatedRobotPose
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.toVector2L
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.getRealFPGATimestamp
import org.team2471.frc.lib.util.length
import org.team2471.frc.lib.vision.CameraResult.Companion.EmptyCameraResult

@SuppressWarnings("unused")
data class CameraResult(
    val pos: Vector2L,
    val heading: Angle,
    // Seconds
    val timeStampSeconds: Double,
    val numTags: Int,
    // In % of image (check if 0-100 or 0-1)
    val avgTagArea: Double,
    val cameraType: CameraType,
    val isEmpty: Boolean = false
) {
    fun toArray(): DoubleArray {
        return if (isEmpty) doubleArrayOf() else doubleArrayOf(
            this.pos.x.asMeters,
            this.pos.y.asMeters,
            this.heading.asRadians,
            this.timeStampSeconds,
            this.numTags.toDouble(),
            this.avgTagArea,
            this.cameraType.toDouble()
        )
    }

    fun toGlobalPose(stdDev: Double): GlobalPose {
        return if (isEmpty) {
//            println("hello error...")
            GlobalPose.EmptyGlobalPose
        } else {
            GlobalPose(
                this.pos,
                this.heading,
                stdDev,
                this.timeStampSeconds
            )
        }
    }

    companion object {
        val EmptyCameraResult = CameraResult(Vector2L.Zeros, 0.0.radians, 0.0, 0, 0.0, CameraType.LIMELIGHT, true)

        fun fromLLTable(llTable: NetworkTable): CameraResult {
            val llArray = llTable.getEntry("botpose_orb_wpiblue").getDoubleArray(DoubleArray(11))
            val pos = Vector2L(llArray[0].meters, llArray[1].meters)
            return if (pos == Vector2L.Zeros) EmptyCameraResult else CameraResult(
                pos = pos,
                heading = llArray[5].radians,
                timeStampSeconds = getRealFPGATimestamp() - (llArray[6] / 1000),
                numTags = llArray[7].toInt(),
                avgTagArea = llArray[10],
                cameraType = CameraType.LIMELIGHT
            )
        }

        fun recordOutput(key: String, value: CameraResult) {
            Logger.recordOutput("$key/Position/X", value.pos.x.asMeters)
            Logger.recordOutput("$key/Position/Y", value.pos.y.asMeters)
            Logger.recordOutput("$key/Heading", value.heading.asDegrees)
            Logger.recordOutput("$key/Timestamp (s)", value.timeStampSeconds)
            Logger.recordOutput("$key/Tag Number", value.numTags.toDouble())
            Logger.recordOutput("$key/Average Tag Area", value.avgTagArea)
        }
    }
}

fun DoubleArray.toCameraResult(): CameraResult {
    if (this.size != 8) return EmptyCameraResult
    return CameraResult(
        pos = Vector2L(this[0].meters, this[1].meters),
        heading = this[2].radians,
        timeStampSeconds = this[3],
        numTags = this[4].toInt(),
        avgTagArea = this[6],
        cameraType = CameraType.fromDouble(this[7])
    )
}

fun DoubleArray.toCameraResults(): List<CameraResult> {
    if (this.size % 8 != 0) return listOf(EmptyCameraResult)
    val results = MutableList(this.size / 8) { EmptyCameraResult }

    for (i in 0..(this.size / 8)) {
        val start = i * 8
        results[i] = CameraResult(
            pos = Vector2L(this[start].meters, this[start + 1].meters),
            heading = this[start + 2].radians,
            timeStampSeconds = this[start + 3],
            numTags = this[start + 4].toInt(),
            avgTagArea = this[start + 6],
            cameraType = CameraType.fromDouble(this[start + 7])
        )
    }

    return results
}

fun Collection<CameraResult>.toDoubleArray(): DoubleArray {
    val out = DoubleArray(this.size * 7)
    var index = 0
    for (result in this) {
        out[index] = result.pos.x.asMeters
        out[index + 1] = result.pos.y.asMeters
        out[index + 2] = result.heading.asRadians
        out[index + 3] = result.timeStampSeconds
        out[index + 4] = result.numTags.toDouble()
        out[index + 5] = result.avgTagArea
        out[index + 6] = result.cameraType.toDouble()
        index += 7
    }
    return out
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
        avgArea,
        CameraType.PHOTONVISION
    )
}

fun NetworkTableEntry.setCameraResult(cameraResult: CameraResult) {
    this.setDoubleArray(cameraResult.toArray())
}