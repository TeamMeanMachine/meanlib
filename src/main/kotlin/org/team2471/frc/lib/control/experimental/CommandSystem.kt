package org.team2471.frc.lib.control.experimental

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.networktables.NetworkTable
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext

typealias Subsystem = Any

/**
 * All global state related to the command system is kept and managed here.
 */
object CommandSystem {
    internal val defaultCommands: MutableMap<Subsystem, Command> = HashMap()
    internal val requirementsMap: MutableMap<Subsystem, Command> = HashMap()
    internal val activeCommands: MutableSet<Command> = HashSet()

    private var contextInstance: CoroutineContext? = null

    val Context: CoroutineContext by lazy {
        val instance = contextInstance ?: throw IllegalStateException("The command context was accessed before it was initialized. " +
                "Command.initCoroutineContext is either being called too late or not being called at all.")
        launch(instance) {
            val table = NetworkTable.getTable("Command System")
            while(true) {
                synchronized(this@CommandSystem) {
                    table.putNumber("Commands running", commandsRunning.toDouble())
                    table.putStringArray("Commands", commands)
                    table.putStringArray("Requirements", requirements)
                }
                delay(500)
            }
        }

        instance
    }

    var isEnabled = false
        set(value)  {
            synchronized(this) {
                field = value
                if (value) {
                    println("Command system enabled. Starting default commands...")
                    startDefaultCommands()
                } else {
                    println("Command system disabled. Cancelling all commands...")
                    cancelAllCommands()
                }
            }
        }

    val commandsRunning get() = activeCommands.size
    val commands get() = activeCommands.map { it.label }.toTypedArray()
    val requirements get() = requirementsMap.map { (subsystem, command) ->
        "${subsystem::class.simpleName}: $command" }.toTypedArray()

    fun initCoroutineContext(commandContext: CoroutineContext) {
        if (contextInstance != null) {
            return DriverStation.reportWarning("The command context was initialized more than once. This call will be ignored.", true)
        }
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
        activeCommands.forEach {
            println("Command ${it.label} canceled: ${it.job?.cancel()}")
        }
    }

    private fun startDefaultCommands() = synchronized(this) {
        defaultCommands.forEach { _, command -> command() }
    }

    private fun addDefaultCommand(subsystem: Subsystem, command: Command) = synchronized(this) {
        defaultCommands[subsystem] = command

        if (command !in activeCommands) command()
    }

    internal fun dispatchCommand(command: Command): Boolean = synchronized(this) {
        if (!isEnabled) {
            println("Command ${command.label} could not be started because the command system is disabled")
            return false
        }

        if (command.isRunning) {
            println("Command ${command.label} could not be started because it is already running")
            return false
        }

        val conflictingCommands = command.requirements.mapNotNull { requirementsMap[it] }
        if (conflictingCommands.any { !it.isCancelable }) {
            println("Command ${command.label} could not be started because of an un cancellable conflict")
            return false
        }
        println("Starting command ${command.label} with ${conflictingCommands.size} conflicting commands")

        command.requirements.forEach { requirementsMap[it] = command }
        activeCommands.add(command)

        conflictingCommands.forEach { it.cancel() }

        command.job = launch(Context) {
            // wait for canceled commands to finish
            conflictingCommands.forEach { it.join() }
            try {
                println("Starting command ${command.label}")
                command.body(this@launch)
                println("Command ${command.label} completed gracefully.")
            } finally {
                synchronized(this) {
                    println("Cleaning up command ${command.label}")
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

        return true
    }

    // for testing
    @Synchronized internal fun clearAllState() {
        isEnabled = false
        runBlocking { activeCommands.forEach { println("Joining command..."); it.join() } }
        defaultCommands.clear()
        activeCommands.clear()
        requirementsMap.clear()
    }
}
