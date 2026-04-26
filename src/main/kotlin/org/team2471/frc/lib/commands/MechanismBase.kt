package org.team2471.frc.lib.commands

import org.team2471.frc.lib.util.isSim
import org.wpilib.commands3.Command
import org.wpilib.commands3.Coroutine
import org.wpilib.commands3.Mechanism
import org.wpilib.commands3.Scheduler

open class MechanismBase(name: String): Mechanism(name) {
    init {
        Scheduler.getDefault().addPeriodic(::periodic)
        if (isSim) Scheduler.getDefault().addPeriodic(::simulationPeriodic)
//        defaultCommand = use("$name[DEFAULT]", this) { default() }
//        defaultCommand = use(this) { default() }/*.withPriority(Command.DEFAULT_PRIORITY - 1)*/.named("$name[DEFAULT]")
    }

    open fun Coroutine.default() { park() }

    open fun periodic() {}

    open fun simulationPeriodic() {}
}