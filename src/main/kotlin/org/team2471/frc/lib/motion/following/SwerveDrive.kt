package org.team2471.frc.lib.motion.following

import choreo.trajectory.Trajectory
import com.team254.lib.util.Interpolable
import com.team254.lib.util.InterpolatingDouble
import com.team254.lib.util.InterpolatingTreeMap
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.kinematics.SwerveModuleState
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.units.Units.*
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.ironmaple.simulation.drivesims.SelfControlledSwerveDriveSimulation
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion.following.SwerveDrive.Companion.simulatedDrive
import org.team2471.frc.lib.motion.following.SwerveDrive.Companion.useMapleSim
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.*
import org.team2471.frc.lib.vision.VisionPoseEstimator
import kotlin.math.*
private val poseHistory = InterpolatingTreeMap<InterpolatingDouble, SwerveDrive.Pose>(75)
private var prevPosition = Vector2(0.0, 0.0)
private var prevPose = SwerveDrive.Pose(Vector2(0.0, 0.0), 0.0.degrees)
private var prevPathPosition = Vector2(0.0, 0.0).feet
private var prevTime = -0.02
private var prevPathHeading = 0.0.radians
private val MAXHEADINGSPEED_DEGREES_PER_SECOND = 600.0
private val MAXTRANSLATIONSPEED_FEET_PER_SECOND = 15.0

private var prevHeadingError = 0.0.degrees

interface SwerveDrive {
    val parameters: SwerveParameters
    var heading: Angle
    var headingRate: AngularVelocity
    var position: Vector2
    var velocity: Vector2
    var acceleration: Vector2
    var deltaPos: Vector2L
    var robotPivot: Vector2L // location of rotational pivot in robot coordinates
    var headingSetpoint: Angle
    val carpetFlow: Vector2
    val kCarpet: Double
    val kTread: Double
    val plannedPath: NetworkTableEntry
    val actualRoute: NetworkTableEntry
    var lastResetTime: Double

    val gyroConnected: Boolean

    val modules: Array<Module>

    val poseEstimator: VisionPoseEstimator

    fun resetOdom() = Unit

    val isRedAlliance: Boolean
        get() =
            if (DriverStation.getAlliance().isEmpty) {
                prevIsRedAlliance ?: true
            } else {
                (DriverStation.getAlliance().get() == DriverStation.Alliance.Red).also { prevIsRedAlliance = it }
            }
    val isBlueAlliance: Boolean get() = !isRedAlliance

    companion object {
        private var prevIsRedAlliance: Boolean? = null
        var simulatedDrive = SelfControlledSwerveDriveSimulation(
            SwerveDriveSimulation(DriveTrainSimulationConfig.Default(), Pose2d(2.0, 2.0, Rotation2d(0.0))))
        var useMapleSim = false
    }

    interface Module {
        val index: Int

        val gearRatio: Double

        // module fixed parameters
        val modulePosition: Vector2L // coordinates of module in robot coordinates

        val angleOffset: Angle

        var wheelDiameter: Length

        // encoder interface
        val angle: Angle
        val speed: Double
        val acceleration: Double
        val currDistance: Double
        var prevDistance: Double
        var prevSpeed: Double
        val treadWear: Double
        var odometer: Double

        var prevAngle: Angle

        // motor interface
        var angleSetpoint: Angle
        val rawWheelRotation: Angle

        fun setDrivePower(power: Double)
        fun setDriveVelocityVoltage(velocity: LinearVelocity)

        fun stop()
        fun zeroEncoder()
        fun driveWithDistance(angle: Angle, distance: Length)
    }

    data class Pose(val position: Vector2, val heading: Angle) : Interpolable<Pose>/*, (Double) -> Pose?*/ {
        override fun interpolate(other: Pose, x: Double): Pose = when {
            x <= 0.0 -> this
            x >= 1.0 -> other
            else -> Pose(position.interpolate(other.position, x), (other.heading - heading) * x + heading)
        }
    }

    data class PathPose(
        val totalTime: Double,
        val position: Vector2L,
        val heading: Angle,
        val velocityPerSec: Vector2L? = null,
        val rotationalVelocityPerSec: Angle? = null
    )
}

val SwerveDrive.pose: SwerveDrive.Pose
    get() = SwerveDrive.Pose(position, heading)
val SwerveDrive.demoMode: Boolean
    get() = demoSpeed < 1.0
val SwerveDrive.demoSpeed: Double
    get() = SmartDashboard.getNumber("DemoSpeed", 1.0).coerceIn(0.0, 1.0)

