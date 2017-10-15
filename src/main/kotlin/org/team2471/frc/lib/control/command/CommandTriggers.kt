package org.team2471.frc.lib.control.command

import java.util.*

internal val triggerFunctions = LinkedList<() -> Unit>()

fun runCommandWhen(command: Command, condition: () -> Boolean) {
    var previousState = false

    triggerFunctions.add {
        val state = condition()
        if (state && !previousState)
            Scheduler.runCommand(command)
        previousState = state
    }
}

fun toggleCommandWhen(command: Command, condition: () -> Boolean) {
    var previousState = false

    triggerFunctions.add {
        val state = condition()

        if (state && !previousState) {
            if (command in Scheduler) {
                Scheduler.interruptCommand(command)
            } else {
                Scheduler.runCommand(command)
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
            Scheduler.runCommand(command)
        } else if (!state && previousState) {
            Scheduler.interruptCommand(command)
        }

        previousState = state
    }
}

