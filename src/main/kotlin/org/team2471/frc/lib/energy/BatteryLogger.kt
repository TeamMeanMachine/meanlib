package org.team2471.frc.lib.energy

import edu.wpi.first.units.measure.Current
import edu.wpi.first.units.measure.Energy
import edu.wpi.first.units.measure.Power
import edu.wpi.first.units.measure.Voltage
import edu.wpi.first.wpilibj.RobotController
import edu.wpi.first.wpilibj.Timer
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.units.amps
import org.team2471.frc.lib.units.asAmps
import org.team2471.frc.lib.units.asWatts
import org.team2471.frc.lib.units.wattHours
import org.team2471.frc.lib.units.watts

object BatteryLogger {
    val batteryVoltage: Voltage get() = RobotController.getMeasureBatteryVoltage()

    val mechanismPowerReports = mutableMapOf<String, PowerReport>()

    fun recordCurrent(mechanismName: String, current: Current) {
        val now = Timer.getFPGATimestamp()
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
        Logger.recordOutput("Battery/Battery Voltage", batteryVoltage)
        var totalPowerReport = PowerReport()
        mechanismPowerReports.forEach { (m, report) ->
            totalPowerReport += report
            Logger.recordOutput("Battery/${m}/Current", report.current)
            Logger.recordOutput("Battery/${m}/Power", report.power)
            Logger.recordOutput("Battery/${m}/AmpHours", report.ampHours)
            Logger.recordOutput("Battery/${m}/WattHours", report.wattHours)
        }
        Logger.recordOutput("Battery/Total/Current", totalPowerReport.current)
        Logger.recordOutput("Battery/Total/Power", totalPowerReport.power)
        Logger.recordOutput("Battery/Total/AmpHours", totalPowerReport.ampHours)
        Logger.recordOutput("Battery/Total/WattHours", totalPowerReport.wattHours)
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