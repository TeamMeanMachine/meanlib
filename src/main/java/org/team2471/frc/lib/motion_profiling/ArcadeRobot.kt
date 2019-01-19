package org.team2471.frc.lib.motion_profiling

abstract class ArcadeRobot : MotionRobot() {
    private var trackWidth = 25.0 / 12.0
    private var scrubFactor = 1.12

    abstract fun setLeftPower()
    abstract fun setRightPower()
    abstract fun setLeftDistance()
    abstract fun setRightDistance()

    fun Drive( power: Double, turning: Double) {

    }

    fun DriveAlongPath( path2D : Path2D ) {

    }
}