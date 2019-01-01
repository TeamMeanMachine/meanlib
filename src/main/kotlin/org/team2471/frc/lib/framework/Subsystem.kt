@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc.lib.framework

import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.Job
import org.team2471.frc.lib.coroutines.halt
import org.team2471.frc.lib.framework.internal.EventHandler

open class Subsystem(
    val name: String,
    startEnabled: Boolean = true,
    internal val defaultFunction: (suspend () -> Unit)? = null
) {
    private val table = NetworkTableInstance.getDefault().getTable("Subsystems").getSubTable(name)
    private val enabledEntry = table.getEntry("Enabled")

    internal var activeJob: Job? = null

    var isEnabled: Boolean = false
        internal set(value) {
            field = value
            enabledEntry.setBoolean(value)
        }

    // always called when a coroutine using the subsystem finishes, cancelled or otherwise.
    open fun reset() = Unit

    init {
        enabledEntry.setBoolean(false)
        if (startEnabled) enable()
    }
}

fun Subsystem.enable() = EventHandler.enableSubsystem(this)

fun Subsystem.disable() =
    EventHandler.disableSubsystem(this)

suspend fun <R> use(vararg subsystems: Subsystem, cancelConflicts: Boolean = true, body: suspend () -> R) =
    EventHandler.useSubsystems(setOf(*subsystems), cancelConflicts, body)
