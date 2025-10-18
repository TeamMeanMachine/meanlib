package org.team2471.frc.lib.control.commands

import edu.wpi.first.util.sendable.SendableBuilder
import edu.wpi.first.util.sendable.SendableRegistry
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj2.command.Command

class WaitUntilOrTimeCommand(val seconds: Double = Double.MAX_VALUE, val condition: () -> Boolean): Command() {
    private val timer = Timer()

    init {
        SendableRegistry.setName(this, getName() + ": " + seconds + " seconds")
    }

    override fun initialize() {
        timer.restart()
    }

    override fun end(interrupted: Boolean) {
        timer.stop()
    }

    override fun isFinished(): Boolean {
        return timer.hasElapsed(seconds) || condition()
    }

    override fun runsWhenDisabled(): Boolean {
        return true
    }

    override fun initSendable(builder: SendableBuilder) {
        super.initSendable(builder)
        builder.addDoubleProperty("duration", { seconds }, null)
    }
}