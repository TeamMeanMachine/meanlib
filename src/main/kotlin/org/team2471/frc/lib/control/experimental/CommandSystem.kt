package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.networktables.NetworkTable
import kotlinx.coroutines.experimental.delay
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
        val instance = contextInstance ?: throw IllegalStateException("The command context was accessed before it was initialized. " +
                "Command.initCoroutineContext is either being called too late or not being called at all.")
        launch(instance) {
            val table = NetworkTable.getTable("Command System")
            while(true) {
                synchronized(this@CommandSystem) {
                    table.putNumber("Command count", activeCommands.size.toDouble())
                    table.putStringArray("Commands", activeCommands.map { it.name }.toTypedArray())
                    table.putStringArray("Requirements",
                            requirementsMap.map { (s, c) ->  "${s::class.simpleName}: $c" }.toTypedArray())
                }
                delay(500)
            }
        }

        instance
    }

    var isEnabled = false
        set(value)  {
            if (field == value) return
            synchronized(this) { field = value }
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
        println("Starting command ${command.name} with ${conflictingCommands.size} conflicting commands")

        command.requirements.forEach { requirementsMap[it] = command }
        activeCommands.add(command)

        conflictingCommands.forEach { it.cancel() }

        command.job = launch(Context) {
            // wait for canceled commands to finish
            conflictingCommands.forEach { it.join() }
            try {
                command.body(this@launch)
            } finally {
                cleanupCommand(command)
            }
        }

        return true
    }

    private fun cleanupCommand(command: Command) = synchronized(this) {
        println("Cleaning up command ${command.name}")

        synchronized(this) {
            activeCommands.remove(command)
            command.job = null
            // remove requirements that haven't been been overtaken
            command.requirements.filter { requirementsMap[it] == command }.forEach { subsystem ->
                requirementsMap.remove(command)
                if (isEnabled) defaultCommands[subsystem]?.invoke()
            }
        }
    }
}
