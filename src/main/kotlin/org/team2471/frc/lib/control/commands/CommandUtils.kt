package org.team2471.frc.lib.control.commands

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer
import edu.wpi.first.wpilibj.Watchdog
import org.team2471.frc.lib.coroutines.measureTimeFPGA
import org.team2471.frc.lib.units.seconds
import org.wpilib.commands3.Command
import org.wpilib.commands3.Coroutine
import org.wpilib.commands3.Mechanism
import org.wpilib.commands3.NeedsNameBuilderStage

class PeriodicScope {
    @PublishedApi
    internal var isDone = false

    fun stop() {
        isDone = true
    }
}

fun use(name: String, vararg mechanisms: Mechanism, body: Coroutine.() -> Unit): Command = Command.requiring(setOf(*mechanisms)).executing(body).named(name)
fun use(name: String, vararg mechanisms: Mechanism, body: Coroutine.() -> Unit, onCancel: () -> Unit): Command = Command.requiring(setOf(*mechanisms)).executing(body).whenCanceled(onCancel).named(name)
fun use(vararg mechanisms: Mechanism, body: Coroutine.() -> Unit): NeedsNameBuilderStage = Command.requiring(setOf(*mechanisms)).executing(body)
fun (Coroutine.() -> Unit).use(name: String, vararg mechanisms: Mechanism, onCancel: () -> Unit = {}) = use(name, *mechanisms, body = this, onCancel = onCancel)

fun NeedsNameBuilderStage.named(name: String, onCancel: () -> Unit = {}): Command = whenCanceled(onCancel).named(name)

inline fun Coroutine.periodic(
    period: Double = 0.02,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
) {
    val scope = PeriodicScope()

    val watchdog = if (watchOverrunName != null) {
        Watchdog(period) { DriverStation.reportWarning("Periodic loop $watchOverrunName overrun", true) }
    } else {
        null
    }

    while (true) {
        watchdog?.reset()
        val dt = measureTimeFPGA {
            body(scope)
        }
        if (scope.isDone) break
        val remainder = period - dt
        if (remainder <= 0.0) {
            yield()
        } else {
            wait(remainder.seconds)
        }
    }
}

inline fun Coroutine.periodicTimeout(
    timeout: Double,
    period: Double = 0.02,
    watchOverrunName: String? = null,
    crossinline body: PeriodicScope.() -> Unit
) {
    val timer = Timer()
    timer.start()
    periodic(period, watchOverrunName) {
        if (timer.get() > timeout) {
            stop()
        } else {
            body()
        }
    }
}

fun Coroutine.parallel(
    vararg commands: Command
) {
    awaitAll(*commands)
}

fun Coroutine.parallel(
    vararg blocks: Coroutine.() -> Unit
) {
    parallel(*blocks.mapIndexed { index, coroutine -> use("unnamedParallel$index") { coroutine() }}.toTypedArray())
}

//fun test2Command() = use("test2Command") { println("in test2 command") }
//
//fun test3Command() = Command.parallel()
//
//val testCommand4: Command = use {
//
//}.whenCanceled {
//
//}.named("testCommand4")
//
//val testCommand = use("testCommand") {
//    periodic {
//        println("in periodic")
//
//        wait(1.0.seconds)
//
//        println("bye")
//        stop()
//    }
//
//    parallel(use ("parallel1"){
//        periodic {
//            println("in periodic2")
//        }
//    }, use ("parallel2") {
//        periodic {
//            println("in periodic3")
//        }
//    })
//
//    parallel(
//        {
//            periodic {
//                println("in parallel")
//            }
//        }, {
//            periodic {
//                println("in parallel2")
//            }
//        }
//    )
//
//
//    wait(1.0.seconds)
//
////    val test2Command = useWithName("test2Command") { println("in test2 command") }
//    val test3Command = use("test3Command") { println("in test3 command") }
//
//    test2Command() // bad. doesn't work
//
//    await(test2Command()) // do this
//
//    awaitAll(test2Command(), test3Command)
//
//    waitUntil { false }
//
//    println("hi")
//}