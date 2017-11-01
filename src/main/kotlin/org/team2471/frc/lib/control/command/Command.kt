package org.team2471.frc.lib.control.command

import java.util.*
import kotlin.collections.HashMap

// anything can be a subsystem!
typealias Subsystem = Any

abstract class Command(vararg requirements: Subsystem) {
    internal val requirements = mutableSetOf(*requirements)

    open val interruptable = true

    open fun initialize() = Unit

    open fun execute() = Unit

    abstract val isFinished: Boolean

    open fun end() = Unit

    open fun interrupted() = end()

    fun schedule() = Scheduler.runCommand(this)

    fun cancel() = Scheduler.interruptCommand(this)

    operator fun invoke() = schedule()
}

abstract class DefaultCommand(subsystem: Subsystem) : Command(subsystem) {
    fun register() = Scheduler.registerDefaultCommand(this)

    override val interruptable = true

    override val isFinished: Boolean = false
}

object Scheduler {
    private val commands: LinkedList<Command> = LinkedList()
    private val defaultCommands: MutableMap<Subsystem, Command> = HashMap()
    private val requirements: MutableMap<Subsystem, Command> = HashMap()

    fun runCommand(command: Command) {
        // don't schedule command if any of it's requirements cannot be interrupted
        if(command.requirements.any { requirements[it]?.interruptable == false }) return

        interruptCommand(command)

        commands.addFirst(command)
        for (subsystem in command.requirements) {
            requirements[subsystem]?.cancel()
            requirements[subsystem] = command
        }
        command.initialize()
    }

    fun interruptCommand(command: Command) {
        if (command !in commands) return

        command.interrupted()
        removeCommand(command)
    }

    operator fun contains(command: Command) = command in commands

    fun interruptAllCommands() {
        commands.forEach { interruptCommand(it) }
    }

    fun clearAllState() {
        commands.clear()
        defaultCommands.clear()
        requirements.clear()
    }

    internal fun registerDefaultCommand(defaultCommand: DefaultCommand) {
        val subsystem = defaultCommand.requirements.first()
        defaultCommands[subsystem] = defaultCommand
        if(subsystem !in requirements) defaultCommand.schedule()
    }

    fun tick() {
        // handle commands
        val iterator = commands.iterator()
        while (iterator.hasNext()) {
            val command = iterator.next()
            command.execute()
            if (command.isFinished) {
                command.end()
                removeCommand(command)
            }
        }

        // schedule triggers
        triggerFunctions.forEach { it() }
    }

    private fun removeCommand(command: Command) {
        if (commands.remove(command)) {
            for(subsystem in command.requirements) {
                requirements.remove(subsystem)
                defaultCommands[subsystem]?.schedule()
            }
        }
    }
}
