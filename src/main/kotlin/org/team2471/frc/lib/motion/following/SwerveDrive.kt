package org.team2471.frc.lib.motion.following

import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

private var prevPosition = Vector2(0.0, 0.0)
private var prevPathPosition = Vector2(0.0, 0.0)
private var prevTime = 0.0
private var prevPathHeading = 0.0.radians

interface SwerveDrive {
    val parameters: SwerveParameters
    var heading: Angle
    val headingRate: AngularVelocity
    var position: Vector2
    var velocity: Vector2
    var robotPivot: Vector2 // location of rotational pivot in robot coordinates

    val modules: Array <Module>

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    interface Module {
        // module fixed parameters
        val modulePosition: Vector2 // coordinates of module in robot coordinates
        val angleOffset: Angle

        // encoder interface
        val angle: Angle
        val speed: Double
        val currDistance: Double
        var prevDistance: Double

        // motor interface
        var angleSetpoint: Angle
        fun setDrivePower(power: Double)

        fun stop()
        fun zeroEncoder()
        fun driveWithDistance(angle: Angle, distance: Length)
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

// temporary to measure maximum turn speed
fun SwerveDrive.drive(translation: Vector2, turn: Double, fieldCentric: Boolean = true) {
    recordOdometry()

    if (translation.x == 0.0 && translation.y == 0.0 && turn == 0.0) {
        return stop()
    }

    val adjustedTranslation = if (fieldCentric) {
        val heading = (heading + (headingRate * parameters.gyroRateCorrection).changePerSecond).wrap()
        translation.rotateRadians(heading.asRadians)
    } else {
        translation
    }

    val speeds = Array(modules.size) { 0.0 }

    for (i in 0 until modules.size) {
        speeds[i] = modules[i].calculateAngleReturnSpeed(adjustedTranslation, turn, robotPivot)
    }

    val maxSpeed = speeds.maxBy(Math::abs)!!
    if (maxSpeed > 1.0) {
        for (i in 0 until speeds.size) {
            speeds[i] /= maxSpeed
        }
    }

    for (i in 0 until modules.size) {
        modules[i].setDrivePower(speeds[i])
    }
}

private fun SwerveDrive.Module.calculateAngleReturnSpeed(
    translation: Vector2,
    turn: Double,
    robotPivot: Vector2
) : Double {
    val localGoal = translation + (modulePosition - robotPivot).perpendicular().normalize() * turn
    var power = localGoal.length
    var setPoint = localGoal.angle.radians
    val angleError = (setPoint - angle).wrap()
    if (Math.abs(angleError.asRadians) > Math.PI / 2.0) {
        setPoint -= Math.PI.radians
        power = -power
    }
    angleSetpoint = setPoint
    return power
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

fun SwerveDrive.recordOdometry() {
    var translation = Vector2(0.0, 0.0)

    val translations: Array<Vector2> = Array(modules.size) { Vector2(0.0, 0.0) }
    for (i in 0 until modules.size) {
        translations[i] = modules[i].recordOdometry(heading)
    }

    for (i in 0 until modules.size) {
        translation += translations[i]
    }
    translation /= modules.size.toDouble()

    position += translation
    val time = System.currentTimeMillis().toDouble()/1000.0
    val deltaTime = time - prevTime
    velocity = (position - prevPosition)/deltaTime
    prevTime = time
    prevPosition = position
}

fun SwerveDrive.Module.recordOdometry(heading: Angle): Vector2 {
    val angleInFieldSpace = heading + angle
    val deltaDistance = currDistance - prevDistance
    prevDistance = currDistance
    return Vector2(
        deltaDistance * sin(angleInFieldSpace.asRadians),
        deltaDistance * cos(angleInFieldSpace.asRadians)
    )
}

fun SwerveDrive.resetOdometry() {
    for (module in modules) {
        module.prevDistance = 0.0
    }
    zeroEncoders()
    position = Vector2(0.0, 0.0)
    heading = 0.0.degrees
}

suspend fun SwerveDrive.driveAlongPath(
    path: Path2D,
    resetOdometry: Boolean = false,
    extraTime: Double = 0.0
) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")

    if (resetOdometry) {
        println("Position = $position Heading = $heading")
        resetOdometry()

        // set to the numbers required for the start of the path
        position = path.getPosition(0.0)
        heading = path.getTangent(0.0).angle.degrees + path.headingCurve.getValue(0.0).degrees
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
        val pathVelocity = (pathPosition - prevPathPosition)/dt
        prevPathPosition = pathPosition

        val translationControlField = pathVelocity * parameters.kPositionFeedForward + positionError * parameters.kPosition

        // heading error
        val robotHeading = heading
        val pathHeading = path.getAbsoluteHeadingDegreesAt(t).degrees
        val headingError = (pathHeading - robotHeading).wrap()
        println("RobotHeading=$robotHeading PathHeading=$pathHeading HeadingError=$headingError")

        // heading feed forward
        val headingVelocity = (pathHeading.asDegrees - prevPathHeading.asDegrees)/dt
        prevPathHeading = pathHeading

        val turnControl = headingVelocity * parameters.kHeadingFeedForward + headingError.asDegrees * parameters.kHeading

        // send it
        drive(translationControlField, turnControl, true)

        // are we done yet?
        if (t >= path.durationWithSpeed + extraTime)
            stop()

        prevTime = t

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
    }

    // shut it down
    drive(Vector2(0.0,0.0), 0.0, true)
}
