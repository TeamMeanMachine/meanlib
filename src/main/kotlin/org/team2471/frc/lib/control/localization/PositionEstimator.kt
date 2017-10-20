package org.team2471.frc.lib.control.localization

import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.math.Point
import org.team2471.frc.lib.math.average
import java.lang.Math.*

class PositionEstimator(private var position: Point, private var heading: Double,
                        time: Double = Timer.getFPGATimestamp()) {
    private var lastTimestamp = time

    /**
     * Update the estimated [position] based on given values
     *
     * @param leftVelocity The current left side drive train velocity
     * @param rightVelocity The current right side drive train velocity
     * @param heading The current heading in degrees between 0 and 360
     */
    fun getUpdatedPosition(leftVelocity: Double, rightVelocity: Double, heading: Double,
                           time: Double = Timer.getFPGATimestamp()): Point {
        val dt = time - lastTimestamp
        val avgVelocity = average(leftVelocity, rightVelocity)
        val deltaHeading = (Math.toRadians(heading - this.heading))
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

