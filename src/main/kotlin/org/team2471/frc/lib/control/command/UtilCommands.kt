package org.team2471.frc.lib.control.command

class ConditionalCommand(private val command: Command, private val condition: () -> Boolean) :
        Command(*command.requirements.toTypedArray()) {

    var running = false

    override fun initialize() {
        running = condition()
        if(running) command.initialize()
    }

    override fun execute() {
        if(running) command.execute()
    }

    override val isFinished: Boolean get() = if(running) command.isFinished else true

    override fun end() {
        if(running) command.end()
    }

    override fun interrupted() {
        if(running) command.interrupted()
    }
}

class InstantCommand(private val body: () -> Unit) : Command() {
    override val isFinished = true

    override fun initialize() = body()
}
