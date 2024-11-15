package org.team2471.frc.lib.util

import edu.wpi.first.hal.HALUtil
import edu.wpi.first.wpilibj.RuntimeType

const val doReplay = false

val robotMode: RobotMode =
    when (RuntimeType.getValue(HALUtil.getHALRuntimeType())) {
        RuntimeType.kRoboRIO2, RuntimeType.kRoboRIO -> RobotMode.REAL
        RuntimeType.kSimulation -> if (doReplay) RobotMode.REPLAY else RobotMode.SIM
        else -> RobotMode.REAL
    }.also { println("robotMode = $it") }

val isReal = robotMode == RobotMode.REAL

enum class RobotMode {
    REAL,
    SIM,
    REPLAY
}