fun SwerveDrive.lookupPose(time: Double): SwerveDrive.Pose? =
    if (time < lastResetTime) SwerveDrive.Pose(position, heading) else poseHistory.getInterpolated(
        InterpolatingDouble(time)
    )

fun SwerveDrive.configureSim(newSim: SwerveDriveSimulation) {
    if (isSim) simulatedDrive = SelfControlledSwerveDriveSimulation(newSim)
}

fun SwerveDrive.poseDiff(latency: Double): SwerveDrive.Pose? {
    val currPose = pose
    val previousPose = lookupPose(getRealFPGATimestamp().minus(latency))
    return if (previousPose == null) {
        null
    } else {
        SwerveDrive.Pose(currPose.position - previousPose.position, (currPose.heading - previousPose.heading).wrap())
    }
}

fun SwerveDrive.stop() {
    for (module in modules) {
        module.stop()
    }
    if (isSim && useMapleSim) simulatedDrive.runSwerveStates(Array(4) { SwerveModuleState(0.0, Rotation2d(0.0)) })
}

fun SwerveDrive.zeroEncoders() {
    for (module in modules) {
        module.zeroEncoder()
    }
    position = Vector2(0.0, 0.0)
}


fun SwerveDrive.drive(
    translation: Vector2,
    turn: Double,
    fieldCentric: Boolean = true,
    closedLoopHeading: Boolean = false,
    softTranslation: Vector2 = Vector2(0.0, 0.0),
    softTurn: Double = 0.0
) {
    var requestedTranslation = translation

    if (fieldCentric) {
        requestedTranslation = requestedTranslation.rotateDegrees(-heading.asDegrees)
        // Correct for moving while spinning
        requestedTranslation = requestedTranslation.rotateDegrees(turn * parameters.kMoveWhileSpin)
        //println("Correction: ${turn * 60.0}")
    }
    requestedTranslation += softTranslation

    if (!SmartDashboard.containsKey("DemoSpeed")) {
        SmartDashboard.setDefaultNumber("DemoSpeed", 1.0)
        SmartDashboard.setPersistent("DemoSpeed")
    }
    requestedTranslation *= demoSpeed

    var requestedTurn = turn + softTurn

    requestedTurn *= demoSpeed

    if (requestedTranslation.length > 0.01 && requestedTurn.absoluteValue < 0.01) {
        if (closedLoopHeading) {  // closed loop on heading position
            // heading error
            val headingError = (heading - headingSetpoint).wrap()
//            println("Heading Error: $headingError.")

            // heading d
            val deltaHeadingError = headingError - prevHeadingError
            prevHeadingError = headingError

            requestedTurn = headingError.asDegrees * parameters.kpHeading * 0.60 + deltaHeadingError.asDegrees * parameters.kdHeading
        } else if (parameters.gyroRateCorrection > 0.0) {  // closed loop on heading velocity
            requestedTurn += (requestedTurn * MAXHEADINGSPEED_DEGREES_PER_SECOND - headingRate.changePerSecond.asDegrees) * parameters.gyroRateCorrection
        }
    } else {
        headingSetpoint = heading
    }

    if (requestedTranslation.x == 0.0 && requestedTranslation.y == 0.0 && requestedTurn == 0.0) {
        return stop()
    }

    val requestedLocalGoals = Array(modules.size) { Vector2(0.0, 0.0) }
    for (i in modules.indices) {
        requestedLocalGoals[i] = requestedTranslation + (modules[i].modulePosition - robotPivot).asInches.perpendicular().normalize() * requestedTurn
    }

    val speeds = Array(modules.size) { 0.0 }

    for (i in modules.indices) {
        val angleAndSpeed = modules[i].calculateAngleAndSpeed(requestedLocalGoals[i])
        modules[i].angleSetpoint = angleAndSpeed.angle
        speeds[i] = angleAndSpeed.power
    }

    // adjust wheels to account for velocity of highest speed wheel
    val maxSpeed = speeds.maxByOrNull(Math::abs)!!
    if (maxSpeed > 1.0) {
        for (i in speeds.indices) {
            speeds[i] /= maxSpeed
        }
    }

    for (i in modules.indices) {
        //print("${modules[i].currDistance} ")
        modules[i].setDrivePower(speeds[i])
    }

    if (isSim && useMapleSim) {
        val moduleStates = Array(modules.size) { SwerveModuleState() }
        for (i in modules.indices) {
            moduleStates[i] = SwerveModuleState(
                speeds[i] * simulatedDrive.maxLinearVelocity().`in`(MetersPerSecond),
                modules[i].angleSetpoint.wrap().asRotation2d)
        }
        simulatedDrive.runSwerveStates(moduleStates)
    }
    //println()
//    recordOdometry()
}

