package org.team2471.frc.lib.hardware.loggedMotors

import edu.wpi.first.units.measure.Angle
import edu.wpi.first.units.measure.AngularAcceleration
import edu.wpi.first.units.measure.AngularVelocity
import edu.wpi.first.units.measure.Voltage
import org.littletonrobotics.junction.AutoLog
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.degreesPerSecond
import org.team2471.frc.lib.units.perSecond
import org.team2471.frc.lib.units.volts

@AutoLog
open class MotorInputs {
    @JvmField
    var angularPosition: Angle = 0.0.degrees
    @JvmField
    var angularVelocity: AngularVelocity = 0.0.degreesPerSecond
    @JvmField
    var angularAcceleration: AngularAcceleration = 0.0.degreesPerSecond.perSecond
    @JvmField
    var supplyVoltage: Voltage = 0.0.volts
}