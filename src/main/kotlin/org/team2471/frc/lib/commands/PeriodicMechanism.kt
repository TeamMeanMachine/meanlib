package org.team2471.frc.lib.commands

import org.wpilib.command3.Mechanism

/**
 * A [Mechanism] with a [periodic] and [telemetryPeriodic] function.
 *
 * Does not automatically call [periodic], [telemetryPeriodic], or [simulationPeriodic], they must be called somewhere else.
 */
interface PeriodicMechanism: Mechanism {
    /**
     * Periodically called **before**, the command scheduler is run.
     * Generally, if something should be running periodically, it should go here. (Updating sensor values, odometry, motor values)
     *
     * Consider using [telemetryPeriodic] for logging/telemetry. Although not required.
     * @see telemetryPeriodic
     * */
    fun periodic() {}
    /**
     * Periodically called **after**, the command scheduler is run.
     * Generally used for Logging/Telemetry.
     * @see periodic
     */
    fun telemetryPeriodic() {}

    /**
     * Periodically called only in simulation.
     *
     * Usually called inside Robot.simulationPeriodic function
     */
    fun simulationPeriodic() {}
}