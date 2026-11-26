package org.team2471.frc.lib.control

import org.team2471.frc.lib.commands.command
import org.team2471.frc.lib.units.seconds
import org.wpilib.command3.Command
import org.wpilib.command3.sysid.SysIdRoutine
import org.wpilib.units.measure.Time

fun SysIdRoutine.fullRoutineCommand(name: String, timeBetweenRoutines: Time = 1.0.seconds): Command {
    val routine = this
    return command(name) {
        println("Running SysID routine $name")
        println("Running forward quasistatic")
        await(routine.quasistatic(SysIdRoutine.Direction.FORWARD))
        wait(timeBetweenRoutines)
        println("Running reverse quasistatic")
        await(routine.quasistatic(SysIdRoutine.Direction.REVERSE))
        wait(timeBetweenRoutines)
        println("Running forward dynamic")
        await(routine.dynamic(SysIdRoutine.Direction.FORWARD))
        wait(timeBetweenRoutines)
        println("Running reverse dynamic")
        await(routine.dynamic(SysIdRoutine.Direction.REVERSE))
        println("Finished SysID routine $name")
    }
}