data class AngleAndSpeed(val angle: Angle, val power: Double)

private fun SwerveDrive.Module.calculateAngleAndSpeed(localGoal: Vector2): AngleAndSpeed {
    var currAngle = angle
    var power = localGoal.length
    var setPoint = localGoal.angle


    if (isSim && useMapleSim) {
        currAngle = simulatedDrive.measuredStates[index].angle.asAngle.wrap()
    }

    val angleError = (setPoint - currAngle).wrap()
    if (Math.abs(angleError.asRadians) > Math.PI / 2.0) {
        setPoint -= Math.PI.radians
        power = -power
    }
    return AngleAndSpeed(setPoint, power * Math.abs(angleError.cos()))
}

suspend fun SwerveDrive.Module.steerToAngle(angle: Angle, tolerance: Angle = 2.degrees) {
    try {
        periodic() {
            angleSetpoint = angle

            val error = (angle - this@steerToAngle.angle).wrap()

            if (error.asRadians.absoluteValue < tolerance.asRadians) stop()
        }
        delay(0.2)
    } finally {
        stop()
    }
}

data class ModuleState(val translation: Vector2, val velocity: Vector2, val acceleration: Vector2)

fun SwerveDrive.Module.recordOdometry(heading: Angle, carpetFlow: Vector2, kCarpet: Double, gyroConnected: Boolean): ModuleState {

    val moduleAngle = angle

    val angleInFieldSpace = if (gyroConnected) heading + moduleAngle else (heading - prevAngle) + (moduleAngle - prevAngle)  //prevAngleInFieldSpace + deltaAngle
    val wheelDir = angleInFieldSpace.vector2
    var signedWheelDir = wheelDir

    val holdDistance = currDistance
    var deltaDistance = (holdDistance - prevDistance)
    if (deltaDistance < 0.0) {
        signedWheelDir *= -1.0
    }


    if (isReal) {
        deltaDistance *= (1.0 + signedWheelDir.dot(carpetFlow) * kCarpet) * treadWear //direction based kCarpet
    } else if (useMapleSim) {
        val simAngle = -simulatedDrive.measuredStates[index].angle.asAngle.wrap()
        val simAngleFieldSpace = heading + moduleAngle // heading + simAngle
        val simSpeed = simulatedDrive.measuredStates[index].speedMetersPerSecond.meters.asFeet
        val simAccel = simSpeed - prevSpeed
        val simDistance = simulatedDrive.latestModulePositions[index].distanceMeters.meters.asFeet
        val simDeltaDistance = simDistance - prevDistance


        val simWheelDir = Vector2(simAngleFieldSpace.cos(), simAngleFieldSpace.sin())

        prevDistance = simDistance
        prevAngle = simAngle
        prevSpeed = simSpeed

        return ModuleState(simWheelDir * simDeltaDistance, simWheelDir * simSpeed, simWheelDir * simAccel)
    }

    prevDistance = holdDistance
    prevAngle = moduleAngle

    return ModuleState(wheelDir * deltaDistance, wheelDir * speed, wheelDir * acceleration)
}

fun SwerveDrive.recordOdometry() {
    var robotTranslation = Vector2(0.0, 0.0)
    var robotRotation = 0.0.degrees
    var robotVelocity = Vector2(0.0, 0.0)
    var robotAcceleration = Vector2(0.0, 0.0)

    val time = getRealFPGATimestamp()
    val dt = time - prevTime

    val numberOfModules = modules.size.toDouble()

    for (i in modules.indices) {
        val moduleState = modules[i].recordOdometry(heading, carpetFlow, kCarpet, gyroConnected)


        val translation = moduleState.translation
        modules[i].odometer += translation.length

//        val modulePosition = modules[i].modulePosition.asFeet.mirrorYAxis().flipXAndY().rotate(heading)
        val modulePosition = modules[i].modulePosition.asFeet.rotate(heading)

        val deltaAngle = ((translation + modulePosition).angle - modulePosition.angle).wrap() //calculate robot rotation using swerve translation

        robotRotation += deltaAngle / numberOfModules
        robotTranslation += translation / numberOfModules
        robotVelocity += moduleState.velocity / numberOfModules
        robotAcceleration += moduleState.acceleration / numberOfModules
    }

    position += Vector2(robotTranslation.x, robotTranslation.y)
//    poseEstimator.updateOdometry(getRealFPGATimestamp(), position.feet)
//    println("hi there: ${modules.map { it.wpiPosition.distanceMeters }}")
    deltaPos = Vector2L(robotTranslation.x.feet, robotTranslation.y.feet)
    velocity = robotVelocity
    acceleration = robotAcceleration
    if (!gyroConnected) { //if gyro is not connected, update heading
        if (isSim && useMapleSim) {
            val mHeadingRate = simulatedDrive.actualSpeedsFieldRelative.omegaRadiansPerSecond.radians

            headingRate = mHeadingRate.perSecond
            heading += (mHeadingRate * 0.02)
        } else {
//        println("robotRotation $robotRotation")
            heading += robotRotation
            headingRate = (robotRotation / dt).perSecond
        }
    }

    poseHistory[InterpolatingDouble(time)] = pose
    prevTime = time
    prevPosition = position
    prevPose = pose
}

