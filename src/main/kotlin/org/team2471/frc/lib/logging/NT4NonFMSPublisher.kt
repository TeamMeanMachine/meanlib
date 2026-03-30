package org.team2471.frc.lib.logging

import edu.wpi.first.wpilibj.DriverStation
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.networktables.NT4Publisher

/**
 * A NetworkTables publisher that only publishes log data to networktables if the robot is not connected to the FMS.
 * @see NT4Publisher
 */
class NT4NonFMSPublisher: NT4Publisher() {
    override fun putTable(table: LogTable?) {
        if (!DriverStation.isFMSAttached()) {
            super.putTable(table)
        }
    }
}