package org.team2471.frc.lib.motion.following

import kotlinx.coroutines.withTimeout
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.Timer
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

interface SwerveDrive {
    val parameters: SwerveParameters
    var heading: Angle
    val headingRate: AngularVelocity
    var position: Vector2
    var prevPosition: Vector2
    var prevTime: Double
    var velocity: Vector2
    var prevPathPosition: Vector2

    val frontLeftModule: Module
    val frontRightModule: Module
    val backLeftModule: Module
    val backRightModule: Module

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    interface Module {
        val angle: Angle
        val speed: Double
        val currentDistance: Double
        var previousDistance: Double

        fun drive(angle: Angle, power: Double)

        fun driveWithDistance(angle: Angle, distance: Length)

        fun stop()

        fun zeroEncoder()
    }
}

fun SwerveDrive.stop() {
    frontLeftModule.stop()
    frontRightModule.stop()
    backLeftModule.stop()
    backRightModule.stop()
}

fun SwerveDrive.zeroEncoders() {
    frontLeftModule.zeroEncoder()
    frontRightModule.zeroEncoder()
    backLeftModule.zeroEncoder()
    backRightModule.zeroEncoder()
    position = Vector2(0.0, 0.0)
}

fun SwerveDrive.drive(translation: Vector2, turn: Double, fieldCentric: Boolean = true) {
    if (translation.x == 0.0 && translation.y == 0.0 && turn == 0.0) {
        return stop()
    }
    val heading = (heading + (headingRate * parameters.gyroRateCorrection).changePerSecond).wrap()
    recordOdometry()

    translation.let { (x, y) ->
        translation.x = -y * heading.sin() + x * heading.cos()
        translation.y = y * heading.cos() + x * heading.sin()
    }

    val a = translation.x - turn * parameters.lengthComponent
    val b = translation.x + turn * parameters.lengthComponent
    val c = translation.y - turn * parameters.widthComponent
    val d = translation.y + turn * parameters.widthComponent

    val speeds = doubleArrayOf(
        Math.hypot(b, d),
        Math.hypot(b, c),
        Math.hypot(a, d),
        Math.hypot(a, c)
    )

    val turns = arrayOf(
        Angle.atan2(b, d),
        Angle.atan2(b, c),
        Angle.atan2(a, d),
        Angle.atan2(a, c)
    )

    val maxSpeed = speeds.max()!!
    if (maxSpeed > 1.0) {
        for (i in 0..3) {
            speeds[i] /= maxSpeed
        }
    }

    val angles = arrayOf(frontLeftModule.angle, frontRightModule.angle, backLeftModule.angle, backRightModule.angle)

    for (i in 0..3) {
        val angleError = (turns[i] - angles[i]).wrap()
        if (Math.abs(angleError.asRadians) > Math.PI / 2.0) {
            turns[i] -= Math.PI.radians
            speeds[i] = -speeds[i]
        }
    }

    frontLeftModule.drive(turns[0], speeds[0])
    frontRightModule.drive(turns[1], speeds[1])
    backLeftModule.drive(turns[2], speeds[2])
    backRightModule.drive(turns[3], speeds[3])
}

suspend fun SwerveDrive.Module.steerToAngle(angle: Angle, tolerance: Angle = 2.degrees) {
    try {
        periodic(watchOverrun = false) {
            drive(angle, 0.0)

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
    val v0 = frontLeftModule.recordOdometry(heading)
    val v1 = frontRightModule.recordOdometry(heading)
    val v2 = backLeftModule.recordOdometry(heading)
    val v3 = backRightModule.recordOdometry(heading)
//    println("fl=$v0 fr=$v1 bl=$v2 br=$v3")
    translation += v0
    translation += v1
    translation += v2
    translation += v3
    translation /= 4.0
    position += translation
    val time = System.currentTimeMillis().toDouble()/1000.0
    val deltaTime = time - prevTime
    velocity = (position - prevPosition)/deltaTime
    prevTime = time
    prevPosition = position
}

fun SwerveDrive.Module.recordOdometry(heading: Angle): Vector2 {
    val angleInFieldSpace = heading + angle
    val deltaDistance = currentDistance - previousDistance
    previousDistance = currentDistance
    return Vector2(
        deltaDistance * sin(angleInFieldSpace.asRadians),
        deltaDistance * cos(angleInFieldSpace.asRadians)
    )
}

suspend fun SwerveDrive.driveAlongPath(path: Path2D, extraTime: Double = 0.0, resetOdometry: Boolean = false) {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")

    if (resetOdometry) {
        zeroEncoders()
        position = path.getPosition(0.0)
        heading = path.getTangent(0.0).angle.degrees + path.headingCurve.getValue(0.0).degrees
    }
    var prevTime = 0.0

    val timer = Timer()
    timer.start()
    var finished = false
    prevPathPosition = path.getPosition(0.0)
    periodic() {
        val t = timer.get()
        val dt = t - prevTime


        // position error
        val pathPosition = path.getPosition(t)
        val positionError = pathPosition - position

        // velocity feed forward
        val pathVelocity = (pathPosition - prevPathPosition)/dt
        //println("pathPosition=$pathPosition position=$position positionError=$positionError")
        prevPathPosition = pathPosition

        val translationControlField = pathVelocity * parameters.kFeedForward + positionError * parameters.kPosition
        //val translationControlRobot = translationControlField.rotateDegrees(heading.asDegrees)

        // apply gyro corrections
        val gyroAngle = heading
        val pathAngle = path.getTangent(t).angle + path.headingCurve.getValue(t)
        val angleError = pathAngle - windRelativeAngles(pathAngle, gyroAngle.asDegrees)

        val turnControl = angleError * parameters.kTurn

        drive(translationControlField, turnControl, true)

        if (t >= path.durationWithSpeed + extraTime)
            stop()

//        println("Time=$t Path Position=$pathPosition Position=$position")
//        println("DT$dt Path Velocity = $pathVelocity Velocity = $velocity")
        prevTime = t
    }
    drive(Vector2(0.0,0.0), 0.0, true)
}