fun SwerveDrive.odometryReset() {
    for (module in modules) {
        module.prevDistance = 0.0
    }
    zeroEncoders()
    position = Vector2(0.0, 0.0)
    poseHistory.clear()
    poseEstimator.reset(Vector2L.Zeros, getRealFPGATimestamp(), true)
    resetOdom()
}

fun SwerveDrive.resetHeading() {
    heading = ((0.0).degrees)
    resetOdom()
}

suspend fun SwerveDrive.driveAlongPath(
    path: Path2D,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    inResetGyro: Boolean? = null,
    turnOverride: () -> Double? = { null },
    earlyExit: (percentComplete: Double) -> Boolean = { false },
    useApriltags: Boolean = false
) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, reflected ${path.isReflected}, turnOverride ${turnOverride() != null}")

    val pathPoseSupplier: (Double) -> SwerveDrive.PathPose = {
        SwerveDrive.PathPose(path.durationWithSpeed, path.getPosition(it).feet, path.getAbsoluteHeadingDegreesAt(it).degrees)
    }

    driveAlongPathGeneric(pathPoseSupplier, resetOdometry, extraTime, inResetGyro, turnOverride, earlyExit, useApriltags)
}

suspend fun SwerveDrive.driveAlongChoreoPath(
    path: Trajectory<*>?,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    inResetGyro: Boolean? = null,
    turnOverride: () -> Double? = { null },
    earlyExit: (percentComplete: Double) -> Boolean = { false },
    useApriltags: Boolean = false,
    useVelocity: Boolean = false
) {
    val t = Timer()
    t.start()
    println("inside driveAlongChoreoPath ${path?.name()} time: ${t.get()}")

    if (path == null || path.getInitialPose(false).isEmpty) { println("path is null or empty"); return } //exit if path is null

    val pathPoseSupplier: (Double) -> SwerveDrive.PathPose = {
        val currSample = path.sampleAt(it, false).get()
        val velocity = Vector2(currSample.chassisSpeeds.vxMetersPerSecond, currSample.chassisSpeeds.vyMetersPerSecond).meters
        SwerveDrive.PathPose(
            path.totalTime,
            currSample.pose.translation.asVector2().meters,
            currSample.pose.rotation.asAngle,
            velocity,
            currSample.chassisSpeeds.omegaRadiansPerSecond.radians)
    }

    println("Created supplier, calling generic. t: ${t.get()}")
    if (useVelocity) {
        driveAlongPathGenericWithVelocity(pathPoseSupplier, resetOdometry, extraTime, inResetGyro, turnOverride, earlyExit, useApriltags)
    } else {
        driveAlongPathGeneric(pathPoseSupplier, resetOdometry, extraTime, inResetGyro, turnOverride, earlyExit, useApriltags)
    }
    println("finished generic. t: ${t.get()}")
}

