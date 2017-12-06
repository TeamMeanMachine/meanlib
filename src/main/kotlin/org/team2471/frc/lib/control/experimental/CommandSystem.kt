package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.launch
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

// anything can be a subsystem
typealias Subsystem = Any

/**
 * All global state related to the command system is kept and managed here.
 */
object CommandSystem {
    private val defaultCommands: MutableMap<Subsystem, Command> = HashMap()
    private val requirementsMap: MutableMap<Subsystem, Command> = HashMap()
    private val activeCommands: MutableSet<Command> = HashSet()

    private var contextInstance: CoroutineContext? = null

    val Context: CoroutineContext by lazy {
        contextInstance ?: throw IllegalStateException("The command context was accessed before it was initialized. " +
                "Command.initCoroutineContext is either being called too late or not being called at all.")
    }

    var isEnabled = false
        set(value) = synchronized(this) {
            if (field == value) return
            field = value
            if (value) startDefaultCommands() else cancelAllCommands()
        }

    fun initCoroutineContext(commandContext: CoroutineContext) {
        if (contextInstance != null)
            throw IllegalStateException("The command context can only be initialized once.")
        contextInstance = commandContext
    }

    /**
     * Registers the given [command] as the subsystem's default command.
     *
     * When a command that requires a subsystem terminates, the subsystem's default command will be started if present.
     *
     * If a command is not running that requires the calling subsystem, the provided command will be started.
     */
    fun registerDefaultCommand(subsystem: Subsystem, command: Command) {
        if (subsystem !in command.requirements)
            throw IllegalArgumentException("A subsystem cannot register a default command that doesn't require it.")
        else if (command.requirements.size != 1)
            throw IllegalArgumentException("A default command must require exactly one subsystem.")

        addDefaultCommand(subsystem, command)
    }

    private fun cancelAllCommands() = synchronized(this) {
        activeCommands.forEach { it.cancel() }
    }

    private fun startDefaultCommands() = synchronized(this) {
        defaultCommands.forEach { _, command -> command() }
    }

    private fun addDefaultCommand(subsystem: Subsystem, command: Command) = synchronized(this) {
        defaultCommands[subsystem] = command

        if (command !in activeCommands) command()
    }

    internal fun startCommand(command: Command): Boolean = synchronized(this) {
        if (!isEnabled || command.isRunning) return false

        val conflictingCommands = command.requirements.mapNotNull { requirementsMap[it] }
        if (conflictingCommands.any { !it.isCancelable }) return false

        command.requirements.forEach { requirementsMap[it] = command }
        activeCommands.add(command)

        conflictingCommands.forEach { it.cancel() }

        command.job = launch(Context) {
            // wait for canceled commands to finish
            conflictingCommands.forEach { it.join() }
            command.body(Command.Scope(command, this@launch))
            cleanupCommand(command)
        }

        return true
    }

    private fun cleanupCommand(command: Command) = synchronized(this) {
        println("Cleaning up command ${command.name}")
        activeCommands.remove(command)
        command.job = null
        // remove requirements that haven't been been overtaken
        command.requirements.filter { requirementsMap[it] == command }.forEach { subsystem ->
            requirementsMap.remove(command)
            if (isEnabled) defaultCommands[subsystem]?.invoke()
        }
    }
}
