package org.team2471.frc.lib.autonomous.auto

/**
 * Pair of name and function that returns an [AutoOpMode]
 *
 * Intermediate step between constructing an [AutoOpMode] and registering it with [org.wpilib.framework.OpModeRobot.addOpMode]
 *
 * @param name The name of the auto
 * @param opModeSupplier Function that returns an [AutoOpMode]
 */
data class AutoOpModeSupplier(val name: String, val opModeSupplier: () -> AutoOpMode)
