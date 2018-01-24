package org.team2471.frc.lib.control.localization

import edu.wpi.first.wpilibj.RobotController
import org.team2471.frc.lib.math.Point
import org.team2471.frc.lib.math.average
import java.lang.Math.*

class PositionEstimator(private var position: Point, private var heading: Double,
                        time: Long = RobotController.getFPGATime()) {
    private var lastTimestamp = time

    /**
     * Updates and returns the estimated [position] based on given values.
     *
     * @param leftVelocity The current left side drive train velocity in units/second.
     * @param rightVelocity The current right side drive train velocity in units/second.
     * @param heading The current continuous heading in degrees.
     * @param time The current time in milliseconds. Defaults to [RobotController.getFPGATime].
     */
    fun getUpdatedPosition(leftVelocity: Double, rightVelocity: Double, heading: Double,
                           time: Long = RobotController.getFPGATime()): Point {
        val dt = (time - lastTimestamp) / 1000.0 // convert to seconds
        val avgVelocity = average(leftVelocity, rightVelocity)
        val deltaHeading = Math.toRadians(heading - this.heading) // convert to radians for trig functions below
        position += Point(
                avgVelocity * sin(deltaHeading),
                avgVelocity * cos(deltaHeading)) * dt

        this.heading = heading
        lastTimestamp = time
        return position
    }

    fun reset(position: Point = Point.ORIGIN, heading: Double = this.heading) {
        this.position = position
        this.heading = heading
    }
}

