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
            command.reset()
            Scheduler.clear()

            command()

            it("should be active in the scheduler") {
                assertThat(Scheduler.contains(command)).isTrue()
            }
            it("should have been initialized") {
                assertThat(command.initCount).isEqualTo(1)
            }

        }
        on("scheduler tick") {
            Scheduler.clear()
            command()
            command.reset()


            Scheduler.tick()
            it("should not have schedule initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should have schedule execute()") {
                assertThat(command.executeCount).isEqualTo(1)
            }

            it("should have schedule isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(1)
            }

            it("should not have schedule end()") {
                assertThat(command.endCount).isEqualTo(0)
            }
            it("should not have schedule interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(0)
            }

            it("should not been removed from scheduler") {
                assertThat(command in Scheduler).isTrue()
            }
        }

        on("finish") {
            Scheduler.clear()
            command()
            command.reset()
            command.finished = true
            Scheduler.tick()

            it("should not have schedule initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should have schedule execute()") {
                assertThat(command.executeCount).isEqualTo(1)
            }

            it("should have schedule isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(1)
            }

            it("should have schedule end()") {
                assertThat(command.endCount).isEqualTo(1)
            }
            it("should not have schedule interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(0)
            }
            it("should have been removed from scheduler") {
                assertThat(command in Scheduler).isFalse()
            }
        }

        on("cancel") {
            Scheduler.clear()
            command()
            command.reset()

            command.cancel()

            it("should not have schedule initialize()") {
                assertThat(command.initCount).isEqualTo(0)
            }
            it("should not have schedule execute()") {
                assertThat(command.executeCount).isEqualTo(0)
            }

            it("should not have schedule isFinished()") {
                assertThat(command.isFinishedCount).isEqualTo(0)
            }

            it("should not have schedule end()") {
                assertThat(command.endCount).isEqualTo(0)
            }
            it("should have schedule interrupted()") {
                assertThat(command.interruptedCount).isEqualTo(1)
            }
            it("should have been removed from scheduler") {
                assertThat(command in Scheduler).isFalse()
            }
        }
    }
})

class CountingCommand : Command() {
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
