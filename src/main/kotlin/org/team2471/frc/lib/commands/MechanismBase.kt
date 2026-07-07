package org.team2471.frc.lib.commands

import org.team2471.frc.lib.util.isSim
import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.command3.Mechanism
import org.wpilib.command3.Scheduler

open class MechanismBase(val mechanismName: String): Mechanism {

    override fun getName(): String = mechanismName

    init {
        // Checks if periodic() and simulationPeriodic() have been overridden before adding them to the periodic scheduler,
        // just to avoid having a bunch of empty periodic methods in the scheduler.
        if (hasOverride("periodic")) {
            MasterMechanism.callbacksToBeAdded.add(this::periodic)
        }

        if (isSim && hasOverride("simulationPeriodic")) {
            MasterMechanism.callbacksToBeAdded.add(this::simulationPeriodic)
        }

        // If a default command has been specified, apply it to the mechanism.
        default()?.let { defaultCommand = it }
    }

    /** The default command for this mechanism. By default, it is [idle]   */
    open fun default(): Command? = null

    /**
     * Function ran continuously every scheduler tick.
     * @see Scheduler.addPeriodic
     * */
    open fun periodic() {}

    /**
     * Function ran continuously every scheduler tick in only in simulation.
     * @see Scheduler.addPeriodic
     */
    open fun simulationPeriodic() {}


    private fun hasOverride(methodName: String): Boolean {
        val method = javaClass.getMethod(methodName)
        return method.declaringClass != MechanismBase::class.java
    }
}

/**
 * Shortcut utility function to create a default command.
 *
 * Sets the command name to be "[Mechanism.getName] Default", require this mechanism, and have the [Command.LOWEST_PRIORITY]
 * @see Mechanism.setDefaultCommand
 */
fun Mechanism.setDefaultCommand(body: Coroutine.() -> Unit): Command {
    val createdDefault = useUnnamed(this, body = body).withPriority(Command.LOWEST_PRIORITY).named("$name Default")
    defaultCommand = createdDefault
    return createdDefault
}
