package org.team2471.frc.lib.environment

import org.wpilib.hardware.hal.HALUtil
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.RuntimeType

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
private val demoSpeedTopic = NetworkTableInstance.getDefault().getDoubleTopic("DemoSpeed")
private val demoSpeedEntry = demoSpeedTopic.getEntry(1.0).apply {
    if (!exists()) {
        println("DemoSpeed does not exist, setting it to 1.0")
        set(1.0)
        demoSpeedTopic.isPersistent = true
    }
}
val demoSpeed: Double
    get() = demoSpeedEntry.get().coerceIn(0.0, 1.0)
val demoMode: Boolean
    get() = demoSpeed < 1.0