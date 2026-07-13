package org.team2471.frc.lib.util

import org.wpilib.driverstation.Alliance
import org.wpilib.driverstation.MatchState
import org.wpilib.hardware.hal.HALUtil
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.RuntimeType
import kotlin.jvm.optionals.getOrNull


/** Stores basic robot information like Alliance color and isReal/Sim/Replay */

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


//Alliance bool
val isRedAlliance: Boolean
    // Get the current alliance. If null, use the previous value. The previous value is initially set to RED as the fallback.
    get() = (MatchState.getAlliance().getOrNull() ?: prevAlliance).also { prevAlliance = it } == Alliance.RED

val isBlueAlliance: Boolean get() = !isRedAlliance
private var prevAlliance: Alliance = Alliance.RED

//Demo mode
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