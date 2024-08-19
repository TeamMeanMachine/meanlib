package org.team2471.frc.lib.testing

import org.team2471.frc.lib.motion_profiling.Path2D

object EightFootCircle : Path2D() {
    init {
        name = "8 Foot Circle"
        robotDirection = RobotDirection.FORWARD
        val tangentLength = 6.5

        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)
        addPointAndTangent(4.0, 4.0, tangentLength, 0.0)
        addPointAndTangent(8.0, 0.0, 0.0, -tangentLength)
        addPointAndTangent(4.0, -4.0, -tangentLength, 0.0)
        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)

        addEasePoint(0.0, 0.0)
        addEasePoint(8.0, 1.0)
    }
}

object FourFootCircle : Path2D() {
    init {
        name = "4 Foot Circle"
        robotDirection = RobotDirection.FORWARD
        val tangentLength = 3.25

        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)
        addPointAndTangent(2.0, 2.0, tangentLength, 0.0)
        addPointAndTangent(4.0, 0.0, 0.0, -tangentLength)
        addPointAndTangent(2.0, -2.0, -tangentLength, 0.0)
        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)

        addEasePoint(0.0, 0.0)
        addEasePoint(4.0, 1.0)
    }
}

object TwoFootCircle : Path2D() {
    init {
        name = "2 Foot Circle"
        robotDirection = RobotDirection.FORWARD
        val tangentLength = 1.6

        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)
        addPointAndTangent(1.0, 1.0, tangentLength, 0.0)
        addPointAndTangent(2.0, 0.0, 0.0, -tangentLength)
        addPointAndTangent(1.0, -1.0, -tangentLength, 0.0)
        addPointAndTangent(0.0, 0.0, 0.0, tangentLength)

        addEasePoint(0.0, 0.0)
        addEasePoint(2.0, 1.0)
    }
}

object EightFootStraight : Path2D() {
    init {
        name = "8 Foot Straight"
        robotDirection = Path2D.RobotDirection.FORWARD
        addPoint(0.0, 0.0)
        addPoint(8.0, 0.0)
        addEasePoint(0.0, 0.0)
        addEasePoint(5.0, 1.0)
    }
}

object AngleAndMagnitudeBug : Path2D() {
    init {
        name = "Angle and Magnitude Bug"
        addPointAndTangent(0.0, 0.0, 0.0, 3.0)
        //addPointAndTangent(-2.0, 2.0, -2.0, 0.0)
        addPointAngleAndMagnitude(-2.0, 2.0, 15.0, 1.0)
        addEasePoint(0.0, 0.0)
        addEasePoint(5.0, 1.0)
    }
}

object HookPath : Path2D() {
    init {
        name = "Hook Path"
        addPointAndTangent(0.0, 0.0, 0.0, 0.0)
        addPointAndTangent(3.0, 0.0, 0.0, 3.0)
        addEasePoint(0.0, 0.0)
        addEasePoint(5.0, 1.0)
    }
}


