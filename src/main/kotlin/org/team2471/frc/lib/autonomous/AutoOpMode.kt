package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command
import org.wpilib.command3.Scheduler
import org.wpilib.opmode.PeriodicOpMode

/**
 * Autonomous [org.wpilib.opmode.OpMode].
 *
 * When selected will call the [warmupFunction] provided by [Autonomi.warmupFunction]
 *
 * Then will periodically run the [disabledPeriodicFunction] until enabled
 *
 * Schedules [autoCommand] when enabled and runs it until its disabled or finished.
 */
class AutoOpMode(val name: String, val autoCommand: Command, warmupFunction: () -> Unit, val disabledPeriodicFunction: () -> Unit): PeriodicOpMode() {
    val scheduler = Scheduler.getDefault()

    init {
        println("$name auto created")
        warmupFunction()
        println("$name auto warmup complete.")
    }

    override fun disabledPeriodic() {
        disabledPeriodicFunction()
    }

    override fun start() {
        println("scheduling $name auto")
        scheduler.schedule(autoCommand)
        println("Scheduled auto")
    }

    override fun periodic() {}

    override fun end() {
        println("ending $name auto")
        scheduler.cancel(autoCommand)
        println("Cancelled auto")
    }
}