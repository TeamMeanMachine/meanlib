package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

private const val POLLING_RATE = 200L

fun Command.runWhen(condition: () -> Boolean) = launch(CommandSystem.Context) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) invoke()

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.runWhile(condition: () -> Boolean) = launch(CommandSystem.Context) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) invoke()
        else if (previousState && !state) cancel()

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.toggleWhen(condition: () -> Boolean) = launch(CommandSystem.Context) {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) {
            if (!isRunning) invoke()
            else cancel()
        }

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}
