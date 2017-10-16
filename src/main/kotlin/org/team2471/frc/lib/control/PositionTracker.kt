package org.team2471.frc.lib.control

import org.team2471.frc.lib.math.Vector

class PositionTracker(private val robotWidth: Double, leftPosition: Double, rightPosition: Double) {
    private var prevLeftPosition = leftPosition
    private var prevRightPosition = rightPosition

    operator fun get(leftPosition: Double, rightPosition: Double): Vector {
        val leftDelta = leftPosition - prevLeftPosition
        val rightDelta = rightPosition - prevRightPosition
        val diff = rightDelta - leftDelta
        TODO("everything else")
    }
}