package org.team2471.frc.lib.motion_profiling

import org.team2471.frc.lib.vector.Vector2

class SwerveRobot : MotionRobot() {
/*
    val swerveModules : Array<SwerveModule> = {
        SwerveModule(Vector2(15, 16)),
        SwerveModule(Vector2(15, -16),
                SwerveMOdule(Vector2(-15, -16),
                        SwerveModule(Vector2(-15, 16))))
    }
*/

    var pivot = Vector2(0.0, 0.0)  // the turning point of the robot
}