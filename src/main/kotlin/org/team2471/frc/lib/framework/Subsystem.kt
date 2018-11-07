@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc.lib.framework

import kotlinx.coroutines.Job
import org.team2471.frc.lib.framework.internal.EventHandler

open class Subsystem(val name: String, startEnabled: Boolean = true) {
    internal var activeJob: Job? = null

    var isEnabled: Boolean = false
        internal set

    init {
        if (startEnabled) enable()
    }
}

abstract class DaemonSubsystem(name: String, startEnabled: Boolean = true) : Subsystem(name, startEnabled) {
    abstract suspend fun default()
}

fun Subsystem.enable() = EventHandler.enableSubsystem(this)

fun Subsystem.disable() =
    EventHandler.disableSubsystem(this)

suspend fun <R> use(vararg subsystems: Subsystem, cancelConflicts: Boolean = true, body: suspend () -> R) =
    EventHandler.useSubsystems(setOf(*subsystems), cancelConflicts, body)
