package org.team2471.frc.lib.control.experimental.next

import kotlinx.coroutines.experimental.Job

open class Resource(val name: String, isEnabled: Boolean = true) {
    internal var activeJob: Job? = null

    var isEnabled: Boolean = true
        internal set
}

abstract class DaemonResource(name: String) : Resource(name) {
    abstract suspend fun onReset()
}

fun Resource.enable() = EventHandler.enableResource(this)

fun Resource.disable() = EventHandler.disableResource(this)

