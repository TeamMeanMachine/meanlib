package org.team2471.frc.lib.commands

import org.wpilib.command3.Command
import org.wpilib.command3.Coroutine
import org.wpilib.system.Timer

/** Utility functions for [Coroutine]s. Which are used inside [Command] actions, and allow for muti-threading "like" features. Ex: yield() await() */
// Different from [kotlinx.coroutines] which are also installed. (Those use the "suspend" keyword)

// TODO: Figure out delay function. Perhaps "extend" or wrap [Coroutine] and use @RestrictsSuspension

class PeriodicScope {
    @PublishedApi
    internal var isDone = false

    /** Ends a periodic scope */
    fun stop() {
        isDone = true
    }
}

/**
 * Runs the provided [body] of code periodically per loop.
 *
 * @param loopCount Number of times to run the body. -1 for infinite.
 *
 * The provided [body] loop will continue to loop until [PeriodicScope.stop] is called, or an exception is thrown.
 * Note that if [PeriodicScope.stop] is called, the body will continue to run to the end of the loop. If your
 * intention is to exit the code early, insert a return after calling [PeriodicScope.stop].
 */
inline fun Coroutine.periodic(
    loopCount: Int = -1,
    crossinline body: PeriodicScope.(Int) -> Unit
) {
    val scope = PeriodicScope()
    var loopIndex = 0
    while (loopCount == -1 || loopIndex < loopCount) {
        scope.body(loopIndex)
        if (scope.isDone) break
        loopIndex++
        yield()
    }
}

/**
 * Runs the provided [body] of code periodically per loop.
 *
 * The provided [body] loop will continue to loop until [PeriodicScope.stop] is called, an exception is thrown, or [timeout] seconds is reached.
 * Note that if [PeriodicScope.stop] is called, the body will continue to run to the end of the loop. If your
 * intention is to exit the code early, insert a return after calling [PeriodicScope.stop].
 */
@IgnorableReturnValue
inline fun Coroutine.periodicTimeout(
    timeout: Double,
    crossinline body: PeriodicScope.(Double) -> Unit
): Boolean {
    val timer = Timer()
    var timedOut = false
    timer.start()
    periodic {
        val t = timer.get()
        if (t > timeout) {
            timedOut = true
            stop()
        } else {
            body(t)
        }
    }
    return timedOut
}

@IgnorableReturnValue
fun Coroutine.parallel(vararg commands: Command) = awaitAll(*commands)!!

@IgnorableReturnValue
fun Coroutine.parallel(vararg blocks: Coroutine.() -> Unit) =
    parallel(*blocks.mapIndexed { index, coroutine -> command("unnamedParallel$index") { coroutine() }}.toTypedArray())