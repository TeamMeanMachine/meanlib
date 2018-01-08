package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.sync.Mutex
import kotlinx.coroutines.experimental.sync.withLock
import kotlin.coroutines.experimental.AbstractCoroutineContextElement
import kotlin.coroutines.experimental.CoroutineContext

typealias Subsystem = Any

class Command(val name: String,
              vararg requirements: Subsystem,
              internal val isCancellable: Boolean = true,
              private val body: suspend CoroutineScope.() -> Unit) {
    private val mutex = Mutex()

    internal val requirements: Set<Subsystem> = hashSetOf(*requirements)

    private var coroutine: Job? = null

    operator suspend fun invoke(context: CoroutineContext, join: Boolean = true) {
        val parentRequirements: Set<Subsystem> = context[Requirements] ?: emptySet()
        val ourRequirements = requirements - parentRequirements

        mutex.withLock {
            val conflictingCommands = CommandSystem.acquireSubsystems(this, ourRequirements) ?: run {
                println("Command $name failed to acquire it's requirements.")
                return
            }

            coroutine = launch(context + Requirements(parentRequirements + ourRequirements)) {
                if (conflictingCommands.isNotEmpty()) {
                    println("Command $name waiting for conflicts to resolve...")
                    conflictingCommands.forEach { it.join() }
                }

                println("Starting command $name")
                body(this@launch)
                CommandSystem.cleanCommand(this@Command)
            }
        }

        if(join) coroutine?.join()
    }

    fun launch(dispatcher: CoroutineDispatcher = CommandSystem.dispatcher) {
        launch(dispatcher) { invoke(coroutineContext, false) }
    }

    fun cancel(cause: Throwable? = null) = coroutine?.cancel(cause)

    suspend fun join() = coroutine?.join()

    val isActive get() = coroutine?.isActive == true
}

internal class Requirements(requirements: Set<Subsystem>) :
        AbstractCoroutineContextElement(Key),
        Set<Subsystem> by requirements {

    companion object Key : CoroutineContext.Key<Requirements>

    /**
     * A key of this coroutine context element.
     */
    override val key: CoroutineContext.Key<*> get() = Key
}