suspend fun SwerveDrive.driveAlongPathGeneric(
    path: (seconds: Double) -> SwerveDrive.PathPose,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    inResetGyro: Boolean? = null,
    turnOverride: () -> Double? = { null },
    earlyExit: (percentComplete: Double) -> Boolean = { false },
    useApriltags: Boolean = false
) {
    println("inside driveAlongPathGeneric ${path(0.0)}")

    if (inResetGyro ?: resetOdometry) {
        println("Heading = $heading")
        heading = path(0.0).heading
        if (isSim && useMapleSim) simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
        println("After Reset Heading = $heading")
    }

    if (resetOdometry) {
        println("Position = $position")

        // set to the numbers required for the start of the path

        if (isSim && useMapleSim) {
            simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
        }
        position = path(0.0).position.asFeet
        if (useApriltags) {
            poseEstimator.reset(path(0.0).position, odometryReset = true)
        }
//        position = path(0.0).position.asFeet
//        if (isSim && useMapleSim) simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
//        prevPosition = position

        println("After Reset Position = $position")
        println("April Tag Position = ${poseEstimator.latestPos}")
    }


    var prevTime = -0.2

    val timer = Timer()
    timer.start()
    var prevPositionError = Vector2(0.0, 0.0).meters
    var prevHeadingError = 0.0.degrees
    suspendUntil(10) { timer.get() != 0.0 }
    println("entering drive periodic")
    periodic {
        val t = timer.get()
        val dt = if (t - prevTime != 0.0) t - prevTime else 0.02
        val pathSample = path(t)

        // position error
        val pathPosition = pathSample.position
        val currentPosition = if (useApriltags) poseEstimator.latestPos else position.feet
        val positionError = pathPosition - currentPosition
//        println("time=$t   dt=$dt    pathPosition=$pathPosition position=$currentPosition positionError=$positionError")

        // position feed forward
        val pathVelocity = pathSample.velocityPerSec ?: ((pathPosition - prevPathPosition) / dt)

        // position d
        val deltaPositionError = positionError - prevPositionError
        prevPositionError = positionError

        var translationControlField =
            pathVelocity.asFeet * parameters.kPositionFeedForward + positionError.asFeet * parameters.kpPosition + deltaPositionError.asFeet * parameters.kdPosition

        translationControlField = Vector2(-translationControlField.y, translationControlField.x)
//        println("translationControlField = $translationControlField")


        // heading error
        val robotHeading = heading
        val pathHeading = pathSample.heading
        MeanLogger.recordOutput("pathPose", pathPosition.asMeters.toPose2d(pathHeading))
        val headingError = (robotHeading - pathHeading).wrap()
//        println("Heading Error: $headingError. pathHeading: $pathHeading")

        // heading feed forward
        val headingVelocity = pathSample.rotationalVelocityPerSec ?: ((pathHeading - prevHeadingError) / dt)

        // heading d
        val deltaHeadingError = headingError - prevHeadingError
        prevHeadingError = headingError

        val turnControl = headingVelocity.asDegrees * parameters.kHeadingFeedForward + headingError.asDegrees * parameters.kpHeading + deltaHeadingError.asDegrees * parameters.kdHeading
//        println("Turn Control: $turnControl")
        if (turnControl.isNaN() || translationControlField.y.isNaN() || translationControlField.x.isNaN()) {
            println("turnControl: $turnControl")
            println("translationControlField $translationControlField")
            println("dt: $dt")

//            throw IllegalArgumentException("requestedVolts == NaN")
        }

        // send it
        drive(Vector2(translationControlField.y, -translationControlField.x), turnOverride() ?: turnControl, true)

        // are we done yet?
        if (t >= path(t).totalTime + extraTime) {
            println("exiting path")
            stop()
        }
        if (earlyExit(t / (path(t).totalTime + extraTime))) {
            println("early exiting path. time: $t  duration: ${path(t).totalTime} percent complete: ${t / path(t).totalTime}")
            stop()
        }
        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }
    println("at the end of driveAlongChoreoPath")
    MeanLogger.recordOutput("pathPose", Pose2d())

    // shut it down
    drive(Vector2(0.0, 0.0), 0.0, true)
//    actualRoute.setDoubleArray(doubleArrayOf())
//    plannedPath.setString("")
}
suspend fun SwerveDrive.driveAlongPathGenericWithVelocity(
    path: (seconds: Double) -> SwerveDrive.PathPose,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    inResetGyro: Boolean? = null,
    turnOverride: () -> Double? = { null },
    earlyExit: (percentComplete: Double) -> Boolean = { false },
    useApriltags: Boolean = false
) {
    println("inside driveAlongPathGeneric ${path(0.0)}. New timer started.")
    if (path(0.0).velocityPerSec == null || path(0.0).rotationalVelocityPerSec == null) throw IllegalArgumentException("Path Velocity is null, path is corrupted or not using choreo path")

    if (inResetGyro ?: resetOdometry) {
        println("Heading = $heading")
        heading = path(0.0).heading
        if (isSim && useMapleSim) simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
        println("After Reset Heading = $heading")
    }

    if (resetOdometry) {
        println("Position = $position")

        // set to the numbers required for the start of the path

        if (isSim && useMapleSim) {
            simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
            println("maplesim pose after reset ${simulatedDrive.actualPoseInSimulationWorld.translation.asVector2().meters.asFeet}")
        }
        position = path(0.0).position.asFeet
        if (useApriltags) {
            poseEstimator.reset(path(0.0).position, odometryReset = true)
        }
//        position = path(0.0).position.asFeet
//        if (isSim && useMapleSim) simulatedDrive.setSimulationWorldPose(path(0.0).position.asMeters.toPose2d(path(0.0).heading))
//        prevPosition = position

        println("After Reset Position = $position")
        println("April Tag Position = ${poseEstimator.latestPos}")
    }

    println("After reset code.")


    var prevTime = -0.2

    val timer = Timer()
    timer.start()
    var prevPositionError = Vector2(0.0, 0.0).meters
    var prevHeadingError = 0.0.degrees
    println("entering drive periodic.")
    periodic {
        val t = timer.get()
        val dt = t - prevTime
        val pathSample = path(t)

        // position error
        val pathPosition = pathSample.position
        val currentPosition = if (useApriltags) poseEstimator.latestPos else position.feet
        val positionError = pathPosition - currentPosition
//        println("time=$t   dt=$dt    pathPosition=$pathPosition position=$currentPosition positionError=$positionError")

        // position feed forward
        val pathVelocity = pathSample.velocityPerSec!!

        // position d
        val deltaPositionError = positionError - prevPositionError
        prevPositionError = positionError

        var translationControlField = pathVelocity.asFeet + positionError.asFeet * parameters.kpPosition * 14.5 + deltaPositionError.asFeet * parameters.kdPosition * 00.0

        translationControlField = Vector2(-translationControlField.y, translationControlField.x)
        println("translationControlField = $translationControlField")


        // heading error
        val robotHeading = heading
        val pathHeading = pathSample.heading
        MeanLogger.recordOutput("pathPose", pathPosition.asMeters.toPose2d(pathHeading))
        val headingError = (robotHeading - pathHeading).wrap()
//        println("Heading Error: $headingError. pathHeading: $pathHeading")

        // heading feed forward
        val headingVelocity = pathSample.rotationalVelocityPerSec!!

        // heading d
        val deltaHeadingError = headingError - prevHeadingError
        prevHeadingError = headingError

        val turnControl = headingVelocity.asDegrees + headingError.asDegrees * parameters.kpHeading * 500.0 + deltaHeadingError.asDegrees * parameters.kdHeading * 500.0
//        println("Turn Control: $turnControl")
        if (turnControl.isNaN() || translationControlField.y.isNaN() || translationControlField.x.isNaN()) {
            println("turnControl: $turnControl")
            println("translationControlField $translationControlField")
            println("dt: $dt")

//            throw IllegalArgumentException("requestedVolts == NaN")
        }

        println("turnOverride: ${turnOverride()?.degrees} ")

        // send it
        driveWithVelocity(Vector2L(translationControlField.y.feet, -translationControlField.x.feet), turnOverride()?.degrees ?: turnControl.degrees, true)

        // are we done yet?
        if (t >= path(t).totalTime + extraTime) {
            println("exiting path")
            stop()
        }
        if (earlyExit(t / (path(t).totalTime + extraTime))) {
            println("early exiting path. time: $t  duration: ${path(t).totalTime} percent complete: ${t / path(t).totalTime}")
            stop()
        }
        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }
    println("at the end of driveAlongChoreoPath")
    MeanLogger.recordOutput("pathPose", Pose2d())

    // shut it down
//    drive(Vector2(0.0, 0.0), 0.0, true)
//    actualRoute.setDoubleArray(doubleArrayOf())
//    plannedPath.setString("")
}

