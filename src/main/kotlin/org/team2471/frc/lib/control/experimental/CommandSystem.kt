package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

internal object CommandSystem {
    private val mutex = Mutex()

    // stores all registered default commands
    private val defaultCommandsMap: MutableMap<Subsystem, Command> = HashMap()
    // stores all commands that are running
    private val activeCommands: MutableSet<Command> = HashSet()
    // serves as a lookup table for requirements
    private val activeRequirementsMap: MutableMap<Subsystem, Command> = HashMap()

    var dispatcher: CoroutineDispatcher = CommonPool

    internal suspend fun cleanCommand(command: Command) = mutex.withLock {
        removeCommand(command)
        processDefaultCommands(command.requirements)
    }

    internal suspend fun acquireSubsystems(command: Command, subsystems: Set<Subsystem>): Set<Command>? {
        subsystems.forEach {
            println("Command ${command.name} attempting to acquire subsystem ${it::class.simpleName}")
        }

        lateinit var conflictingCommands: Set<Command>
        mutex.withLock {
            conflictingCommands = subsystems.mapNotNull { activeRequirementsMap[it] }.toSet()
            println("${conflictingCommands.size} conflicting commands found for command ${command.name}")

            // verify that all conflicting commands may be interrupted
            if (conflictingCommands.any { !it.isCancellable }) {
                println("Uninterruptible requirement found when starting ${command.name}")
                return null
            }

            // start cancellation process
            conflictingCommands.forEach {
                it.cancel()
                removeCommand(it)
            }

            // update state
            activeCommands.add(command)
            command.requirements.forEach { activeRequirementsMap[it] = command }

            processDefaultCommands(subsystems)
        }

        return conflictingCommands
    }

    fun registerDefaultCommand(subsystem: Subsystem, defaultCommand: Command) {
        if (subsystem !in defaultCommand.requirements) {
            throw IllegalStateException("A default command must require it's subsystem.")
        }

        runBlocking(dispatcher) {
            mutex.withLock {
                defaultCommandsMap[subsystem] = defaultCommand
            }
        }
    }

    private fun removeCommand(command: Command) {
        activeCommands.remove(command)
        command.requirements.forEach { activeRequirementsMap.remove(it) }
    }

    private fun processDefaultCommands(subsystems: Set<Subsystem>) {
        subsystems
                .filter { it !in activeRequirementsMap }
                .mapNotNull { defaultCommandsMap[it] }
                .forEach { it.launch() }
    }

    // for testing
    internal val commandsRunning get() = activeCommands.size

    internal fun clearAllState() {
        runBlocking {
            mutex.withLock {
                defaultCommandsMap.clear()
                activeCommands.forEach { it.cancel() }
                activeCommands.forEach { it.join() }
                activeRequirementsMap.clear()
                activeCommands.clear()
            }
        }
    }
}
