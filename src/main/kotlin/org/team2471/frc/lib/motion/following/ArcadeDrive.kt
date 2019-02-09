package org.team2471.frc.lib.motion.following

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.Path2D
import org.team2471.frc.lib.motion_profiling.following.ArcadeParameters

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

suspend fun <T> T.driveAlongPath(
        path: Path2D,
        extraTime: Double = 0.0
) where T : ArcadeDrive, T : Subsystem = use(this) {
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

            val rightFeedForward = rightVelocity * parameters.leftFeedForwardCoefficient +
                    (parameters.leftFeedForwardOffset * Math.signum(rightVelocity)) -
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
