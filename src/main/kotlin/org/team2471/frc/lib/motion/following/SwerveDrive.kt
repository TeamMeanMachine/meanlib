package org.team2471.frc.lib.motion.following

import com.team254.lib.util.Interpolable
import com.team254.lib.util.InterpolatingDouble
import com.team254.lib.util.InterpolatingTreeMap
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import org.team2471.frc.lib.util.getRealFPGATimestamp
import org.team2471.frc.lib.util.isReal
import kotlin.math.*
private val poseHistory = InterpolatingTreeMap<InterpolatingDouble, SwerveDrive.Pose>(75)
private var prevPosition = Vector2(0.0, 0.0)
private var prevPose = SwerveDrive.Pose(Vector2(0.0, 0.0), 0.0.degrees)
private var prevPathPosition = Vector2(0.0, 0.0)
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
    }

    interface Module {
        // module fixed parameters
        val modulePosition: Vector2L // coordinates of module in robot coordinates
        val angleOffset: Angle

        // encoder interface
        val angle: Angle
        val speed: Double
        val acceleration: Double
        val currDistance: Double
        var prevDistance: Double
        val treadWear: Double
        var odometer: Double

        var prevAngle: Angle

        // motor interface
        var angleSetpoint: Angle

        fun setDrivePower(power: Double)

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
}

val SwerveDrive.pose: SwerveDrive.Pose
    get() = SwerveDrive.Pose(position, heading)
val SwerveDrive.demoMode: Boolean
    get() = demoSpeed < 1.0
val SwerveDrive.demoSpeed: Double
    get() = SmartDashboard.getNumber("DemoSpeed" , 1.0).coerceIn(0.0, 1.0)
fun SwerveDrive.lookupPose(time: Double): SwerveDrive.Pose? = if (time < lastResetTime) SwerveDrive.Pose(position, heading) else poseHistory.getInterpolated(InterpolatingDouble(time))

fun SwerveDrive.poseDiff(latency: Double): SwerveDrive.Pose? {
    val currPose = pose
    val previousPose = lookupPose( getRealFPGATimestamp().minus(latency))
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
    softTurn: Double = 0.0)
{
    var requestedTranslation = Vector2(translation.x, translation.y)

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
    //println()
//    recordOdometry()
}

data class AngleAndSpeed(val angle: Angle, val power: Double)

private fun SwerveDrive.Module.calculateAngleAndSpeed(localGoal : Vector2) : AngleAndSpeed {

    var power = localGoal.length
    var setPoint = localGoal.angle
    val angleError = (setPoint - angle).wrap()
    if (Math.abs(angleError.asRadians) > Math.PI / 2.0) {
        setPoint -= Math.PI.radians
        power = -power
    }
    return AngleAndSpeed(setPoint, power * Math.abs(angleError.cos()))
}

