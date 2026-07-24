package org.team2471.frc.lib.commands

import org.team2471.frc.lib.environment.isSim
import org.wpilib.command3.Command
import org.wpilib.command3.Mechanism
import org.wpilib.driverstation.DriverStationErrors

open class MechanismBase(val mechanismName: String): Mechanism {

    override fun getName(): String = mechanismName

    init {
        // If a default command has been specified, apply it to the mechanism.
        if (hasOverride("defaultCommand")) {
            val defaultCommand = defaultCommand()
            setDefaultCommandSafe(defaultCommand)
        }
    }

    /** The default command for this mechanism. Runs when no running commands are actively requiring this mechanism.
     *
     * **The default command must require the mechanism it is a part of.**
     *
     * Internally, this sets the [Mechanism.setDefaultCommand] variable
     */
    open fun defaultCommand(): Command = idle()

    private fun hasOverride(methodName: String): Boolean {
        val method = javaClass.getMethod(methodName)
        return method.declaringClass != MechanismBase::class.java
    }
}

fun Mechanism.setDefaultCommandSafe(dCommand: Command) {
    if (!dCommand.requires(this)) {
        DriverStationErrors.reportError("Default command MUST require this mechanism [$name].", true)
        throw IllegalArgumentException("Default command MUST require this mechanism [$name]. Did you put 'command(this) {}'? ")
    }
    val defaultCommandCopy = commandUnnamed(*dCommand.requirements().toTypedArray(), body = dCommand::run).named("$name Default", Command.LOWEST_PRIORITY, dCommand::onCancel)
    defaultCommand = defaultCommandCopy
}

/**
 * Adds a periodic function to the scheduler.
 * This function will run every robot loop cycle.
 *
 * For a custom period, different from the robot loop period, use [org.wpilib.framework.OpModeRobot.addPeriodic]. Or Kotlinx Coroutines.
 */
fun Mechanism.addPeriodic(body: () -> Unit) {
    this.registeredScheduler.addPeriodic(body)
}

fun Mechanism.addSimulationPeriodic(body: () -> Unit) {
    if (isSim) this.addPeriodic(body)
}