fun SwerveDrive.driveWithVelocity(translationPerSecond: Vector2L, turnPerSecond: Angle, fieldCentric: Boolean = true) {
    var requestedTranslation = translationPerSecond

    if (fieldCentric) {
        requestedTranslation = requestedTranslation.rotateDegrees(-heading.asDegrees)
    }

    if (requestedTranslation.x.asFeet == 0.0 && requestedTranslation.y.asFeet == 0.0 && turnPerSecond.asDegrees == 0.0) {
        return stop()
    }

    val requestedLocalGoals = Array(modules.size) { Vector2L(0.0.feet, 0.0.feet) }
    for (i in modules.indices) {
        requestedLocalGoals[i] = requestedTranslation + (modules[i].modulePosition - robotPivot).perpendicular() * (2.0 * Math.PI) * turnPerSecond.asRotations
    }

    val speeds = Array(modules.size) { 0.0 }

    for (i in modules.indices) {
        val angleAndSpeed = modules[i].calculateAngleAndSpeed(requestedLocalGoals[i].asFeet)
        modules[i].angleSetpoint = angleAndSpeed.angle
        speeds[i] = angleAndSpeed.power
    }

    // adjust wheels to account for velocity of highest speed wheel
    val maxSpeed = speeds.maxByOrNull { it.absoluteValue }!!
    if (maxSpeed > MAXTRANSLATIONSPEED_FEET_PER_SECOND) {
        for (i in speeds.indices) {
            speeds[i] /= maxSpeed
            speeds[i] *= MAXTRANSLATIONSPEED_FEET_PER_SECOND
        }
    }

    for (i in modules.indices) {
        //print("${modules[i].currDistance} ")
        modules[i].setDriveVelocityVoltage(speeds[i].feet.perSecond)
    }

    if (isSim && useMapleSim) {
        val moduleStates = Array(modules.size) { SwerveModuleState() }
        for (i in modules.indices) {
            moduleStates[i] = SwerveModuleState(
                speeds[i].feet.asMeters,
                modules[i].angleSetpoint.wrap().asRotation2d)
        }
        simulatedDrive.runSwerveStates(moduleStates)
    }
    //println()
//    recordOdometry()



}


