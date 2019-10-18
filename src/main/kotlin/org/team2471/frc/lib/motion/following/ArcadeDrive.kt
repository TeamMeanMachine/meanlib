package org.team2471.frc.lib.motion.following

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.ArcadeParameters
import org.team2471.frc.lib.units.degrees

interface ArcadeDrive {
    val heading: Double
    val headingRate: Double
    val parameters: ArcadeParameters

    fun driveOpenLoop(leftPower: Double, rightPower: Double)

    fun driveClosedLoop(
            leftDistance: Double, leftFeedForward: Double,
            rightDistance: Double, rightFeedForward: Double
    )

    fun startFollowing() { /* NOOP */ }

    fun stopFollowing() { /* NOOP */ }

    fun stop() {
        driveOpenLoop(0.0, 0.0)
    }
}

/**
 * Follows a specified [path] using the robot's [ArcadeParameters].
 *
 * @param path the [Path2D] to follow
 * @param extraTime the amount of extra time to wait for minor corrections to the path after its completion
 */
suspend fun <T> T.driveAlongPath(
        path: Path2D,
        extraTime: Double = 0.0
) where T : ArcadeDrive, T : Subsystem = use(this, name = "Drive Along Path") {
    println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, " +
            "travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")

    startFollowing()

    val arcadePath = ArcadePath(path, parameters.trackWidth * parameters.scrubFactor)

    var prevLeftDistance = 0.0
    var prevRightDistance = 0.0
    var prevTime = 0.0

    val timer = Timer().apply { start() }

    var angleErrorAccum = 0.0
    try {
        periodic {
            val t = timer.get()
            val dt = t - prevTime

            // apply gyro corrections to the distances
            val gyroAngle = heading
            val pathAngle = Math.toDegrees(path.getTangent(t).angle)
            val angleError = pathAngle - windRelativeAngles(pathAngle, gyroAngle)

            angleErrorAccum = angleErrorAccum * parameters.headingCorrectionIDecay + angleError

            val gyroCorrection = if (parameters.doHeadingCorrection) {
                angleError * parameters.headingCorrectionP + angleErrorAccum * parameters.headingCorrectionI
            } else {
                0.0
            }

            // update left/right path positions

            val leftDistance = arcadePath.getLeftDistance(t) + gyroCorrection
            val rightDistance = arcadePath.getRightDistance(t) - gyroCorrection

            val leftVelocity = (leftDistance - prevLeftDistance) / dt
            val rightVelocity = (rightDistance - prevRightDistance) / dt

            val velocityDeltaTimesCoefficient = (leftVelocity - rightVelocity) * parameters.headingFeedForward

            val leftFeedForward = leftVelocity * parameters.leftFeedForwardCoefficient +
                    (parameters.leftFeedForwardOffset * Math.signum(leftVelocity)) +
                    velocityDeltaTimesCoefficient

            val rightFeedForward = rightVelocity * parameters.rightFeedForwardCoefficient +
                    (parameters.rightFeedForwardOffset * Math.signum(rightVelocity)) -
                    velocityDeltaTimesCoefficient

            driveClosedLoop(leftDistance, leftFeedForward, rightDistance, rightFeedForward)

            if (t >= path.durationWithSpeed + extraTime) stop()

            prevTime = t
            prevLeftDistance = leftDistance
            prevRightDistance = rightDistance
        }
    } finally {
        stop()
        stopFollowing()
    }
}

suspend fun ArcadeDrive.tuneDrivePositionController(controller: org.team2471.frc.lib.input.XboxController)
{
    var prevLeftDistance = 0.0
    var prevRightDistance = 0.0
    var prevTime = 0.0

    val timer = Timer().apply { start() }

    var angleErrorAccum = 0.0.degrees
    try {
        periodic {
            val t = timer.get()
            val dt = t - prevTime

            // apply gyro corrections to the distances
            val gyroAngle = heading
            val joystickHeading = 90.0.degrees * controller.rightThumbstickX
            val angleError = (joystickHeading - gyroAngle.degrees).wrap()

            angleErrorAccum = angleErrorAccum * parameters.headingCorrectionIDecay + angleError

            val gyroCorrection = if (parameters.doHeadingCorrection) {
                angleError.asDegrees * parameters.headingCorrectionP + angleErrorAccum.asDegrees * parameters.headingCorrectionI
            } else {
                0.0
            }

            // update left/right positions

            val leftDistance = controller.leftThumbstickY + gyroCorrection
            val rightDistance = controller.leftThumbstickY - gyroCorrection

            val leftVelocity = (leftDistance - prevLeftDistance) / dt
            val rightVelocity = (rightDistance - prevRightDistance) / dt

            val velocityDeltaTimesCoefficient = (leftVelocity - rightVelocity) * parameters.headingFeedForward

            val leftFeedForward = leftVelocity * parameters.leftFeedForwardCoefficient +
                    (parameters.leftFeedForwardOffset * Math.signum(leftVelocity)) +
                    velocityDeltaTimesCoefficient

            val rightFeedForward = rightVelocity * parameters.rightFeedForwardCoefficient +
                    (parameters.rightFeedForwardOffset * Math.signum(rightVelocity)) -
                    velocityDeltaTimesCoefficient

            driveClosedLoop(leftDistance, leftFeedForward, rightDistance, rightFeedForward)

            prevTime = t
            prevLeftDistance = leftDistance
            prevRightDistance = rightDistance
        }
    } finally {
        stop()
    }
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
    val totalTurn = (softTurn * Math.abs(throttle)) + hardTurn
    val velocitySetpoint = totalTurn * 250.0

    val gyroRate = if (parameters.doHeadingCorrection) headingRate else 0.0
    val velocityError = velocitySetpoint - gyroRate

    val turnAdjust = (velocityError * parameters.driveTurningP).deadband(1.0e-2)

    var leftPower = throttle + totalTurn + turnAdjust
    var rightPower = throttle - totalTurn - turnAdjust

    val maxPower = Math.max(Math.abs(leftPower), Math.abs(rightPower))
    if (maxPower > 1) {
        leftPower /= maxPower
        rightPower /= maxPower
    }

    driveOpenLoop(leftPower, rightPower)
}
