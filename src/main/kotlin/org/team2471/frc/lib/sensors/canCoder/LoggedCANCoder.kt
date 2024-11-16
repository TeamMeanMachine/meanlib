package org.team2471.frc.lib.sensors.canCoder

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.AngularVelocity
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.perSecond
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode

class LoggedCANCoder(val id: Int, name: String, simRotationSupplier: () -> Double = {0.0}, canBus: String = "") {
    private val inputs = LoggedCANCoderIO.CANCoderIOInputs(name)
    private val io: LoggedCANCoderIO = when (robotMode) {
        RobotMode.REAL -> LoggedCANCoderReal(id, canBus)
        RobotMode.REPLAY, RobotMode.SIM -> LoggedCANCoderSim(simRotationSupplier)
    }

    val position: Angle get() = inputs.position.degrees
    val velocity: AngularVelocity get() = inputs.velocity.degrees.perSecond

    init {
        GlobalScope.launch {
            periodic {
                io.updateInputs(inputs)
                Logger.processInputs("Sensors", inputs)
            }
        }
    }

    fun setAngleOffset(offset: Angle) {
        io.setMagnetSensorOffset(offset.asDegrees)
    }

    fun setInverted(invert: Boolean) {
        io.setInverted(invert)
    }
}