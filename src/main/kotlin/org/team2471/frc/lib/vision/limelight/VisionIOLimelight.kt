package org.team2471.frc.lib.vision.limelight

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.units.measure.Angle
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.asSeconds
import org.team2471.frc.lib.units.milliseconds
import kotlin.math.sqrt


class VisionIOLimelight(val name: String, val useMegatag2: Boolean = true, val headingSupplier: () -> Angle) :
    VisionIO {

    override var mode: LimelightMode = LimelightMode.APRILTAG
        set(value) {
            when (value) {
                LimelightMode.APRILTAG -> LimelightHelpers.setPipelineIndex(name, 0)
                LimelightMode.GAMEPIECE -> LimelightHelpers.setPipelineIndex(name, 1)
            }

            field = value
        }

    val heartbeatSub = NetworkTableInstance.getDefault().getTable(name).getDoubleTopic("hb").subscribe(0.0)
    var prevHeartbeats = MutableList(3) { 0.0 }
    var beforeFirstEnable = true
    var isEnabled = false

    override fun updateInputs(inputs: VisionIO.VisionIOInputs) {

        val heartbeat = heartbeatSub.get()
        if (heartbeat != 0.0 && prevHeartbeats[2] != heartbeat) {
            if (!inputs.isConnected) {
                onConnect()
            }
            inputs.isConnected = true
        } else {
            inputs.isConnected = false
        }
        prevHeartbeats.add(0, heartbeat)
        prevHeartbeats.removeAt(prevHeartbeats.size - 1)

        inputs.mode = mode

        if (mode == LimelightMode.APRILTAG) {
            val llPoseEstimate =
                if (beforeFirstEnable || !useMegatag2) LimelightHelpers.getBotPoseEstimate_wpiBlue(name) else LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(
                    name
                )
            //                                                                                                                                                                  kinda satisfies the compiler? ive already filtered the nulls out
            val veryRawFiducials: List<LimelightHelpers.RawFiducial> = LimelightHelpers.getRawFiducials(name)
                .filterIndexed { index, fiducial -> fiducial != null && index < 4 } as List<LimelightHelpers.RawFiducial>
            val rawFiducials = DoubleArray(15) { 0.0 }

            for (i in veryRawFiducials.indices) {
                rawFiducials[i * 4] = veryRawFiducials[i].id.toDouble()
                rawFiducials[i * 4 + 1] = veryRawFiducials[i].txnc
                rawFiducials[i * 4 + 2] = veryRawFiducials[i].tync
                rawFiducials[i * 4 + 3] = veryRawFiducials[i].ta
            }

            inputs.aprilTagPoseEstimate = llPoseEstimate?.pose ?: Pose2d()
            inputs.aprilTagTimestamp =
                Timer.getFPGATimestamp() - (llPoseEstimate?.latency?.milliseconds?.asSeconds ?: 0.0)
            inputs.rawFiducials = rawFiducials

            inputs.targetCorners = DoubleArray(8) { 0.0 }
            inputs.targetCoords = DoubleArray(2) { 0.0 }
        } else {
            inputs.targetCoords = doubleArrayOf(
                LimelightHelpers.getTX(name), LimelightHelpers.getTY(name)
            )

            inputs.targetCorners = NetworkTableInstance.getDefault().getTable(name).getEntry("tcornxy")
                .getDoubleArray(DoubleArray(8) { 0.0 })

            inputs.aprilTagPoseEstimate = Pose2d()
            inputs.aprilTagTimestamp = 0.0
        }

        Logger.processInputs(name, inputs)
    }

    fun onConnect() {/*
            There are 5 different limelight IMU modes.
            0: Ignores internal imu, only uses external IMU through setRobotOrientation()
            1: Resets internal IMU to the given angle whenever setRobotOrientation() is called
            2: Solely relies on the internal IMU
            3: IMU_ASSIST_MT1 - uses seen AprilTags in MegaTag1 to update heading
            4: IMU_ASSIST_EXTERNALIMU - uses external IMU for gradual heading correction.
         */
        // We primarily use 3, but will switch to 1 on gyro reset
        // I want to test not even switching to 1
        LimelightHelpers.SetIMUMode(name, 3)

        if (isEnabled) {
            LimelightHelpers.SetThrottle(name, 0)
        } else {
            LimelightHelpers.SetThrottle(name, 200)
        }
    }

    override fun enable() {
        beforeFirstEnable = false
        isEnabled = true
        LimelightHelpers.SetThrottle(name, 0)
    }

    override fun disable() {
        isEnabled = false
        LimelightHelpers.SetThrottle(name, 200)
    }

    override fun gyroReset() {
        LimelightHelpers.SetIMUMode(name, 1)
        LimelightHelpers.SetRobotOrientation(name, headingSupplier.invoke().asDegrees, 0.0, 0.0, 0.0, 0.0, 0.0)
        LimelightHelpers.SetIMUMode(name, 3)
    }

    private fun updateCropping(fiducials: List<Triple<Double, Pair<Double, Double>, Double>>) {
        // todo tune this
        val overshootPercentage = 1.25

        var minCoord = Pair(-1.0, -1.0)
        var maxCoord = Pair(1.0, 1.0)

        for (fiducial in fiducials) {
            // Todo figure these out
            val normalizedTx = fiducial.second.first / 1.0
            val normalizedTy = fiducial.second.second / 1.0

            // half a side length               area of image (when x and y are between -1 and 1)
            val targetRadius = sqrt(fiducial.third) / 2.0 * 4.0

            val minX = normalizedTx - targetRadius * overshootPercentage
            val maxX = normalizedTx + targetRadius * overshootPercentage
            val minY = normalizedTy - targetRadius * overshootPercentage
            val maxY = normalizedTy + targetRadius * overshootPercentage

            if (minX < minCoord.first) minCoord = Pair(minX, minCoord.second)
            if (maxX > minCoord.first) maxCoord = Pair(maxX, minCoord.second)
            if (minY < minCoord.second) minCoord = Pair(minCoord.first, minY)
            if (maxY < minCoord.second) maxCoord = Pair(minCoord.first, maxY)
        }

        LimelightHelpers.setCropWindow(name, minCoord.first, maxCoord.first, minCoord.second, maxCoord.second)
    }
}

