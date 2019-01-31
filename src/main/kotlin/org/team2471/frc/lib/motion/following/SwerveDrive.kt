package org.team2471.frc.lib.motion.following

import org.team2471.frc.lib.Unproven
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters
import org.team2471.frc.lib.units.*

@Unproven
interface SwerveDrive {
    val heading: Angle
    val headingRate: AngularVelocity
    val frontLeftAngle: Angle
    val frontRightAngle: Angle
    val backRightAngle: Angle
    val backLeftAngle: Angle
    val parameters: SwerveParameters

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    fun stop()

    fun driveClosedLoop(
        frontLeftDistance: Length, frontRightDistance: Length,
        backRightDistance: Length, backLeftDistance: Length,
        frontLeftAngle: Angle, frontRightAngle: Angle,
        backRightAngle: Angle, backLeftAngle: Angle
    )

    fun driveOpenLoop(
        frontLeftPower: Double, frontRightPower: Double,
        backLeftPower: Double, backRightPower: Double,
        frontLeftAngle: Angle, frontRightAngle: Angle,
        backLeftAngle: Angle, backRightAngle: Angle
    )
}

@Unproven
fun SwerveDrive.drive(translation: Vector2, turn: Double) {
    var heading = heading + (headingRate * parameters.gyroRateCorrection).changePerSecond
    heading = Math.IEEEremainder(heading.asRadians, 2 * Math.PI).radians

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

    val angles = arrayOf(frontLeftAngle, frontRightAngle, backLeftAngle, backRightAngle)

    for (i in 0..3) {
        val angleError = Math.IEEEremainder((angles[i] - turns[i]).asRadians, 2 * Math.PI).radians
        if (Math.abs(angleError.asRadians) > Math.PI / 2.0) {
            turns[i] -= Math.copySign(Math.PI, angleError.asRadians).radians
            speeds[i] = -speeds[i]
        }
    }

    driveOpenLoop(speeds[0], speeds[1], speeds[2], speeds[3], turns[0], turns[1], turns[2], turns[3])
}