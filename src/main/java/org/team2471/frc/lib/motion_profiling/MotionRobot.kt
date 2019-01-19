package org.team2471.frc.lib.motion_profiling

import org.team2471.frc.lib.vector.Vector2

open class MotionRobot {
    private var robotWidth = 30.0 / 12.0
    private var robotLength = 38.0 / 12.0

    fun getHeading() : Double = 0.0
    fun getPosition() : Vector2 { return Vector2(0.0, 0.0) }
}