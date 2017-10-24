package org.team2471.frc.lib.control.command

class ParallelCommand(vararg private val commands: Command) : Command() {
    private lateinit var activeCommands: MutableSet<Command>

    init {
        commands.forEach { command ->
            command.requirements.forEach { requirements += it }
        }
    }

    override fun initialize() {
        activeCommands = mutableSetOf(*commands)
        activeCommands.forEach { it.initialize() }
    }

    override fun execute() {
        val iterator = activeCommands.iterator()
        while(iterator.hasNext()) {
            val command = iterator.next()
            command.execute()
            if(command.isFinished()) {
                command.end()
                activeCommands.remove(command)
            }
        }
    }

    override fun isFinished(): Boolean = activeCommands.size == 0

    override fun interrupted() = activeCommands.forEach { it.interrupted() }
}

class SequentialCommand(vararg private val commands: Command) : Command() {
    private var activeIndex = 0
    private val activeCommand get() = commands[activeIndex]

    init {
        commands.forEach { command ->
            command.requirements.forEach { requirements += it }
        }
    }

    override fun initialize() {
        activeIndex = 0
        activeCommand.initialize()
    }

    override fun execute() {
        activeCommand.execute()
        if(activeCommand.isFinished()) {
            activeCommand.end()
            activeIndex++
        }
    }

    override fun isFinished(): Boolean = activeIndex == commands.size

    override fun interrupted() = commands.drop(activeIndex).forEach { it.interrupted() }
}

class RunUntilCommand(private val backgroundCommand: Command, private val mainCommand: Command) : Command() {
    private var backgroundFinished = false

    init {
        mainCommand.requirements.forEach { requirements.add(it) }
        backgroundCommand.requirements.forEach { requirements.add(it) }
    }

    override fun initialize() {
        mainCommand.initialize()
        backgroundCommand.initialize()
        backgroundFinished = false
    }

    override fun execute() {
        mainCommand.execute()
        if(!backgroundFinished) {
            backgroundCommand.execute()
            if(backgroundCommand.isFinished()) {
                backgroundCommand.end()
                backgroundFinished = true
            }
        }
    }

    override fun isFinished() = mainCommand.isFinished()

    override fun end() {
        mainCommand.end()
        if(!backgroundFinished) backgroundCommand.end()
    }

    override fun interrupted() {
        mainCommand.interrupted()
        if(!backgroundFinished) backgroundCommand.interrupted()
    }
}

class DoWhileCommand(private val command: Command, private val condition: () -> Boolean) :
        Command(*command.requirements.toTypedArray()) {
    constructor(block: () -> Unit, condition: () -> Boolean) : this(InstantCommand(block), condition)

    override fun initialize() = command.initialize()

    override fun execute() = command.execute()

    override fun isFinished(): Boolean = condition()

    override fun end() = command.end()

    override fun interrupted() = command.interrupted()
}

class InstantCommand(private val body: () -> Unit, vararg requirements: Subsystem) : Command(*requirements) {
    override fun initialize() = body()

    override fun isFinished(): Boolean = true
}

// factory functions for convenience
fun parallel(vararg commands: Command) = ParallelCommand(*commands)
fun sequential(vararg commands: Command) = SequentialCommand(*commands)
fun runUntil(backgroundCommand: Command, mainCommand: Command) = RunUntilCommand(backgroundCommand, mainCommand)
fun doWhile(command: Command, condition: () -> Boolean) = DoWhileCommand(command, condition)
fun doWhile(block: () -> Unit, condition: () -> Boolean) = DoWhileCommand(block, condition)
fun instant(body: () -> Unit, vararg requirements: Subsystem) = InstantCommand(body, *requirements)
