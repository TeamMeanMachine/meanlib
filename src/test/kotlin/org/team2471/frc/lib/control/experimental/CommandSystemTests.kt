package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.delay
import org.amshove.kluent.shouldBe
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit

object CommandSystemTest : Spek({
    given("a command") {
        val command = Command("Test") { delay(2, TimeUnit.SECONDS) }

        it("should not be running if it was not invoked") {
            CommandSystem.clearAllState()

            command.isActive shouldBe false
        }

        on("invoking the command") {
            CommandSystem.clearAllState()

            it("should be running") {
                command.launch()
                Thread.sleep(500)
                command.isActive shouldBe true
                CommandSystem.commandsRunning shouldBe  1
            }
        }

        on("waiting for the command to complete") {
            CommandSystem.clearAllState()

            command.launch()
            Thread.sleep(2500)
            it("should be completed") {
                CommandSystem.commandsRunning shouldBe 0
                command.isActive shouldBe false
            }
        }
    }

    given("a subsystem with a default command") {
        val subsystem = Unit
        val defaultCommand = Command("Test Default", subsystem) { delay(Long.MAX_VALUE) }

        on("enabling the command system") {
            CommandSystem.clearAllState()
            CommandSystem.registerDefaultCommand(subsystem, defaultCommand)

            it("should invoked automatically") {
                defaultCommand.isActive shouldBe true
            }
        }

        on("disabling the command system") {
            CommandSystem.clearAllState()
            CommandSystem.registerDefaultCommand(subsystem, defaultCommand)

            it("should not be invoked automatically") {
                defaultCommand.isActive shouldBe false
            }
        }

        given("another command that requires the same subsystem") {
            val otherCommand = Command("Other Command", subsystem) { delay(1000) }

            on("starting the other command while the default command is running") {
                CommandSystem.clearAllState()
                CommandSystem.registerDefaultCommand(subsystem, defaultCommand)

                otherCommand.launch()

                it("should take over the default command") {
                    otherCommand.isActive shouldBe true
                    defaultCommand.isActive shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }
            }

            on("waiting for the other command to finish") {
                CommandSystem.clearAllState()
                CommandSystem.registerDefaultCommand(subsystem, defaultCommand)

                otherCommand.launch()

                it("should take over the default command") {
                    otherCommand.isActive shouldBe true
                    defaultCommand.isActive shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }

                it("should return control back to default command") {
                    Thread.sleep(1500)
                    defaultCommand.isActive shouldBe true
                    otherCommand.isActive shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }
            }
        }
    }
})
