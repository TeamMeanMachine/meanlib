package org.team2471.frc.lib.vision.limelight

//import org.littletonrobotics.junction.LogTable
//import org.littletonrobotics.junction.Logger
//import org.littletonrobotics.junction.inputs.LoggableInputs
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.asSeconds
import org.team2471.frc.lib.units.milliseconds
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.geometry.Translation2d
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.Timer
import org.wpilib.units.measure.Angle
import kotlin.math.sqrt


class VisionIOLimelight(val name: String, val useMegatag2: Boolean = true, val headingSupplier: () -> Angle): VisionIO {

    /*
        There are 5 different limelight IMU modes.
        0: Ignores internal imu, only uses external IMU through setRobotOrientation()
        1: Resets internal IMU to the given angle whenever setRobotOrientation() is called
        2: Solely relies on the internal IMU
        3: IMU_ASSIST_MT1 - uses seen AprilTags in MegaTag1 to update heading
        4: IMU_ASSIST_EXTERNALIMU - uses external IMU for gradual heading correction.
    */
    override var imuMode = 1
        set(value) {
            println("Resetting Limelight IMU")
            LimelightHelpers.SetIMUMode(name, value)
            field = value
        }

    override var imuAssistAlpha = 0.001
        set(value) {
            LimelightHelpers.SetIMUAssistAlpha(name, value)
            field = value
        }



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
            inputs.seesTag = LimelightHelpers.getTV(name)

            val llPoseEstimate =
                if (beforeFirstEnable || !useMegatag2) LimelightHelpers.getBotPoseEstimate_wpiBlue(name) else LimelightHelpers.getBotPoseEstimate_wpiBlue_MegaTag2(
                    name
                )
            //                                                                                                                                                                  kinda satisfies the compiler? ive already filtered the nulls out
            val veryRawFiducials: List<LimelightHelpers.RawFiducial> = LimelightHelpers.getRawFiducials(name)
                .filterIndexed { index, fiducial -> fiducial != null && index < 4 } as List<LimelightHelpers.RawFiducial>
            val rawFiducials = DoubleArray(veryRawFiducials.size * 4) { 0.0 }

            for (i in veryRawFiducials.indices) {
                rawFiducials[i * 4] = veryRawFiducials[i].id.toDouble()
                rawFiducials[i * 4 + 1] = veryRawFiducials[i].txnc
                rawFiducials[i * 4 + 2] = veryRawFiducials[i].tync
                rawFiducials[i * 4 + 3] = veryRawFiducials[i].ta
            }

            inputs.aprilTagPoseEstimate = llPoseEstimate?.pose ?: Pose2d()
            inputs.aprilTagTimestamp =
                Timer.getMonotonicTimestamp() - (llPoseEstimate?.latency?.milliseconds?.asSeconds ?: 0.0)
            inputs.rawFiducials = rawFiducials

            inputs.objectCorners = DoubleArray(8) { 0.0 }
            inputs.objectCoords = DoubleArray(2) { 0.0 }
        } else {
            inputs.objectCoords = doubleArrayOf(
                LimelightHelpers.getTX(name), LimelightHelpers.getTY(name)
            )

            inputs.objectCorners = NetworkTableInstance.getDefault().getTable(name).getEntry("tcornxy")
                .getDoubleArray(DoubleArray(8) { 0.0 })

            inputs.aprilTagPoseEstimate = Pose2d()
            inputs.aprilTagTimestamp = 0.0
        }

        Logger.processInputs(name, inputs)
    }

    fun onConnect() {
        LimelightHelpers.SetIMUMode(name, imuMode)
        LimelightHelpers.SetIMUAssistAlpha(name, imuAssistAlpha)

        if (isEnabled) {
            LimelightHelpers.SetThrottle(name, 0)
        } else {
//            LimelightHelpers.SetThrottle(name, 100)
        }
    }

    override fun enable() {
        beforeFirstEnable = false
        isEnabled = true
        LimelightHelpers.SetIMUMode(name, imuMode)
        LimelightHelpers.SetIMUAssistAlpha(name, imuAssistAlpha)
        LimelightHelpers.SetThrottle(name, 0)
    }

    override fun disable() {
        isEnabled = false
//        LimelightHelpers.SetThrottle(name, 100)
    }



    override fun gyroReset() {
        LimelightHelpers.SetIMUMode(name, 1)
        LimelightHelpers.SetRobotOrientation(name, headingSupplier.invoke().asDegrees, 0.0, 0.0, 0.0, 0.0, 0.0)
        imuMode = imuMode
    }

    override fun disabledGyroReset() {
        LimelightHelpers.SetIMUMode(name, 1)
        LimelightHelpers.SetRobotOrientation(name, headingSupplier.invoke().asDegrees, 0.0, 0.0, 0.0, 0.0, 0.0)
    }

    override fun updateCropping(minX: Double, maxX: Double, minY: Double, maxY: Double) {
        LimelightHelpers.setCropWindow(name, minX, maxX, minY, maxY)
    }
}

