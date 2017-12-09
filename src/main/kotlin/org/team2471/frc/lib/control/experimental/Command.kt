package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job

class Command(val name: String, vararg requirements: Subsystem, val isCancelable: Boolean = true,
              internal val body: suspend CoroutineScope.() -> Unit) {
    val label = "$name@${hashCode()}"

    val isRunning get() = job?.isActive == true

    val isCanceled get() = job?.isCancelled == true

    internal val requirements: Set<Subsystem> = hashSetOf(*requirements)

    internal var job: Job? = null

    suspend fun invokeAndJoin() {
        if (invoke()) job?.join()
    }

    suspend fun join() = job?.join()

    /**
     * Cancel current command if it is running.
     *
     * Returns true if the command was canceled as a result of this call.
     */
    fun cancel(cause: Throwable? = null): Boolean = if (isCancelable) {
        job?.cancel(cause) ?: false
    } else {
        false
    }

    /**
     * Attempts to start the command inside a job.
     *
     * If the command is running or one if it's required subsystems cannot be acquired, the command will not be started.
     *
     * A subsystem cannot be acquired if it's current command is not interrupible.
     *
     * If all subsystems can be acquired, commands requiring the subsystems will be canceled if present.
     */
    operator fun invoke(): Boolean = CommandSystem.dispatchCommand(this)
}
