package org.team2471.frc.lib.sensors.canCoder

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode

class LoggedCANCoder(val id: Int, name: String, simRotationSupplier: () -> Double = {0.0}, canBus: String = "") {
    private val inputs = LoggedCANCoderIO.CANCoderIOInputs(name)
    private val io: LoggedCANCoderIO = when (robotMode) {
        RobotMode.REAL -> LoggedCANCoderReal(id, canBus)
        RobotMode.REPLAY, RobotMode.SIM -> LoggedCANCoderSim(simRotationSupplier)
    }

    val position: Double get() = inputs.position
    val velocity: Double get() = inputs.velocity

    init {
        GlobalScope.launch {
            periodic {
                io.updateInputs(inputs)
                Logger.processInputs("Sensors", inputs)
            }
        }
    }

    fun setAngleOffset(offset: Double) {
        io.setMagnetSensorOffset(offset)
    }

    fun setInverted(invert: Boolean) {
        io.setInverted(invert)
    }
}