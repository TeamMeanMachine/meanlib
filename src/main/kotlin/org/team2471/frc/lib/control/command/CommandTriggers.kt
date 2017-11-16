package org.team2471.frc.lib.control.command

import java.util.*

internal val triggerFunctions = LinkedList<() -> Unit>()

fun runCommandWhen(command: Command, condition: () -> Boolean) {
    var previousState = false

    triggerFunctions.add {
        val state = condition()
        if (state && !previousState)
            command.schedule()
        previousState = state
    }
}

fun toggleCommandWhen(command: Command, condition: () -> Boolean) {
    var previousState = false

    triggerFunctions.add {
        val state = condition()

        if (state && !previousState) {
            if (command in Scheduler) {
                command.cancel()
            } else {
                command.schedule()
            }
        }

        previousState = state
    }
}

fun runCommandWhile(command: Command, condition: () -> Boolean) {
    var previousState = false

    triggerFunctions.add {
        val state = condition()

        if (state && !previousState) {
            command.schedule()
        } else if (!state && previousState) {
            command.cancel()
        }

        previousState = state
    }
}

