package org.team2471.frc.lib.environment

import org.team2471.frc.lib.logging.getTunable
import org.wpilib.hardware.hal.HALUtil
import org.wpilib.system.RuntimeType
import org.wpilib.tunable.TunableConfig
import org.wpilib.tunable.Tunables

val doReplay: Boolean = false
val robotType: RobotType = when (RuntimeType.getValue(HALUtil.getHALRuntimeType())) {
    RuntimeType.SYSTEMCORE -> RobotType.REAL
    RuntimeType.SIMULATION -> if (doReplay) RobotType.REPLAY else RobotType.SIM
    else -> RobotType.REAL
}.also { println("robotMode = $it") }

val isReal = robotType == RobotType.REAL
val isSim = !isReal
val isReplay = robotType == RobotType.REPLAY

enum class RobotType {
    REAL,
    SIM,
    REPLAY
}


/** Demo Mode */

private val demoSpeedTunable = Tunables.getTable().getTunable("DemoSpeed", 1.0, true, TunableConfig().withProperty("min", "0.0").withProperty("max", "1.0"))
val demoSpeed: Double
    get() = demoSpeedTunable.get().coerceIn(0.0, 1.0)
val demoMode: Boolean
    get() = demoSpeed < 1.0