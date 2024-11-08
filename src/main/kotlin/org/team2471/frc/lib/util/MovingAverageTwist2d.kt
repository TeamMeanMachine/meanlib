package org.team2471.frc.lib.util

import edu.wpi.first.math.filter.LinearFilter
import edu.wpi.first.math.geometry.Twist2d

class MovingAverageTwist2d(val taps: Int) {
    private val movingAverageDX = LinearFilter.movingAverage(taps)
    private val movingAverageDY = LinearFilter.movingAverage(taps)
    private val movingAverageDTheta = LinearFilter.movingAverage(taps)

    var latestTwist = Twist2d()

    fun update(twist2d: Twist2d) {
        val dX = movingAverageDX.calculate(twist2d.dx)
        val dY = movingAverageDY.calculate(twist2d.dy)
        val dTheta = movingAverageDTheta.calculate(twist2d.dtheta)

        latestTwist = Twist2d(dX, dY, dTheta)
    }

    fun calculate(twist2d: Twist2d): Twist2d {
        val dX = movingAverageDX.calculate(twist2d.dx)
        val dY = movingAverageDY.calculate(twist2d.dy)
        val dTheta = movingAverageDTheta.calculate(twist2d.dtheta)

        latestTwist = Twist2d(dX, dY, dTheta)

        return latestTwist
    }

    fun reset() {
        movingAverageDX.reset()
        movingAverageDY.reset()
        movingAverageDTheta.reset()

        latestTwist = Twist2d()
    }
}