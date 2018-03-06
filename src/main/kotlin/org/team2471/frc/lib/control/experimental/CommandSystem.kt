package org.team2471.frc.lib.control.experimental

import com.sun.org.apache.xpath.internal.operations.Bool
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotState
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock

object CommandSystem {
    private val mutex = Mutex()

    // stores all registered default commands
    private val defaultCommandsMap: MutableMap<Subsystem, Command> = HashMap()
    // stores all commands that are running
    private val activeCommands: MutableSet<Command> = HashSet()
    // serves as a lookup table for requirements
    private val activeRequirementsMap: MutableMap<Subsystem, Command> = HashMap()

    private val table = NetworkTableInstance.getDefault().getTable("Command System")

    val isEnabled: Boolean
        get() = RobotState.isEnabled()

    init {
        launch {
            val activeCommandsEntry = table.getEntry("Active Commands")
            val activeRequirementsEntry = table.getEntry("Active Requirements")
            val defaultCommandsEntry = table.getEntry("Default Commands")
            val enabledEntry = table.getEntry("Enabled")

            var prevEnabled = isEnabled
            periodic(100) {
                mutex.withLock {
                    val isEnabled = isEnabled
                    // Cancel all non-default commands on robot disable
                    if (isEnabled && !prevEnabled) {
                        defaultCommandsMap.forEach { subsystem, defaultCommand ->
                            if (!activeRequirementsMap.contains(subsystem)) {
                                defaultCommand.launch()
                            }
                        }
                    }
                    prevEnabled = isEnabled

                    if (!isEnabled) {
                        activeCommands.forEach { it.cancel() }
                    }

                    enabledEntry.setString(if (isEnabled) "ENABLED" else "DISABLED")
                    activeCommandsEntry.setStringArray(activeCommands.map { it.name }.toTypedArray())
                    activeRequirementsEntry.setStringArray(activeRequirementsMap.map { (subsystem, command) ->
                        "${subsystem::class.simpleName} -> ${command.name}"
                    }.toTypedArray())
                    defaultCommandsEntry.setStringArray(defaultCommandsMap.map { (subsystem, command) ->
                        "${subsystem::class.simpleName} -> ${command.name}"
                    }.toTypedArray())
                }
            }
        }
    }

    internal suspend fun cleanCommand(command: Command) = mutex.withLock {
        println("Cleaning command ${command.name}")
        activeCommands.remove(command)

        command.requirements
                .forEach { subsystem ->
                    if (activeRequirementsMap[subsystem] == command) {
                        activeRequirementsMap.remove(subsystem)

                        if (isEnabled) defaultCommandsMap[subsystem]?.launch()
                    }
                }
    }

    /**
     * This method has 3 purposes:
     *  1. Verify that it is legal to run the [command] (no not-cancellable conflicts)
     *  2. Update the [CommandSystem] global state
     *  3. Return all of the conflicts of the [command], so that the [command] can wait for them
     *     to complete in it's own coroutine.
     *
     * @return All conflicting commands to [command] or null if the command could not be started.
     */
    internal suspend fun acquireSubsystems(command: Command, subsystems: Set<Subsystem>): Set<Command>? {
        subsystems.forEach {
            println("Command ${command.name} attempting to acquire subsystem ${it::class.simpleName}")
        }

        mutex.withLock {
            val conflictingCommands = subsystems.mapNotNull { activeRequirementsMap[it] }.toSet()
            println("${conflictingCommands.size} conflicting commands found for command ${command.name}")

            // verify that all conflicting commands may be interrupted
            if (conflictingCommands.any { !it.isCancellable }) {
                println("Uninterruptible requirement found when starting ${command.name}")
                return null
            }

            // Update state.
            // It is important to do take over the requirements before you cancel the conflicts.
            // If you don't, a default command for one of our requirements may be launched.
            command.requirements.forEach { activeRequirementsMap[it] = command }
            activeCommands.add(command)

            // start cancellation process
            conflictingCommands.forEach { conflict ->
                println("Cancelling command ${conflict.name}")
                conflict.cancel()
            }

            return conflictingCommands
        }
    }

    /**
     * Registers [command] as the default command of [subsystem] in the [CommandSystem].
     *
     * If [command] does not have [subsystem] as one of it's requirements, an exception will be thrown.
     *
     * [command] will also be launched if none of it's requirements are being used by other commands.
     */
    fun registerDefaultCommand(subsystem: Subsystem, command: Command) {
        if (subsystem !in command.requirements) {
            throw IllegalArgumentException("A default command must require it's subsystem.")
        }

        runBlocking {
            mutex.withLock {
                defaultCommandsMap[subsystem] = command
                if (isEnabled && command.requirements.all { activeRequirementsMap[it] == null }) command.launch()
            }
        }
    }
}
