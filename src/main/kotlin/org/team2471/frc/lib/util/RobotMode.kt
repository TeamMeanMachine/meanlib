package org.team2471.frc.lib.util

import org.wpilib.driverstation.Alliance
import org.wpilib.driverstation.MatchState
import org.wpilib.driverstation.RobotState
import org.wpilib.hardware.hal.HALUtil
import org.wpilib.networktables.NetworkTableInstance
import org.wpilib.system.RuntimeType


/** Stores basic robot information like Alliance color and isReal/Sim/Replay */

val doReplay: Boolean = false
val robotMode: RobotMode = when (RuntimeType.getValue(HALUtil.getHALRuntimeType())) {
    RuntimeType.SYSTEMCORE -> RobotMode.REAL
    RuntimeType.SIMULATION -> if (doReplay) RobotMode.REPLAY else RobotMode.SIM
    else -> RobotMode.REAL
}.also { println("robotMode = $it") }

val isReal = robotMode == RobotMode.REAL
val isSim = !isReal
val isReplay = robotMode == RobotMode.REPLAY

enum class RobotMode {
    REAL,
    SIM,
    REPLAY
}


//Alliance bool
val isRedAlliance: Boolean
    get() = if (MatchState.getAlliance().isEmpty) {
        prevIsRedAlliance ?: true // If no alliance, return the last known alliance or default to red
    } else {
        (MatchState.getAlliance().get() == Alliance.RED).also { prevIsRedAlliance = it }
    }
val isBlueAlliance: Boolean get() = !isRedAlliance
private var prevIsRedAlliance: Boolean? = null

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