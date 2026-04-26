package org.team2471.frc.lib.commands

import org.team2471.frc.lib.util.isSim
import org.wpilib.commands3.Coroutine
import org.wpilib.commands3.Mechanism
import org.wpilib.commands3.Scheduler

open class MechanismBase(name: String): Mechanism(name) {
    init {
        Scheduler.getDefault().addPeriodic(::periodic)
        if (isSim) Scheduler.getDefault().addPeriodic(::simulationPeriodic)
        defaultCommand = use("$name default", this) { default() }
    }

    open fun Coroutine.default() {}

    open fun periodic() {}

    open fun simulationPeriodic() {}
}