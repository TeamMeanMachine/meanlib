package org.team2471.frc.lib.motion_profiling.following

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.Unproven
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.Subsystem
import org.team2471.frc.lib.framework.use
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.windRelativeAngles
import org.team2471.frc.lib.motion_profiling.Path2D

interface ArcadeRobot {
    fun driveOpenLoop(leftPower: Double, rightPower: Double)

    fun driveClosedLoop(
            leftDistance: Double, leftFeedForward: Double,
            rightDistance: Double, rightFeedForward: Double
    )

    fun startFollowing()

    fun stop() {
        driveOpenLoop(0.0, 0.0)
    }

    val heading: Double
    val headingRate: Double

    val config: Config

    data class Config(
            val trackWidth: Double,
            val scrubFactor: Double,
            val driveTurningP: Double,
            val leftFeedForwardOffset: Double,
            val leftFeedForwardCoefficient: Double,
            val rightFeedForwardOffset: Double,
            val rightFeedForwardCoefficient: Double,
            val headingFeedForward: Double,
            val doHeadingCorrection: Boolean,
            val headingCorrectionP: Double,
            val headingCorrectionI: Double,
            val headingCorrectionIDecay: Double
    )
}

@Unproven
suspend fun <T> T.driveAlongPath(
        path: Path2D,
        extraTime: Double = 0.0
) where T : ArcadeRobot {
    suspend fun follow() {
        println("Driving along path ${path.name}, duration: ${path.durationWithSpeed}, " +
                "travel direction: ${path.robotDirection}, mirrored: ${path.isMirrored}")

        startFollowing()

        val arcadePath = ArcadePath(path, config.trackWidth * config.scrubFactor)

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

                angleErrorAccum = angleErrorAccum * config.headingCorrectionIDecay + angleError

                val gyroCorrection = if (config.doHeadingCorrection) {
                    angleError * config.headingCorrectionP + angleErrorAccum * config.headingCorrectionI
                } else {
                    0.0
                }

                // update left/right path positions


                val leftDistance = arcadePath.getLeftDistance(t) + gyroCorrection
                val rightDistance = arcadePath.getRightDistance(t) - gyroCorrection

                val leftVelocity = (leftDistance - prevLeftDistance) / dt
                val rightVelocity = (rightDistance - prevRightDistance) / dt

                val velocityDeltaTimesCoefficient = (leftVelocity - rightVelocity) * config.headingFeedForward

                val leftFeedForward = leftVelocity * config.leftFeedForwardCoefficient +
                        (config.leftFeedForwardOffset * Math.signum(leftVelocity)) +
                        velocityDeltaTimesCoefficient

                val rightFeedForward = rightVelocity * config.leftFeedForwardCoefficient +
                        (config.leftFeedForwardOffset * Math.signum(rightVelocity)) -
                        velocityDeltaTimesCoefficient

                driveClosedLoop(leftDistance, leftFeedForward, rightDistance, rightFeedForward)

                if (t >= path.durationWithSpeed + extraTime) stop()

                prevTime = t
                prevLeftDistance = leftDistance
                prevRightDistance = rightDistance
            }
        } finally {
            stop()
        }
    }

    if (this is Subsystem) use(this) {
        follow()
    } else follow()
}

fun ArcadeRobot.drive(throttle: Double, softTurn: Double, hardTurn: Double) {
    val totalTurn = (softTurn * Math.abs(throttle)) + hardTurn
    val velocitySetpoint = totalTurn * 250.0

    val gyroRate = if (config.doHeadingCorrection) headingRate else 0.0
    val velocityError = velocitySetpoint - gyroRate

    val turnAdjust = (velocityError * config.driveTurningP).deadband(1.0e-2)

    var leftPower = throttle + totalTurn + turnAdjust
    var rightPower = throttle - totalTurn - turnAdjust

    val maxPower = Math.max(Math.abs(leftPower), Math.abs(rightPower))
    if (maxPower > 1) {
        leftPower /= maxPower
        rightPower /= maxPower
    }

    driveOpenLoop(leftPower, rightPower)
}