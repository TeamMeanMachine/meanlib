package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command
import org.wpilib.command3.Scheduler
import org.wpilib.opmode.PeriodicOpMode

/**
 * Test OpMode.
 */
class TestOpMode(val name: String, val testCommand: Command, initFunction: () -> Unit): PeriodicOpMode() {
    val scheduler = Scheduler.getDefault()

    init {
        println("$name test created")
        initFunction()
    }

    override fun start() {
        println("scheduling $name test")
        scheduler.schedule(testCommand)
        println("Scheduled test")
    }

    override fun periodic() {}

    override fun end() {
        println("ending $name test")
        scheduler.cancel(testCommand)
        println("Cancelled test")
    }


}