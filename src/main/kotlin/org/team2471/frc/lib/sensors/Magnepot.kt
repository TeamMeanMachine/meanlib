package org.team2471.frc.lib.sensors

import edu.wpi.first.wpilibj.AnalogInput

class Magnepot(channel: Int) : AnalogInput(channel) {
    override fun pidGet(): Double = (averageVoltage - 2.5) / 2.3 * 180
}
