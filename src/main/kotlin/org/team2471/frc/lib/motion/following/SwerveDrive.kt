package org.team2471.frc.lib.motion.following

import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.following.SwerveParameters

interface SwerveDrive {
    val heading: Double
    val headingRate: Double
    val frontLeftAngle: Double
    val frontRightAngle: Double
    val backRightAngle: Double
    val backLeftAngle: Double
    val parameters: SwerveParameters

    fun startFollowing() = Unit

    fun stopFollowing() = Unit

    fun stop()

    fun driveClosedLoop(
            frontLeftDistance: Double, frontRightDistance: Double,
            backRightDistance: Double, backLeftDistance: Double,
            topLeftAngle: Double, topRightAngle: Double,
            bottomRightAngle: Double, bottomLeftAngle: Double
    )

    fun driveOpenLoop(
            frontLeftPower: Double, frontRightPower: Double,
            backLeftPower: Double, backRightPower: Double,
            frontleftAngle: Double, frontRightAngle: Double,
            backleftAngle: Double, backRightAngle: Double
    )
}

fun SwerveDrive.drive(translation: Vector2, turn: Double) {
    var heading = heading + headingRate * parameters.gyroRateCorrection
    heading = Math.IEEEremainder(heading, 360.0)

    heading = Math.toRadians(heading)
    translation.let { (x, y) ->
        translation.x = -y * Math.sin(heading) + x * Math.cos(heading)
        translation.y =  y * Math.cos(heading) + x * Math.sin(heading)
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

    val turns = doubleArrayOf(
            Math.atan2(b, d) * 0.5 / Math.PI,
            Math.hypot(b, c) * 0.5 / Math.PI,
            Math.hypot(a, d) * 0.5 / Math.PI,
            Math.hypot(a, c) * 0.5 / Math.PI
    )

    val maxSpeed = speeds.max()!!
    if (maxSpeed > 1.0){
        for (i in 0..3) {
            speeds[i] /= maxSpeed
        }
    }

    val angles = doubleArrayOf(frontLeftAngle, frontRightAngle, backLeftAngle, backRightAngle)

    for (i in 0..3) {
        val angleError = Math.IEEEremainder(angles[i] - turns[i], 360.0)
        if (Math.abs(angleError) > 90) {
            turns[i] -= Math.copySign(180.0, angleError)
            speeds[i] = -speeds[i]
        }
    }

    driveOpenLoop(speeds[0], speeds[1], speeds[2], speeds[3], turns[0], turns[1], turns[2], turns[3])
}