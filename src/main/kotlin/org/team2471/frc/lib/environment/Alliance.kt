package org.team2471.frc.lib.environment

import org.wpilib.driverstation.Alliance
import org.wpilib.driverstation.MatchState
import kotlin.jvm.optionals.getOrNull

/**
 * Gets if we are on the red alliance. If alliance is undefined, defaults to the previous value, if that is undefined, default true
 *
 * @see MatchState.getAlliance
 * */
val isRedAlliance: Boolean
    // Get the current alliance. If null, use the previous value.
    get() = (MatchState.getAlliance().getOrNull() ?: prevAlliance).also { prevAlliance = it } == Alliance.RED

private var prevAlliance: Alliance = Alliance.RED // Default to RED if undefined

/** ![isRedAlliance] */
val isBlueAlliance: Boolean get() = !isRedAlliance
