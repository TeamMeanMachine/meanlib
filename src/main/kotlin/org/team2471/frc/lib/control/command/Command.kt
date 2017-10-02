package org.team2471.frc.lib.control.command

import java.util.*

abstract class Subsystem {
    internal var defaultCommand: Command? = null

    fun setDefaultCommand(command: Command) {
        defaultCommand = command
        Scheduler.runCommand(command)
    }
}

abstract class Command(vararg internal val requirements: Subsystem) {
    open fun initialize() = Unit

    open fun execute() = Unit

    abstract fun isFinished(): Boolean

    open fun end() = Unit

    open fun interrupted() = Unit
}

object Scheduler {
    private val commands: MutableList<Command> = LinkedList()

    fun runCommand(command: Command) {
        interruptCommand(command)

        commands.add(command)
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
            command.requirements
                    .mapNotNull { it.defaultCommand }
                    .forEach { runCommand(it) }
        }
    }
}
