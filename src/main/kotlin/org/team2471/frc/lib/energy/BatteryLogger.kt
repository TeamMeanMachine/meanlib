package org.team2471.frc.lib.energy

import org.team2471.frc.lib.logging.SimpleLogger
import org.team2471.frc.lib.units.amps
import org.team2471.frc.lib.units.asAmps
import org.team2471.frc.lib.units.asWatts
import org.team2471.frc.lib.units.wattHours
import org.team2471.frc.lib.units.watts
import org.wpilib.system.RobotController
import org.wpilib.system.Timer
import org.wpilib.units.measure.Current
import org.wpilib.units.measure.Energy
import org.wpilib.units.measure.Power
import org.wpilib.units.measure.Voltage

object BatteryLogger {
    val batteryVoltage: Voltage get() = RobotController.getMeasureBatteryVoltage()

    val mechanismPowerReports = mutableMapOf<String, PowerReport>()

    fun recordCurrent(mechanismName: String, current: Current) {
        val now = Timer.getMonotonicTimestamp()
        val lastEntry = mechanismPowerReports.getOrPut(mechanismName) { PowerReport(timestamp = now) }
        val dt = now - lastEntry.timestamp

        val power = current * batteryVoltage

        lastEntry.current = current
        lastEntry.power = power
        lastEntry.ampHours += current.asAmps * dt / 3600.0
        lastEntry.wattHours += (power.asWatts * dt / 3600.0).wattHours
        lastEntry.timestamp = now
    }

    fun logData() {
        SimpleLogger.recordOutput("Battery/Battery Voltage", batteryVoltage)
        var totalPowerReport = PowerReport()
        mechanismPowerReports.forEach { (m, report) ->
            totalPowerReport += report
            SimpleLogger.recordOutput("Battery/${m}/Current", report.current)
            SimpleLogger.recordOutput("Battery/${m}/Power", report.power)
            SimpleLogger.recordOutput("Battery/${m}/AmpHours", report.ampHours)
            SimpleLogger.recordOutput("Battery/${m}/WattHours", report.wattHours)
        }
        SimpleLogger.recordOutput("Battery/Total/Current", totalPowerReport.current)
        SimpleLogger.recordOutput("Battery/Total/Power", totalPowerReport.power)
        SimpleLogger.recordOutput("Battery/Total/AmpHours", totalPowerReport.ampHours)
        SimpleLogger.recordOutput("Battery/Total/WattHours", totalPowerReport.wattHours)
    }


    data class PowerReport(
        var current: Current = 0.0.amps,
        var power: Power = 0.0.watts,

        var ampHours: Double = 0.0,
        var wattHours: Energy = 0.0.wattHours,

        var timestamp: Double = 0.0
    ) {
        operator fun plus(other: PowerReport) = PowerReport(
            current + other.current,
            power + other.power,
            ampHours + other.ampHours,
            wattHours + other.wattHours,
            timestamp
        )
    }
}