package org.team2471.frc.lib.control.experimental

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.delay
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.shouldNotThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import java.util.concurrent.TimeUnit

object CommandSystemTest : Spek({
    given("the command system object") {
        on("not initializing the context") {
            it("should throw an exception if context is accessed but isn't initialized") {
                ({ CommandSystem.Context }) shouldThrow IllegalStateException::class
            }
        }

        on("initializing the context") {
            CommandSystem.initCoroutineContext(CommonPool)

            it("should not throw an exception if context is accessed") {
                ({ CommandSystem.Context }) shouldNotThrow  IllegalStateException::class
            }
        }
    }

    given("a command") {
        val command = Command("Test") { delay(2, TimeUnit.SECONDS) }

        it("should not be running if it was not invoked") {
            CommandSystem.clearAllState()
            CommandSystem.isEnabled = true

            command.isRunning shouldBe false
        }

        on("invoking the command") {
            CommandSystem.clearAllState()
            CommandSystem.isEnabled = true

            command()
            it("should be running") {
                command.isRunning shouldBe true
                CommandSystem.commandsRunning shouldBe  1
            }
        }

        on("invoking the command repeatedly") {
            CommandSystem.clearAllState()
            CommandSystem.isEnabled = true

            val firstCall = command()
            val secondCall = command()
            val thirdCall = command()

            it("should return true in first call") {
                firstCall shouldBe true
            }

            it("should return false on extra calls") {
                secondCall shouldBe false
                thirdCall shouldBe false
            }

            it("should still be running") {
                command.isRunning shouldBe true
            }

            it("should not be duplicated in command system") {
                CommandSystem.commandsRunning shouldBe 1
            }
        }

        on("waiting for the command to complete") {
            CommandSystem.clearAllState()
            CommandSystem.isEnabled = true

            command()
            Thread.sleep(2500)
            it("should be completed") {
                CommandSystem.commandsRunning shouldBe 0
                command.isRunning shouldBe false
            }
        }
    }

    given("a subsystem with a default command") {
        val subsystem = Unit
        val defaultCommand = Command("Test Default", subsystem) { delay(Long.MAX_VALUE) }

        on("enabling the command system") {
            CommandSystem.clearAllState()
            CommandSystem.registerDefaultCommand(subsystem, defaultCommand)
            CommandSystem.isEnabled = true

            it("should invoked automatically") {
                defaultCommand.isRunning shouldBe true
            }
        }

        on("disabling the command system") {
            CommandSystem.clearAllState()
            CommandSystem.registerDefaultCommand(subsystem, defaultCommand)
            CommandSystem.isEnabled = false

            it("should not be invoked automatically") {
                defaultCommand.isRunning shouldBe false
            }
        }

        given("another command that requires the same subsystem") {
            val otherCommand = Command("Other Command", subsystem) { delay(1000) }

            on("starting the other command while the default command is running") {
                CommandSystem.clearAllState()
                CommandSystem.registerDefaultCommand(subsystem, defaultCommand)
                CommandSystem.isEnabled = true

                otherCommand()

                it("should take over the default command") {
                    otherCommand.isRunning shouldBe true
                    defaultCommand.isRunning shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }
            }

            on("waiting for the other command to finish") {
                CommandSystem.clearAllState()
                CommandSystem.registerDefaultCommand(subsystem, defaultCommand)
                CommandSystem.isEnabled = true

                otherCommand()

                it("should take over the default command") {
                    otherCommand.isRunning shouldBe true
                    defaultCommand.isRunning shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }

                it("should return control back to default command") {
                    Thread.sleep(1500)
                    defaultCommand.isRunning shouldBe true
                    otherCommand.isRunning shouldBe false
                    CommandSystem.commandsRunning shouldBe 1
                }
            }
        }
    }
})