suspend fun SwerveDrive.driveAlongPathWithStrafe(
    path: Path2D,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    strafeAlpha: (time: Double) -> Double,
    getStrafe: () -> Double,
    earlyExit: () -> Boolean
) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}, reflected ${path.isReflected}")
    if (resetOdometry) {
        println("Position = $position Heading = $heading")
        odometryReset()
        resetHeading()

        // set to the numbers required for the start of the path
        position = path.getPosition(0.0)
        heading = path.getTangent(0.0).angle + path.headingCurve.getValue(0.0).degrees
        println("After Reset Position = $position Heading = $heading")
    }
    var prevTime = 0.0

    val timer = Timer()
    timer.start()
    prevPathPosition = path.getPosition(0.0).feet
    prevPathHeading = path.getAbsoluteHeadingDegreesAt(0.0).degrees
    periodic {
        val t = timer.get()
        val dt = t - prevTime


        // position error
        val pathPosition = path.getPosition(t)
        val positionError = pathPosition - position
        //println("pathPosition=$pathPosition position=$position positionError=$positionError")

        // position feed forward
        val pathVelocity = (pathPosition - prevPathPosition.asFeet) / dt
        prevPathPosition = pathPosition.feet

        val translationControlField =
            pathVelocity * parameters.kPositionFeedForward + positionError * parameters.kpPosition

        // heading error
        val robotHeading = heading
        val pathHeading = path.getAbsoluteHeadingDegreesAt(t).degrees
        val headingError = (pathHeading - robotHeading).wrap()

        // heading feed forward
        val headingVelocity = (pathHeading.asDegrees - prevPathHeading.asDegrees) / dt
        prevPathHeading = pathHeading

        var turnControl =
            headingVelocity * parameters.kHeadingFeedForward + headingError.asDegrees * parameters.kpHeading

        val heading = (heading + (headingRate * parameters.gyroRateCorrection).changePerSecond).wrap()
        val translationControlRobot = translationControlField.rotateDegrees(heading.asDegrees)

        val alpha = strafeAlpha(t)
        if (alpha > 0.0) {
            translationControlRobot.x = translationControlRobot.x * (1.0 - alpha) + getStrafe() * alpha
            turnControl = 0.0
        }

        // send it
        drive(translationControlRobot, turnControl, false)

        // are we done yet?
        if (t >= path.durationWithSpeed + extraTime)
            stop()

        if (earlyExit()) {
            stop()
        }

        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }

    // shut it down
    drive(Vector2(0.0, 0.0), 0.0, true)
}

// remember to use your own drive subsystem
suspend fun SwerveDrive.tuneDrivePositionController(controller: org.team2471.frc.lib.input.XboxController) {
//    var prevX = 0.0
//    var prevY = 0.0
//    var prevTime = 0.0
    var prevPositionError = Vector2(0.0, 0.0)
    var prevHeadingError = 0.0.degrees

    //val timer = Timer().apply { start() }

//    var angleErrorAccum = 0.0.degrees
    try {
        odometryReset()
        resetHeading()
        periodic {
           // val t = timer.get()
//            val dt = t - prevTime

            val x = -controller.leftThumbstickY
            val y = -controller.leftThumbstickX
            val turn = 75.0*controller.rightThumbstickX

            // position error
            val pathPosition = Vector2(x, y)
            val positionError = pathPosition - position

            MeanLogger.recordOutput("position", position.feet, heading)
            MeanLogger.recordOutput("goalPosition", Pose2d(pathPosition.feet.asMeters.toTranslation2d(), Rotation2d(turn.degrees.asRadians)))
            MeanLogger.recordOutput("positionError", Pose2d(positionError.feet.asMeters.toTranslation2d(), Rotation2d(turn.degrees.asRadians)))

            // position d
            val deltaPositionError = positionError - prevPositionError
            prevPositionError = positionError

            val translationControlField = positionError * parameters.kpPosition * 14.5 + deltaPositionError * parameters.kdPosition * 0.0

            // heading error
            val robotHeading = heading.asDegrees
            val pathHeading = turn.degrees
            val headingError = (pathHeading - robotHeading.degrees).wrap()
//            println("Heading Error: $headingError. Hi.")

            // heading d
            val deltaHeadingError = headingError - prevHeadingError
            prevHeadingError = headingError

            val turnControl = headingError.asDegrees * parameters.kpHeading * 500.0 + deltaHeadingError.asDegrees * parameters.kdHeading * 500.0

//            println("Error ${headingError.asDegrees}, setpoint ${pathHeading}, current pos $robotHeading")
            println("Path Position $pathPosition positionError $positionError")
            driveWithVelocity(translationControlField.feet * MAXTRANSLATIONSPEED_FEET_PER_SECOND, 0.0.degrees/*(-turnControl * MAXHEADINGSPEED_DEGREES_PER_SECOND).degrees*/, true)

           // prevTime = t
        }
    } finally {
        stop()
    }
}

