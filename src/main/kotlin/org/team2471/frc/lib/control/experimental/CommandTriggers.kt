package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.util.concurrent.TimeUnit

private const val POLLING_RATE = 200L

fun Command.runWhen(condition: () -> Boolean) = launch {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) launch()

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.runWhile(condition: () -> Boolean) = launch {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) launch()
        else if (previousState && !state) cancel()

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}

fun Command.toggleWhen(condition: () -> Boolean) = launch {
    var previousState = condition()
    while (isActive) {
        val state = condition()

        if (state && !previousState) {
            if (!isActive) launch()
            else cancel()
        }

        delay(POLLING_RATE, TimeUnit.MILLISECONDS)
        previousState = state
    }
}
