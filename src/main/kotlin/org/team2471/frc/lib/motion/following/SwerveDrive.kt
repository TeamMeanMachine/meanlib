package org.team2471.frc.lib.motion.following

import com.team254.lib.util.Interpolable
import com.team254.lib.util.InterpolatingDouble
import com.team254.lib.util.InterpolatingTreeMap
import edu.wpi.first.networktables.NetworkTableEntry
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import kotlin.math.*
private val poseHistory = InterpolatingTreeMap<InterpolatingDouble, SwerveDrive.Pose>(75)
private var prevPosition = Vector2(0.0, 0.0)
private var prevPose = SwerveDrive.Pose(Vector2(0.0, 0.0), 0.0.degrees)
private var prevPathPosition = Vector2(0.0, 0.0)
private var prevTime = 0.0
private var prevPathHeading = 0.0.radians
private val MAXHEADINGSPEED_DEGREES_PER_SECOND = 600.0
private val MAXTRANSLATIONSPEED_FEET_PER_SECOND = 15.0

private var prevHeadingError = 0.0.degrees

interface SwerveDrive {
    val parameters: SwerveParameters
    var heading: Angle
    val headingRate: AngularVelocity
    var position: Vector2
    val combinedPosition: Vector2
    var velocity: Vector2
    var robotPivot: Vector2 // location of rotational pivot in robot coordinates
    var headingSetpoint: Angle
    val carpetFlow: Vector2
    val kCarpet: Double
    val kTread: Double
    val plannedPath: NetworkTableEntry

    val modules: Array<Module>

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    fun poseUpdate(pose: Pose) = Unit

    fun resetOdom() = Unit

    interface Module {
        // module fixed parameters
        val modulePosition: Vector2 // coordinates of module in robot coordinates
        val angleOffset: Angle

        // encoder interface
        val angle: Angle
        val speed: Double
        val currDistance: Double
        var prevDistance: Double
        val treadWear: Double
        var odometer: Double

        // motor interface
        var angleSetpoint: Angle

        fun setDrivePower(power: Double)

        fun stop()
        fun zeroEncoder()
        fun driveWithDistance(angle: Angle, distance: Length)
    }

    companion object {
        var prevTranslationInput = Vector2(0.0,0.0)
        var prevTurn = 0.0
    }

