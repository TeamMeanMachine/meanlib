package org.team2471.frc.lib.control.command

import java.util.*
import kotlin.collections.HashMap

abstract class Subsystem {
    internal var defaultCommand: Command? = null

    protected fun setDefaultCommand(command: Command) {
        defaultCommand = command
        Scheduler.runCommand(command)
    }
}

abstract class Command(vararg internal val requirements: Subsystem) {
    open val interruptable = true

    open fun initialize() = Unit

    open fun execute() = Unit

    abstract fun isFinished(): Boolean

    open fun end() = Unit

    open fun interrupted() = end()

    fun run() = Scheduler.runCommand(this)

    fun interrupt() = Scheduler.interruptCommand(this)

    operator fun invoke() = run()
}

object Scheduler {
    private val commands: MutableList<Command> = LinkedList()
    private val requirements: MutableMap<Subsystem, Command> = HashMap()

    fun runCommand(command: Command) {
        // don't run command if any of it's requirements cannot be interrupted
        if(command.requirements.any { requirements[it]?.interruptable == false }) return

        interruptCommand(command)

        commands.add(command)
        for (subsystem in command.requirements) {
            requirements[subsystem]?.interrupt()
            requirements[subsystem] = command
        }
        command.initialize()
    }

    fun interruptCommand(command: Command) {
        if (command !in commands) return

        command.interrupted()
        removeCommand(command)
    }

    fun isRunningCommand(command: Command) = command in commands

    fun tick() {
        // handle commands
        for (command in commands) {
            command.execute()
            if (command.isFinished()) {
                command.end()
                removeCommand(command)
            }
        }

        // run triggers
        triggerFunctions.forEach { it() }
    }

    private fun removeCommand(command: Command) {
        if (commands.remove(command)) {
            for(subsystem in command.requirements) {
                requirements.remove(subsystem)
                subsystem.defaultCommand?.run()
            }
        }
    }
}
