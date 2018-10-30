@file:Suppress("EXPERIMENTAL_API_USAGE")

package org.team2471.frc.lib.resources

import kotlinx.coroutines.Job

open class Resource(val name: String, startEnabled: Boolean = true) {
    internal var activeJob: Job? = null

    var isEnabled: Boolean = false
        internal set

    init {
        if (startEnabled) enable()
    }
}

abstract class DaemonResource(name: String, startEnabled: Boolean = true) : Resource(name, startEnabled) {
    abstract suspend fun onReset()
}

fun Resource.enable() = EventHandler.enableResource(this)

fun Resource.disable() =
    EventHandler.disableResource(this)

suspend fun <R> use(vararg resources: Resource, cancelConflicts: Boolean = true, body: suspend () -> R) =
    EventHandler.useResources(setOf(*resources), cancelConflicts, body)
