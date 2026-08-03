package org.team2471.frc.lib.commands

import org.wpilib.command3.Command
import org.wpilib.command3.Mechanism
import org.wpilib.driverstation.DriverStationErrors

/**
 * A [PeriodicMechanism] with a default command.
 *
 * Adds an overwritable [defaultCommand] which can be easily used to set a defualt command.
 */
open class MechanismBase(val mechanismName: String): PeriodicMechanism {

    override fun getName(): String = mechanismName

    init {
        // If a default command has been specified, apply it to the mechanism.
        if (hasOverride("defaultCommand")) setDefaultCommandSafe(defaultCommand())
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

    companion object {
        /**
         * Function that sets the default command of the mechanism and names it to "[Mechanism Name] Default".
         *
         * Throws if the default command does not require the mechanism.
         */
        fun Mechanism.setDefaultCommandSafe(dCommand: Command) {
            if (!dCommand.requires(this)) {
                DriverStationErrors.reportError("Default command MUST require this mechanism [$name].", true)
                throw IllegalArgumentException("Default command MUST require this mechanism [$name]. Did you put 'command(this) {}'? ")
            }
            val defaultCommandCopy = commandUnnamed(*dCommand.requirements().toTypedArray(), body = dCommand::run).named("$name Default", Command.LOWEST_PRIORITY, dCommand::onCancel)
            defaultCommand = defaultCommandCopy
        }
    }
}
