package org.team2471.frc.lib.motion.following

import org.team2471.frc.lib.environment.demoSpeed
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.units.*
import org.wpilib.smartdashboard.SmartDashboard
import org.wpilib.units.measure.AngularVelocity
import kotlin.math.abs

interface ArcadeDrive {
    val headingRate: AngularVelocity
    val parameters: ArcadeParameters

    fun driveOpenLoop(leftPower: Double, rightPower: Double)
}

/**
 * Allows for teleoperated hybrid drive of the robot, with optional heading correction and turning
 * correction if specified in the [ArcadeParameters].
 *
 * @param throttle the forward percent speed to drive at
 * @param softTurn an amount of turn proportional to the [throttle]
 * @param hardTurn a raw turn value, added to the left output and subtracted from the right output
 */
fun ArcadeDrive.hybridDrive(throttle: Double, softTurn: Double, hardTurn: Double) {
    if (!SmartDashboard.containsKey("DemoSpeed")) SmartDashboard.setDefaultNumber("DemoSpeed", 1.0)
    var cappedThrottle = throttle * demoSpeed
    var cappedHardTurn = hardTurn * demoSpeed
    val totalTurn = (softTurn * abs(cappedThrottle)) + cappedHardTurn

    var leftPower = cappedThrottle + totalTurn
    var rightPower = cappedThrottle - totalTurn

    // Heading correction
    if (parameters.driveTurningP != 0.0) {
        val velocitySetpoint = 250.0.degrees.perSecond * totalTurn
        val gyroRate = if (parameters.doHeadingCorrection) headingRate else 0.0.degrees.perSecond
        val velocityError = velocitySetpoint - gyroRate
        val turnAdjust = (velocityError.asDegreesPerSecond * parameters.driveTurningP).deadband(1.0e-2)
        leftPower += turnAdjust
        rightPower -= turnAdjust
    }

    val maxPower = abs(leftPower).coerceAtLeast(abs(rightPower))
    if (maxPower > 1) {
        leftPower /= maxPower
        rightPower /= maxPower
    }

    driveOpenLoop(leftPower, rightPower)
}


data class ArcadeParameters(
    var driveTurningP: Double = 0.0,
    var doHeadingCorrection: Boolean = false,
)