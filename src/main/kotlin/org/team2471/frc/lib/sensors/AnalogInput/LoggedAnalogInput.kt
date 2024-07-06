package org.team2471.frc.lib.sensors.AnalogInput

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode
import edu.wpi.first.wpilibj.AnalogInput


/**
 * An AdvantageKit logged version of the [AnalogInput] class
 *
 * @param id the channel number of the [AnalogInput]
 * @param name log path of the [AnalogInput] in the "Sensors" section.
 * @param simTicksSupplier During simulation, the function that supplies the [value] that should be logged and returned. If null, [value] will return 0
 */
class LoggedAnalogInput(id: Int, name: String, simTicksSupplier: () -> Int? = {null}) {
    private val inputs = LoggedAnalogIO.AnalogIOInputs(name)

    val value: Int get() = inputs.ticks
    val voltage: Double get() = inputs.voltage

    private val io: LoggedAnalogIO = when (robotMode) {
        RobotMode.REAL -> LoggedAnalogIOReal(id)
        RobotMode.REPLAY, RobotMode.SIM -> LoggedAnalogIOSim(simTicksSupplier)
    }

    init {
        GlobalScope.launch {
            periodic {
                io.updateInputs(inputs)
                Logger.processInputs("Sensors", inputs)
            }
        }
    }
}