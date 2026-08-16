// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.
package org.team2471.frc.lib.energy

import org.littletonrobotics.junction.AutoLog
import org.littletonrobotics.junction.Logger
import org.wpilib.driverstation.RobotState
import org.wpilib.math.filter.Debouncer
import org.wpilib.system.RobotController
import org.wpilib.util.Alert
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object FindAmpsManager {
    // Solve for damage state where breaker will trip if we run at maxBudgetAmps for horizon.
    private val breakerDamageWarningThreshold: Double = ((1.0 - breakerNiceness)
            - (breakerDangerHorizonSecs / BreakerModel.getTripTime(maxBudgetAmps / BreakerModel.I_RATED)))

    // MARK: - Constants
    private const val minVoltageBrownout = 7.0
    private const val maxBudgetAmps = 200.0
    private const val breakerNiceness = 0.05
    private const val budgetWarningThreshold = 180.0

    // Allow ramping from other subsystems
    private const val budgetHeadroom = 0.9

    // Time we can run max budget before trip
    private const val breakerDangerHorizonSecs = 3.0
    private val budgetWarning = Alert("budgetWarning", "Battery is low, robot performance may be degraded.", Alert.Level.LOW)
    private val budgetWarningDebouncer = Debouncer(0.5, Debouncer.DebounceType.kBoth)
    private val brownoutWarning = Alert("brownoutWarning", "Brownout detected, drive performance may be degraded.", Alert.Level.MEDIUM)
    private val breakerDamageWarning = Alert("breakerDamageWarning", "Breaker damage is high, please stop using the robot.", Alert.Level.MEDIUM)
    private val breakerDamageWarningDebouncer = Debouncer(0.5, Debouncer.DebounceType.kBoth)


    // MARK: - Members
    private val battery: BatteryModel = BatteryModel()
    private val breaker: BreakerModel = BreakerModel(breakerNiceness)
    private val inputs: BatteryIOInputsAutoLogged = BatteryIOInputsAutoLogged()

    private var budget = 0.0
    private var driveBudget = 0.0
    private val brownoutDebouncer = Debouncer(2.0, Debouncer.DebounceType.kFalling)

    init {

    }

    fun reset() {
        battery.setInitialVoltage(inputs.batteryVoltage, BatteryLogger.totalCurrent)
    }

    fun reportCurrentUsage(key: String?, drive: Boolean, vararg amps: Double) {
        var totalAmps = 0.0
        for (amp in amps) totalAmps += max(0.0, amp)
//        BatteryLogger.reportCurrentUsage(key, drive, totalAmps)
    }

    fun periodic() {
        inputs.batteryVoltage = RobotController.getBatteryVoltage()
        inputs.rioCurrent = 0.0//RobotController.getInputCurrent()
        inputs.brownedOut = RobotController.isBrownedOut()
        Logger.processInputs("EnergyLogger", inputs)
    }

    fun periodicAfterScheduler() {
        // Run energy logger
//        BatteryLogger.periodicAfterScheduler()

        // Update models
        battery.update(BatteryLogger.totalCurrent, inputs.batteryVoltage)
        breaker.update(BatteryLogger.totalCurrent)

        // Calculate budgets
        val batteryMaxCurrent: Double = battery.calculateMaxCurrent(minVoltageBrownout)
        val breakerMaxCurrent: Double = breaker.calculateMaxCurrent(breakerDangerHorizonSecs)
        Logger.recordOutput("FinanceDepartment/BatteryMaxCurrent", batteryMaxCurrent)
        Logger.recordOutput("FinanceDepartment/BreakerMaxCurrent", breakerMaxCurrent)
        budget = min(min(batteryMaxCurrent, breakerMaxCurrent) * budgetHeadroom, maxBudgetAmps)

        val brownoutDebounced = brownoutDebouncer.calculate(inputs.brownedOut)
        if (!brownoutDebounced) {
            driveBudget = budget - BatteryLogger.totalCurrent + BatteryLogger.driveCurrent
        } else {
            val calculatedBudget: Double =
                budget - BatteryLogger.totalCurrent + BatteryLogger.driveCurrent
            // Asymmetric ramping of drive budget
            driveBudget =
                if (calculatedBudget < driveBudget) {
                    calculatedBudget
                } else {
                    calculatedBudget.coerceAtMost(driveBudget + 50.0 * 0.02)
                }
        }
        driveBudget = max(0.0, driveBudget)

        Logger.recordOutput("FinanceDepartment/Budget", budget)
        Logger.recordOutput("FinanceDepartment/DriveBudget", driveBudget)

        // Update alerts
        budgetWarning.set(budgetWarningDebouncer.calculate(budget < budgetWarningThreshold))
        brownoutWarning.set(brownoutDebounced)
        breakerDamageWarning.set(breakerDamageWarningDebouncer.calculate(breaker.damageState > breakerDamageWarningThreshold))

//        energyLogger.resetTotals()
    }

    val driveLimit: Double
        /** Get the current limits for a subsystem.  */
        get() {
            var driveLimit = floor((driveBudget / 4.0).coerceIn(5.0, 50.0) / 0.5) * 0.5
            if (RobotState.isAutonomous()) {
                driveLimit = 50.0
            }
            Logger.recordOutput("FinanceDepartment/DriveLimit", driveLimit)
            return driveLimit
        }

    val batteryVoltage: Double
        get() = inputs.batteryVoltage

    @AutoLog
    open class BatteryIOInputs {
        @JvmField
        var batteryVoltage: Double = 12.0
        @JvmField
        var rioCurrent: Double = 0.0
        @JvmField
        var macMiniCurrent: Double = 0.0
        @JvmField
        var brownedOut: Boolean = false
    }
}