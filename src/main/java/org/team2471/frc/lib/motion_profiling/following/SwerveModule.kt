package org.team2471.frc.lib.motion_profiling.following

import org.team2471.frc.lib.vector.Vector2

class SwerveModule {
    var location = Vector2(0.0, 0.0)  // the location of this module in Robot coordinates

    fun setPower(power: Double) {}  // open loop motor power or feed forward for position control
    fun setPosition(position: Double) {}  // closed loop setpoint for position
    fun setAngle(angle: Double) {}  // closed loop setpoint for angle

    fun getPosition() : Double = 0.0  // return the encoder value for distance traveled
    fun getAngle() : Double = 0.0  // return the angle of the wheel direction encoder
}