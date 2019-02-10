package org.team2471.frc.lib.motion.following

import kotlinx.coroutines.withTimeout
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*
import kotlin.math.absoluteValue

interface SwerveDrive {
    val parameters: SwerveParameters
    val heading: Angle
    val headingRate: AngularVelocity

    val frontLeftModule: Module
    val frontRightModule: Module
    val backLeftModule: Module
    val backRightModule: Module

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    interface Module {
        val angle: Angle

        fun drive(angle: Angle, power: Double)

        fun driveWithDistance(angle: Angle, distance: Length)

        fun stop()
    }
}

fun SwerveDrive.stop() {
    frontLeftModule.stop()
    frontRightModule.stop()
    backLeftModule.stop()
    backRightModule.stop()
}

fun SwerveDrive.drive(translation: Vector2, turn: Double) {
    if (translation.x == 0.0 && translation.y == 0.0 && turn == 0.0) {
        return stop()
    }
    val heading = (heading + (headingRate * parameters.gyroRateCorrection).changePerSecond).wrap()

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