interface VisionIO {

    var imuMode: Int
    var imuAssistAlpha: Double
    var mode: LimelightMode

    fun updateInputs(inputs: VisionIOInputs)

    fun enable()
    fun disable()

    fun gyroReset()
    fun disabledGyroReset()

    fun updateCropping(minX: Double, maxX: Double, minY: Double, maxY: Double)

    open class VisionIOInputs : LoggableInputs {

        var isConnected = false
        var mode = LimelightMode.APRILTAG

        // April Tag
        var seesTag = false

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
        var objectCorners: DoubleArray = DoubleArray(8) { 0.0 }
        var objectCoords: DoubleArray = DoubleArray(2) { 0.0 }

        val hasObjects: Boolean
            get() = objectCorners.isNotEmpty() && objectCoords.isNotEmpty()

        val objectCenter: Translation2d
            get() {
                if (hasObjects) {
                    val targetCornersX = objectCorners.filterIndexed { index, _ -> index % 2 == 0 }
                    val targetCornersY = objectCorners.filterIndexed { index, _ -> index % 2 == 1 }

                    return Translation2d(
                        (targetCornersX.max() + targetCornersX.min()) / 2,
                        (targetCornersY.max() + targetCornersY.min()) / 2
                    )
                } else {
                    return Translation2d()
                }
            }

        val objectDimensions: Pair<Double, Double>
            get() {
                try {
                    val targetCornersX = objectCorners.filterIndexed { index, _ -> index % 2 == 0 }
                    val targetCornersY = objectCorners.filterIndexed { index, _ -> index % 2 == 1 }

                    return targetCornersX.max() - targetCornersX.min() to targetCornersY.max() - targetCornersY.min()
                } catch (_: Exception) {
                    return 0.0 to 0.0
                }
            }

        override fun toLog(table: LogTable) {
            table.put("Is Connected", isConnected)
            table.put("Mode", mode)
            table.put("Sees Tag", seesTag)
            table.put("AprilTag Pose Estimate", aprilTagPoseEstimate)
            table.put("AprilTag Timestamp", aprilTagTimestamp)
            table.put("RawFiducials", rawFiducials)
            table.put("Object Corners", objectCorners)
            table.put("Object Coordinates", objectCoords)
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("Is Connected", isConnected)
            mode = table.get("Mode", mode)
            seesTag = table.get("Sees Tag", seesTag)
            aprilTagPoseEstimate = table.get("AprilTag Pose Estimate", aprilTagPoseEstimate).first()
            aprilTagTimestamp = table.get("AprilTag Timestamp", aprilTagTimestamp)
            rawFiducials = table.get("RawFiducials", rawFiducials)
            objectCorners = table.get("Object Corners", objectCorners)
            objectCoords = table.get("Object Coordinates", objectCoords)
        }
    }
}

enum class LimelightMode {
    APRILTAG, GAMEPIECE
}