suspend fun SwerveDrive.Module.steerToAngle(angle: Angle, tolerance: Angle = 2.degrees) {
    try {
        periodic(watchOverrun = false) {
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
    val angleInFieldSpace = if (gyroConnected) heading - moduleAngle else (heading - prevAngle) + (moduleAngle - prevAngle)  //prevAngleInFieldSpace + deltaAngle
    val wheelDir = Vector2(angleInFieldSpace.cos(), angleInFieldSpace.sin())
    var signedWheelDir = wheelDir

    val holdDistance = currDistance
    var deltaDistance = (holdDistance - prevDistance)
    if (deltaDistance < 0.0) {
        signedWheelDir *= -1.0
    }

    val accelDir = wheelDir * acceleration.sign

    if (modulePosition.x > 0.0.inches && modulePosition.y > 0.0.inches) { // println("acceleration: ${(requestedSpeed - currentSpeed).round(3)} carpetFactor ${(accelerationVector.dot(carpetFlow))}")
//        println("accelDir: $accelDir   finalCalc: ${(1.0 + accelDir.dot(carpetFlow) * kCarpet)}")
//        println("value: ${(1.0 + signedWheelDir.dot(carpetFlow) * kCarpet) * treadWear}")

    }
    if (isReal) {
//        deltaDistance *= (1.0 + accelDir.dot(carpetFlow) * kCarpet) * treadWear //acceleration based kCarpet
        deltaDistance *= (1.0 + signedWheelDir.dot(carpetFlow) * kCarpet) * treadWear //direction based kCarpet
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
    for (i in modules.indices) {
        val moduleState = modules[i].recordOdometry(heading, carpetFlow, kCarpet, gyroConnected)

        val translation = moduleState.translation
        modules[i].odometer += translation.length

        val modulePosition = modules[i].modulePosition.asFeet.mirrorYAxis().flipXAndY().rotate(heading)
        val deltaAngle = ((translation + modulePosition).angle - modulePosition.angle).wrap() //calculate robot rotation using swerve translation

        val numberOfModules = modules.size.toDouble()
        robotRotation += deltaAngle / numberOfModules
        robotTranslation += translation / numberOfModules
        robotVelocity += moduleState.velocity / numberOfModules
        robotAcceleration += moduleState.acceleration / numberOfModules
    }

    position += Vector2(robotTranslation.x, robotTranslation.y)
    deltaPos = Vector2L(robotTranslation.x.feet, robotTranslation.y.feet)
    velocity = robotVelocity
    acceleration = robotAcceleration
    if (!gyroConnected) { //if gyro is not connected, update heading
        heading += robotRotation
        headingRate = (robotRotation / dt).perSecond
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
    turnOverride: () -> Double? = {null},
    earlyExit: (percentComplete: Double) -> Boolean = {false}
    ) {

//    val gson = Gson()

    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, reflected ${path.isReflected}, turnOverride ${turnOverride() != null}")
    if (inResetGyro ?: resetOdometry) {
        println("Heading = $heading")
        resetHeading()
        heading = path.getAbsoluteHeadingDegreesAt(0.0).degrees //path.headingCurve.getValue(0.0).degrees
        if(parameters.alignRobotToPath) {
            heading += path.getTangent(0.0).angle
        }
        println("After Reset Heading = $heading")
    }
    
    if (resetOdometry) {
        println("Position = $position")
        odometryReset()
        println("Position after odometryReset = $position")

        // set to the numbers required for the start of the path
        position = path.getPosition(0.0)
        prevPosition = position

//        resetOdom()
        println("After Reset Position = $position")
    }

//    plannedPath.setString(gson.toJson(path))


    var prevTime = 0.0

    val timer = Timer()
    timer.start()
    prevPathPosition = path.getPosition(0.0)
    prevPathHeading = path.getAbsoluteHeadingDegreesAt(0.0).degrees
    var prevPositionError = Vector2(0.0, 0.0)
    prevHeadingError = 0.0.degrees
    suspendUntil(10) { timer.get() != 0.0}
    println("entering drive periodic")
    periodic {
        val t = timer.get()
        val dt = t - prevTime

        // position error
        val pathPosition = path.getPosition(t)
        val currentPosition = position.feet
        val positionError = pathPosition - currentPosition.asFeet
//        println("time=$t   dt=$dt    pathPosition=$pathPosition position=$position positionError=$positionError")

        // position feed forward
        val pathVelocity = (pathPosition - prevPathPosition) / dt
        prevPathPosition = pathPosition

        // position d
        val deltaPositionError = positionError - prevPositionError
        prevPositionError = positionError

        var translationControlField =
            pathVelocity * parameters.kPositionFeedForward + positionError * parameters.kpPosition + deltaPositionError * parameters.kdPosition

        translationControlField = Vector2(-translationControlField.y, translationControlField.x)
//        println("translationControlField = $translationControlField")


        // heading error
        val robotHeading = heading
        val pathHeading = path.getAbsoluteHeadingDegreesAt(t).degrees
        val headingError = (robotHeading - pathHeading).wrap()
//        println("Heading Error: $headingError. pathHeading: $pathHeading")

        // heading feed forward
        val headingVelocity = (pathHeading.asDegrees - prevPathHeading.asDegrees) / dt
        prevPathHeading = pathHeading

        // heading d
        val deltaHeadingError = headingError - prevHeadingError
        prevHeadingError = headingError

//        actualRoute.setDoubleArray(doubleArrayOf(t, currentPosition.x.asFeet, currentPosition.y.asFeet, robotHeading.asDegrees))

        val turnControl = headingVelocity * parameters.kHeadingFeedForward + headingError.asDegrees * parameters.kpHeading + deltaHeadingError.asDegrees * parameters.kdHeading
//        println("Turn Control: $turnControl")
        if (turnControl.isNaN() || translationControlField.y.isNaN() || translationControlField.x.isNaN()) {
            println("turnControl: $turnControl")
            println("translationControlField $translationControlField")
            println("dt: $dt")


//            throw IllegalArgumentException("requestedVolts == NaN")
        }

        // send it
        drive(translationControlField, turnOverride() ?: turnControl, true)

        // are we done yet?
        if (t >= path.durationWithSpeed + extraTime) {
            println("exiting path")
            stop()
        }
        if (earlyExit(t / path.durationWithSpeed)) {
            println("early exiting path. time: $t  duration: ${path.durationWithSpeed} percent complete: ${t / path.durationWithSpeed}")
            stop()
        }
        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }
    println("at the end of driveAlongPath")

    // shut it down
    drive(Vector2(0.0, 0.0), 0.0, true)
//    actualRoute.setDoubleArray(doubleArrayOf())
//    plannedPath.setString("")
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
    prevPathPosition = path.getPosition(0.0)
    prevPathHeading = path.getAbsoluteHeadingDegreesAt(0.0).degrees
    periodic {
        val t = timer.get()
        val dt = t - prevTime


        // position error
        val pathPosition = path.getPosition(t)
        val positionError = pathPosition - position
        //println("pathPosition=$pathPosition position=$position positionError=$positionError")

        // position feed forward
        val pathVelocity = (pathPosition - prevPathPosition) / dt
        prevPathPosition = pathPosition

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

            val x = controller.leftThumbstickX
            val y = controller.leftThumbstickY
            val turn = 75.0*controller.rightThumbstickX

            // position error
            val pathPosition = Vector2(x, y)
            val positionError = pathPosition - position

            // position d
            val deltaPositionError = positionError - prevPositionError
            prevPositionError = positionError

            val translationControlField = positionError * parameters.kpPosition + deltaPositionError * parameters.kdPosition

            // heading error
            val robotHeading = heading.asDegrees
            val pathHeading = turn.degrees
            val headingError = (pathHeading - robotHeading.degrees).wrap()
//            println("Heading Error: $headingError. Hi.")

            // heading d
            val deltaHeadingError = headingError - prevHeadingError
            prevHeadingError = headingError

            val turnControl = headingError.asDegrees * parameters.kpHeading + deltaHeadingError.asDegrees * parameters.kdHeading

            println("Error ${headingError.asDegrees}, setpoint ${pathHeading}, current pos $robotHeading")
            drive(translationControlField, turnControl, true)

           // prevTime = t
        }
    } finally {
        stop()
    }
}

fun SwerveDrive.xPose() {
    modules[0].angleSetpoint = -45.0.degrees
    modules[1].angleSetpoint = 45.0.degrees
    modules[2].angleSetpoint = -45.0.degrees
    modules[3].angleSetpoint = 45.0.degrees
}