package org.team2471.frc.lib.autonomous.test

/**
 * Pair of name and function that returns an [TestOpMode]
 *
 * Intermediate step between constructing an [TestOpMode] and registering it with [org.wpilib.framework.OpModeRobot.addOpMode]
 *
 * @param name The name of the test
 * @param opModeSupplier Function that returns an [TestOpMode]
 */
data class TestOpModeSupplier(val name: String, val opModeSupplier: () -> TestOpMode)