interface VisionIO {

    var mode: LimelightMode

    fun updateInputs(inputs: VisionIOInputs)

    fun enable()
    fun disable()

    fun gyroReset()

    open class VisionIOInputs : LoggableInputs {

        var isConnected = false
        var mode = LimelightMode.APRILTAG

        // April Tag
        var aprilTagPoseEstimate = Pose2d()

        // Seconds
        var aprilTagTimestamp = 0.0

        //id, x, y, area, id, x, y, area, id, x, y, area,...
        // if you end up seeing more than 5 tags ill be shocked
        var rawFiducials: DoubleArray = DoubleArray(20) { 0.0 }

        // (id, (x, y), area),...
        val trimmedFiducials: List<Triple<Double, Pair<Double, Double>, Double>>
            get() {
                return rawFiducials.filterIndexed { index, value -> index % 4 == 0 && value != 0.0 }
                    .mapIndexed { index, id ->
                        Triple(
                            id,
                            Pair(rawFiducials[index * 4 + 1], rawFiducials[index * 4 + 2]),
                            rawFiducials[index * 4 + 3]
                        )
                    }
            }


        // object detection
        var targetCorners: DoubleArray = DoubleArray(8) { 0.0 }
        var targetCoords: DoubleArray = DoubleArray(2) { 0.0 }

        val hasTargets: Boolean
            get() = targetCorners.isNotEmpty() && targetCoords.isNotEmpty()

        val targetCenter: Translation2d
            get() {
                if (hasTargets) {
                    val targetCornersX = targetCorners.filterIndexed { index, _ -> index % 2 == 0 }
                    val targetCornersY = targetCorners.filterIndexed { index, _ -> index % 2 == 1 }

                    return Translation2d(
                        (targetCornersX.max() + targetCornersX.min()) / 2,
                        (targetCornersY.max() + targetCornersY.min()) / 2
                    )
                } else {
                    return Translation2d()
                }
            }

        val targetDimensions: Pair<Double, Double>
            get() {
                try {
                    val targetCornersX = targetCorners.filterIndexed { index, _ -> index % 2 == 0 }
                    val targetCornersY = targetCorners.filterIndexed { index, _ -> index % 2 == 1 }

                    return targetCornersX.max() - targetCornersX.min() to targetCornersY.max() - targetCornersY.min()
                } catch (_: Exception) {
                    return 0.0 to 0.0
                }
            }

        override fun toLog(table: LogTable) {
            table.put("Is Connected", isConnected)
            table.put("Mode", mode)
            table.put("AprilTag Pose Estimate", aprilTagPoseEstimate)
            table.put("AprilTag Timestamp", aprilTagTimestamp)
            table.put("Target Corners", targetCorners)
            table.put("Target Coordinates", targetCoords)
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("Is Connected", isConnected)
            mode = table.get("Mode", mode)
            aprilTagPoseEstimate = table.get("AprilTag Pose Estimate", aprilTagPoseEstimate).first()
            aprilTagTimestamp = table.get("AprilTag Timestamp", aprilTagTimestamp)
            targetCorners = table.get("Target Corners", targetCorners)
            targetCoords = table.get("Target Coordinates", targetCoords)
        }
    }
}

enum class LimelightMode {
    APRILTAG, GAMEPIECE
}