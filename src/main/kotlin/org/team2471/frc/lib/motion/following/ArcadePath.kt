package org.team2471.frc.lib.motion.following

import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.motion_profiling.Path2D

class ArcadePath(private val path: Path2D, private val finalTrackWidth: Double) {
    private var prevCenterPositionForLeft: Vector2? = null
    private var prevCenterPositionForRight: Vector2? = null
    private var prevLeftPosition: Vector2? = null
    private var prevRightPosition: Vector2? = null
    private var leftDistance: Double = 0.0
    private var rightDistance: Double = 0.0

    fun resetDistances() {
        rightDistance = 0.0
        leftDistance = rightDistance
        prevCenterPositionForLeft = null
        prevCenterPositionForRight = null
        prevLeftPosition = null
        prevRightPosition = null
    }

    fun getLeftPosition(time: Double): Vector2 {
        return path.getSidePosition(time, -finalTrackWidth / 2.0)
    }

    fun getRightPosition(time: Double): Vector2 {
        return path.getSidePosition(time, finalTrackWidth / 2.0)
    }

    fun getLeftPositionDelta(time: Double): Double {
        if (prevLeftPosition == null) {
            prevCenterPositionForLeft = path.getPosition(time)
            prevLeftPosition = getLeftPosition(time)
            return 0.0
        }

        val centerPosition = path.getPosition(time)
        val leftPosition = getLeftPosition(time)
        val deltaCenter = centerPosition - prevCenterPositionForLeft!!
        val deltaLeft = leftPosition - prevLeftPosition!!
        prevCenterPositionForLeft = centerPosition
        prevLeftPosition = leftPosition

        val result = if (deltaCenter.dot(deltaLeft) > 0) {
            deltaLeft.length
        } else {
            -deltaLeft.length
        }
        return if (path.robotDirection == Path2D.RobotDirection.FORWARD)
            result
        else
            -result
    }

    fun getRightPositionDelta(time: Double): Double {
        if (prevRightPosition == null) {
            prevCenterPositionForRight = path.getPosition(time)
            prevRightPosition = getRightPosition(time)
            return 0.0
        }

        val centerPosition = path.getPosition(time)
        val rightPosition = getRightPosition(time)
        val deltaCenter = centerPosition - prevCenterPositionForRight!!
        val deltaRight = rightPosition - prevRightPosition!!
        prevCenterPositionForRight = centerPosition
        prevRightPosition = rightPosition

        val result = if (deltaCenter.dot(deltaRight) > 0) {
           deltaRight.length
        } else {
            -deltaRight.length
        }
        return if (path.robotDirection == Path2D.RobotDirection.FORWARD)
            result
        else
            -result
    }

    fun getLeftDistance(time: Double): Double {
        leftDistance += getLeftPositionDelta(time)
        return leftDistance
    }

    fun getRightDistance(time: Double): Double {
        rightDistance += getRightPositionDelta(time)
        return rightDistance
    }
}