    data class Pose(val position: Vector2, val heading: Angle) : Interpolable<Pose> {
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
fun SwerveDrive.lookupPose(time: Double): SwerveDrive.Pose? = poseHistory.getInterpolated(InterpolatingDouble(time))

fun SwerveDrive.poseDiff(latency: Double): SwerveDrive.Pose? {
    val currPose = pose
    val previousPose = lookupPose( Timer.getFPGATimestamp().minus(latency))
    return if (previousPose == null) {
        null
    } else {
        SwerveDrive.Pose(currPose.position - previousPose.position, currPose.heading - previousPose.heading)
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
    teleopClosedLoopHeading: Boolean = false,
    softTranslation: Vector2 = Vector2(0.0, 0.0),
    softTurn: Double = 0.0,
    maxChangeInOneFrame: Double = 0.0)
{
    var requestedTranslation = translation

    if (fieldCentric) {
        requestedTranslation = requestedTranslation.rotateDegrees(heading.asDegrees)
    }
    requestedTranslation += softTranslation

    if (!SmartDashboard.containsKey("DemoSpeed")) SmartDashboard.setDefaultNumber("DemoSpeed", 1.0)
    requestedTranslation *= demoSpeed

    var requestedTurn = turn + softTurn

    requestedTurn *= demoSpeed

    if (requestedTranslation.length > 0.01 && requestedTurn.absoluteValue < 0.01) {
        if (teleopClosedLoopHeading) {  // closed loop on heading position
            // heading error
            val headingError = (headingSetpoint - heading).wrap()
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
        requestedLocalGoals[i] = requestedTranslation + (modules[i].modulePosition - robotPivot).perpendicular().normalize() * requestedTurn
    }

    val speeds = Array(modules.size) { 0.0 }

    // the beginning of Bryce's idea of how to drive more smoothly, but maintain responsiveness
    // directional and rotational interpolation from robot state towards joystick request
    if (maxChangeInOneFrame > 0.0) {
        val normalizedRobotDirection = velocity.rotateDegrees(heading.asDegrees) / MAXTRANSLATIONSPEED_FEET_PER_SECOND
        val translateDelta = requestedTranslation - normalizedRobotDirection
        val length1 = min(translateDelta.length, maxChangeInOneFrame)
        val interpolatedTranslation = normalizedRobotDirection + translateDelta.normalize() * length1

        val normalizedTurnRate = headingRate.changePerSecond.asDegrees / MAXHEADINGSPEED_DEGREES_PER_SECOND
        val turnDelta = requestedTurn - normalizedTurnRate
        val length2 = min(turnDelta, maxChangeInOneFrame)
        val interpolatedTurn = normalizedTurnRate + turnDelta.sign * length2

        for (i in modules.indices) {
            val interpolatedLocalGoal = interpolatedTranslation + (modules[i].modulePosition - robotPivot).perpendicular().normalize() * interpolatedTurn
            val bHat = interpolatedLocalGoal.normalize()
            val projectedLocalGoal = bHat * requestedLocalGoals[i].dot(bHat)
            val angleAndSpeed = modules[i].calculateAngleAndSpeed(projectedLocalGoal)
            modules[i].angleSetpoint = angleAndSpeed.angle
            speeds[i] = angleAndSpeed.power
        }
    }
    else {
        for (i in modules.indices) {
            val angleAndSpeed = modules[i].calculateAngleAndSpeed(requestedLocalGoals[i])
            modules[i].angleSetpoint = angleAndSpeed.angle
            speeds[i] = angleAndSpeed.power
        }
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
    recordOdometry()
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

fun SwerveDrive.Module.recordOdometry(heading: Angle, carpetFlow: Vector2, kCarpet: Double, kTread: Double): Vector2 {
    val angleInFieldSpace = heading + angle
    val wheelDir = Vector2(angleInFieldSpace.sin(), angleInFieldSpace.cos())
    var signedWheelDir = wheelDir

    val holdDistance = currDistance
    var deltaDistance = (holdDistance - prevDistance)
    if (deltaDistance < 0.0) {
        signedWheelDir *= -1.0
    }
    deltaDistance *= (1.0 + signedWheelDir.dot(carpetFlow) * kCarpet) * ((1.0 - kTread) + (kTread * treadWear))
    //println("wheelDir = ${wheelDir} carpetFlow = ${carpetFlow} dot = ${wheelDir.dot(carpetFlow)}")
//    if (deltaDistance.absoluteValue < 1.0) {
        prevDistance = holdDistance
        return wheelDir * deltaDistance
//    } else {
//        println("TOO MUCH MOVEMENT")
//        return Vector2(0.0,0.0)
//    }
}

fun SwerveDrive.recordOdometry() {
    var translation = Vector2(0.0, 0.0)

    val translations: Array<Vector2> = Array(modules.size) { Vector2(0.0, 0.0) }
    for (i in modules.indices) {
        translations[i] = modules[i].recordOdometry(heading, carpetFlow,kCarpet, kTread)
        modules[i].odometer += translations[i].length

    }

    for (i in modules.indices) {
        translation += translations[i]
    }
    translation /= modules.size.toDouble()

    position += translation
    val time = Timer.getFPGATimestamp()
    val deltaTime = time - prevTime
    velocity = (position - prevPosition) / deltaTime
    val poseDifference = SwerveDrive.Pose(pose.position - prevPose.position, pose.heading - prevPose.heading)
    poseUpdate(poseDifference)
    poseHistory[InterpolatingDouble(time)] = pose
    prevTime = time
    prevPosition = position
    prevPose = pose
}

fun SwerveDrive.resetOdometry() {
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
    earlyExit: () -> Boolean = {false}
    ) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")
    if (inResetGyro ?: resetOdometry) {
        println("Heading = $heading")
        resetHeading()
        heading = path.headingCurve.getValue(0.0).degrees
        if(parameters.alignRobotToPath) {
            heading += path.getTangent(0.0).angle
        }
        println("After Reset Heading = $heading")
    }
    
    if (resetOdometry) {
        println("Position = $position")
        resetOdometry()

        // set to the numbers required for the start of the path
        position = path.getPosition(0.0)
        resetOdom()
        println("After Reset Position = $position")
    }

    plannedPath.setString(path.toJsonString())


    var prevTime = 0.0

    val timer = Timer()
    timer.start()
    prevPathPosition = path.getPosition(0.0)
    prevPathHeading = path.getAbsoluteHeadingDegreesAt(0.0).degrees
    var prevPositionError = Vector2(0.0, 0.0)
    prevHeadingError = 0.0.degrees
    periodic {
        val t = timer.get()
        val dt = t - prevTime

        // position error
        val pathPosition = path.getPosition(t)
        val positionError = pathPosition - combinedPosition
//        println("time=$t   pathPosition=$pathPosition position=$position positionError=$positionError")

        // position feed forward
        val pathVelocity = (pathPosition - prevPathPosition) / dt
        prevPathPosition = pathPosition

        // position d
        val deltaPositionError = positionError - prevPositionError
        prevPositionError = positionError

        val translationControlField =
            pathVelocity * parameters.kPositionFeedForward + positionError * parameters.kpPosition + deltaPositionError * parameters.kdPosition

        // heading error
        val robotHeading = heading
        val pathHeading = path.getAbsoluteHeadingDegreesAt(t).degrees
        val headingError = (pathHeading - robotHeading).wrap()
        //println("Heading Error: $headingError. Hi. %%%%%%%%%%%%%%%%%%%%%%%%%%")

        // heading feed forward
        val headingVelocity = (pathHeading.asDegrees - prevPathHeading.asDegrees) / dt
        prevPathHeading = pathHeading

        // heading d
        val deltaHeadingError = headingError - prevHeadingError
        prevHeadingError = headingError

        val turnControl = headingVelocity * parameters.kHeadingFeedForward + headingError.asDegrees * parameters.kpHeading + deltaHeadingError.asDegrees * parameters.kdHeading

        // send it
        drive(translationControlField, turnControl, true)

        // are we done yet?
        if (t >= path.durationWithSpeed + extraTime || earlyExit())
            stop()

        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }

    // shut it down
    drive(Vector2(0.0, 0.0), 0.0, true)
}


suspend fun SwerveDrive.driveAlongPathWithStrafe(
    path: Path2D,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0,
    strafeAlpha: (time: Double) -> Double,
    getStrafe: () -> Double,
    earlyExit: () -> Boolean
) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")
    if (resetOdometry) {
        println("Position = $position Heading = $heading")
        resetOdometry()
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
        var translationControlRobot = translationControlField.rotateDegrees(heading.asDegrees)

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
        resetOdometry()
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