suspend fun SwerveDrive.driveToPoint(
    point: Vector2L,
    heading: Angle? = null,
    stopMovingDeadband: Length = 0.0.inches,
    posSupplier: () -> Vector2L = {this.position.feet},
    exitSupplier: (elapsedTime: Double, error: Vector2L, headingError: Angle?) -> Boolean = {seconds, error, headingError -> error.length < 0.5.feet && (headingError == null || headingError < 3.0.degrees)},
    turnOverride: () -> Double? = {null},
) {
    println("driving to point $point")
    MeanLogger.recordOutput("driveToPoint Point", point.asMeters.toPose2d(heading ?: this.heading))

    var prevPosition = posSupplier.invoke()
    var prevPositionError = Vector2L.Zeros

    val t = Timer()
    t.start()

    periodic {
        val currentPosition = posSupplier.invoke()
        val positionError = currentPosition - point
        val velocity = velocity.feet
        prevPosition = currentPosition
        val deltaPositionError = positionError - prevPositionError
        prevPositionError = positionError

        val staticFriction = if (positionError.length > 0.0.inches) { positionError.normalize() * 0.02 } else Vector2(0.0, 0.0).inches
        val translation = velocity * parameters.kPositionFeedForward + positionError * parameters.kpPosition + deltaPositionError * parameters.kdPosition + staticFriction

        val turnControl: Double
        var headingError: Angle? = null
        if (heading!=null) {
            // heading error
            val robotHeading = heading
            val pathHeading = heading
            headingError = (robotHeading - pathHeading).wrap()
            // heading d
            val deltaHeadingError = headingError - prevHeadingError
            prevHeadingError = headingError
            turnControl = headingError.asDegrees * parameters.kpHeading + deltaHeadingError.asDegrees * parameters.kdHeading
        } else {
            turnControl = turnOverride() ?: 0.0
        }

        if (positionError.length > stopMovingDeadband) {
            drive(
                Vector2(-translation.x.asFeet, -translation.y.asFeet),
                turnControl,
                fieldCentric = true
            )
        } else {
            drive(Vector2(0.0, 0.0), 0.0)
        }
        MeanLogger.recordOutput("driveToPoint PositionError", positionError.length.asInches)

        if (exitSupplier(t.get(), positionError, headingError)) {
            println("drive to point exit supplier return true. time: ${t.get()} error: $prevPositionError headingError: $headingError")
            MeanLogger.recordOutput("driveToPoint Point", Pose2d())
            drive(Vector2(0.0, 0.0), 0.0)
            stop()
        }
    }
}

suspend fun SwerveDrive.driveToNearestPoint(points: List<Vector2L>, posSupplier: () -> Vector2L, exitSupplier: (Double, Vector2L, Angle?) -> Boolean, turnOverride: () -> Double? = {null},) {
    MeanLogger.recordOutput("Goal Pos", poseEstimator.latestPos.asFeet.getClosestPoint(*(points.map { it.asFeet }).toTypedArray()).feet.asMeters.toPose2d(heading))
    this.driveToPoint(poseEstimator.latestPos.asFeet.getClosestPoint(*(points.map { it.asFeet }).toTypedArray()).feet, posSupplier = posSupplier, exitSupplier = exitSupplier, turnOverride = turnOverride)
    MeanLogger.recordOutput("Goal Pos", Pose2d())
}

fun SwerveDrive.xPose() {
    modules[0].angleSetpoint = -45.0.degrees
    modules[1].angleSetpoint = 45.0.degrees
    modules[2].angleSetpoint = -45.0.degrees
    modules[3].angleSetpoint = 45.0.degrees
}