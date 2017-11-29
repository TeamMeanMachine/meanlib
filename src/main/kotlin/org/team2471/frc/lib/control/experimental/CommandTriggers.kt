package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

private const val POLLING_RATE = 200L

fun Command.runWhen(coroutineDispatcher: CoroutineDispatcher, condition: () -> Boolean) = launch(coroutineDispatcher) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) invoke(coroutineDispatcher)

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.runWhile(coroutineDispatcher: CoroutineDispatcher, condition: () -> Boolean) = launch(coroutineDispatcher) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) invoke(coroutineDispatcher)
        else if (previousState && !state) cancel()

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.toggleWhen(coroutineDispatcher: CoroutineDispatcher, condition: () -> Boolean) = launch(coroutineDispatcher) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) {
            if (!isRunning) invoke(coroutineDispatcher)
            else cancel()
        }

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}
