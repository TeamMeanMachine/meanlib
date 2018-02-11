package org.team2471.frc.lib.control.experimental

import java.util.concurrent.ConcurrentLinkedQueue

object EventMapper {
    private val entries = ConcurrentLinkedQueue<EventMapperEntry>()

    internal fun addEntry(entry: EventMapperEntry) = entries.add(entry)

    fun tick() = entries.forEach { it() }
}

private typealias EventMapperEntry = () -> Unit

private class RunWhenEntry(private val command: Command, private val condition: () -> Boolean) : EventMapperEntry {
    private var previousState = condition()

    override fun invoke() {
        val state = condition()

        if (state && !previousState) command.launch()

        previousState = state
    }
}

private class RunWhileEntry(private val command: Command, private val condition: () -> Boolean) : EventMapperEntry {
    private var previousState = condition()

    override fun invoke() {
        val state = condition()

        if (state && !previousState) command.launch()
        else if (previousState && !state) command.cancel()

        previousState = state
    }
}

private class ToggleWhenEntry(private val command: Command, private val condition: () -> Boolean) : EventMapperEntry {
    private var previousState = condition()

    override fun invoke() {
        val state = condition()

        if (state && !previousState) {
            if (!command.isActive) {
                println("Toggle")
                command.launch()
            } else {
                println("Untoggle")
                command.cancel()
            }
        }

        previousState = state
    }
}

fun Command.runWhen(condition: () -> Boolean) = EventMapper.addEntry(RunWhenEntry(this, condition))

fun Command.runWhile(condition: () -> Boolean) = EventMapper.addEntry(RunWhileEntry(this, condition))

fun Command.toggleWhen(condition: () -> Boolean) = EventMapper.addEntry(ToggleWhenEntry(this, condition))
