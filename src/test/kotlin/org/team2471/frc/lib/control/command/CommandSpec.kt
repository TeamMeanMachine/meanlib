package org.team2471.frc.lib.control.command

import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

object CommandSpec : Spek({
    given("a command") {
        val command = CountingCommand()

        on("schedule") {
            Scheduler.clearAllState()
            command.reset()

            command()

            it("should be active in the scheduler") {
                assertThat(Scheduler.contains(command)).isTrue()
            }
            it("should have been initialized") {
                assertThat(command.initCount).isEqualTo(1)
            }

        }
        on("scheduler tick") {
            Scheduler.clearAllState()
            command()
            command.reset()


            Scheduler.tick()
            it("should not have ran initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should have ran execute()") {
                assertThat(command.executeCount).isEqualTo(1)
            }

            it("should have ran isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(1)
            }

            it("should not have ran end()") {
                assertThat(command.endCount).isEqualTo(0)
            }
            it("should not have ran interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(0)
            }

            it("should not been ran from scheduler") {
                assertThat(command in Scheduler).isTrue()
            }
        }

        on("finish") {
            Scheduler.clearAllState()
            command()
            command.reset()
            command.finished = true
            Scheduler.tick()

            it("should not have called initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should have called execute()") {
                assertThat(command.executeCount).isEqualTo(1)
            }

            it("should have called isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(1)
            }

            it("should have called end()") {
                assertThat(command.endCount).isEqualTo(1)
            }
            it("should not have called interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(0)
            }
            it("should have been removed from scheduler") {
                assertThat(command in Scheduler).isFalse()
            }
        }

        on("cancel") {
            Scheduler.clearAllState()
            command.reset()
            command()
            command.cancel()

            it("should not have called initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should not have called execute()") {
                assertThat(command.executeCount).isEqualTo(0)
            }

            it("should not have called isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(0)
            }

            it("should not have called end()") {
                assertThat(command.endCount).isEqualTo(0)
            }
            it("should have called interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(1)
            }
            it("should have been removed from scheduler") {
                assertThat(command in Scheduler).isFalse()
            }
        }
    }

    given("A default command") {
        val command = CountingDefaultCommand()

        on("registering") {
            command.register()
            it("should be added to scheduler") {
                assertThat(command in Scheduler).isTrue()
            }
            it("should have called initialize") {
                assertThat(command.initCount).isEqualTo(1)
            }
        }

        on("cancel") {
            command.reset()
            command.cancel()
            it("should have called interrupted") {
                assertThat(command.interruptedCount).isEqualTo(1)
            }
            it("should be re added to scheduler") {
                assertThat(command in Scheduler).isTrue()
            }
            it("should have been reinitialized") {
                assertThat(command.initCount).isEqualTo(1)
            }
        }

        // TODO: Fix tests so they pass
        val adversaryCommand = CountingCommand(Unit)
        on("other command requiring subsystem") {
            command.reset()
            adversaryCommand.schedule()
            it("should have called interrupted") {
                assertThat(command.interruptedCount).isEqualTo(1)
            }
            it("should have been removed from scheduler") {
                assertThat(command !in Scheduler).isTrue()
            }
            it("should have initialized new command") {
                assertThat(adversaryCommand.initCount).isEqualTo(1)
            }
        }

        on("other command finishing") {
            command.reset()
            adversaryCommand.reset()
            adversaryCommand.finished = true
            Scheduler.tick()

            it("should have removed other command from scheduler") {
                assertThat(adversaryCommand !in Scheduler).isTrue()
            }

            it("should have called interrupted in other command") {
                assertThat(adversaryCommand.interruptedCount).isEqualTo(1)
            }

            it("should be re added to scheduler") {
                assertThat(command in Scheduler).isTrue()
            }

            it("should have called initialize") {
                assertThat(command.initCount).isEqualTo(1)
            }
        }
    }
})

class CountingCommand(vararg requirements: Subsystem) : Command(*requirements) {
    var initCount = 0
    var executeCount = 0
    var isFinishedCount = 0
    var endCount = 0
    var interruptedCount = 0
    var finished = false

    fun reset() {
        initCount = 0
        executeCount = 0
        isFinishedCount = 0
        endCount = 0
        interruptedCount = 0
        finished = false
    }

    override fun initialize() {
        initCount++
    }

    override fun execute() {
        executeCount++
    }

    override val isFinished: Boolean get() {
        isFinishedCount++
        return finished
    }

    override fun end() {
        endCount++
    }

    override fun interrupted() {
        interruptedCount++
    }
}

class CountingDefaultCommand : DefaultCommand(Unit) {
    var initCount = 0
    var executeCount = 0
    var isFinishedCount = 0
    var endCount = 0
    var interruptedCount = 0
    var finished = false

    fun reset() {
        initCount = 0
        executeCount = 0
        isFinishedCount = 0
        endCount = 0
        interruptedCount = 0
        finished = false
    }

    override fun initialize() {
        initCount++
    }

    override fun execute() {
        executeCount++
    }

    override val isFinished: Boolean get() {
        isFinishedCount++
        return finished
    }

    override fun end() {
        endCount++
    }

    override fun interrupted() {
        interruptedCount++
    }
}
