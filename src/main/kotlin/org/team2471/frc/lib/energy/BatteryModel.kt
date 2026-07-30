// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.
package org.team2471.frc.lib.energy

import org.littletonrobotics.junction.Logger
import org.wpilib.math.util.MathUtil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow

/**
 * Battery state estimator using a single RC Thevenin model with Peukert correction. SOC is
 * determined from open-loop coulomb counting, which is then used to determine OCV, R0, and RP. VP
 * (polarization voltage) is determined using RC model and corrected from battery terminal voltage
 * measurements using Kalman gain. Model structure based on this paper
 * (https://doi.org/10.1016/j.ifacol.2022.06.031).
 *
 *
 * Battery parameters are for MK Powered battery based on this post
 * (https://www.chiefdelphi.com/t/detailed-frc-battery-comparison-for-2026/508077/22) fitted using
 * data from logs.
 */
class BatteryModel {
    // State
    private var soc = 0.0
    private var vP = 0.0
    private var pVP = 0.0

    init {
        setInitialVoltage(12.5, 0.0)
    }

    fun setInitialVoltage(initialVoltage: Double, initialCurrent: Double) {
        val estimatedOcv = initialVoltage + initialCurrent * (R0_BASE + RP_BASE)
        soc = calculateSoc(estimatedOcv).coerceIn(0.0, 1.0)
        vP = initialCurrent * getRp(soc)
        pVP = Q_VP
    }

    fun calculateMaxCurrent(minVoltageThreshold: Double): Double {
        return max(0.0, (calculateOcv(soc) - vP - minVoltageThreshold) / getR0(soc))
    }

    /**
     * Update battery state estimate
     *
     * @param current Total current draw (Amperes).
     * @param measuredVoltage Terminal voltage from the PDP/PDH.
     */
    fun update(current: Double, measuredVoltage: Double) {
        val dt = 0.02

        // Coloumb counting with peukert correction
        var effectiveAmps = current
        if (current > I_NOMINAL) {
            val peukert = PEUKERT_BASE + PEUKERT_SOC_SCALE * (1.0 - soc)
            effectiveAmps = current * (current / I_NOMINAL).pow(peukert - 1.0)
        } else effectiveAmps = current
        soc = (soc - effectiveAmps * dt / CAPACITY_AS).coerceIn(0.0, 1.0)

        // Update polarization voltage
        val rp = getRp(soc)
        // Calculate vP(dt) using RC dynamics: dV/dt = (I * R - V) / (R * C)
        val tau = rp * CP
        val vP_inf = current * rp
        val decay = exp(-dt / tau)
        vP = vP_inf - (vP_inf - vP) * decay
        // Calculate pVP-
        pVP = decay.pow(2.0) * pVP + Q_VP * dt

        // Correct vP from voltage measurement
        val r0 = getR0(soc)
        val ocv = calculateOcv(soc)
        val predicted = ocv - current * r0 - vP
        val K = pVP / (pVP + (R_VBATT + R0_UNCERTAINTY * current * current))
        vP += K * -(measuredVoltage - predicted)
        pVP *= (1.0 - K)

        // Log
        val corrected = calculateOcv(soc) - (current * getR0(soc)) - vP
        Logger.recordOutput("BatteryModel/EstimatedVoltage", corrected, "volts")
        Logger.recordOutput("BatteryModel/OCV", calculateOcv(soc), "volts")
        Logger.recordOutput("BatteryModel/SOC", soc)
        Logger.recordOutput("BatteryModel/VP", vP, "volts")
        Logger.recordOutput("BatteryModel/P_VP", pVP)
    }

    companion object {
        // Battery
        private const val CAPACITY_AH = 23.10
        private val CAPACITY_AS = CAPACITY_AH * 3600.0

        // OCV(soc) = OCV_VOLTS[i] + t * (OCV_VOLTS[i + 1] - OCV_VOLTS[i])
        private val OCV_SOC_KNOTS = doubleArrayOf(0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
        private val OCV_VOLTS = doubleArrayOf(12.112, 12.113, 12.311, 12.401, 12.535, 13.196)

        // R0(soc) = R0_BASE + R0_KNEE_GAIN * exp(-R0_KNEE_RATE * (R0_KNEE_SOC - soc))
        private const val R0_BASE = 0.01181
        private const val R0_KNEE_GAIN = 0.00335
        private const val R0_KNEE_RATE = 1.36726
        private const val R0_KNEE_SOC = 0.4

        // Single RC polarization
        private const val RP_BASE = 0.00274
        private const val RP_SOC_SCALE = 2.10529
        private const val CP = 400.0

        // Peukert (SOC-scaled)
        private const val PEUKERT_BASE = 1.07
        private const val PEUKERT_SOC_SCALE = 0.1
        private val I_NOMINAL = CAPACITY_AH / 20.0

        // Variance parameters
        private val Q_VP = 0.02.pow(2.0)
        private val R_VBATT = 0.1.pow(2.0)
        private val R0_UNCERTAINTY = 0.0015.pow(2.0)

        private fun getR0(soc: Double): Double {
            return R0_BASE + R0_KNEE_GAIN * exp(R0_KNEE_RATE * (R0_KNEE_SOC - soc))
        }

        private fun getRp(soc: Double): Double {
            val d = 1.0 - soc
            return RP_BASE * (1.0 + RP_SOC_SCALE * d * d)
        }

        private fun calculateOcv(soc: Double): Double {
            var soc = soc
            soc = soc.coerceIn(0.0, 1.0)
            for (i in 0..<OCV_SOC_KNOTS.size - 1) {
                if (soc <= OCV_SOC_KNOTS[i + 1]) {
                    val t = (soc - OCV_SOC_KNOTS[i]) / (OCV_SOC_KNOTS[i + 1] - OCV_SOC_KNOTS[i])
                    return OCV_VOLTS[i] + t * (OCV_VOLTS[i + 1] - OCV_VOLTS[i])
                }
            }
            return OCV_VOLTS[OCV_VOLTS.size - 1]
        }

        private fun calculateSoc(ocv: Double): Double {
            var ocv = ocv
            ocv = ocv.coerceIn(OCV_VOLTS[0], OCV_VOLTS[OCV_VOLTS.size - 1])
            for (i in 0..<OCV_VOLTS.size - 1) {
                if (ocv <= OCV_VOLTS[i + 1]) {
                    val t = (ocv - OCV_VOLTS[i]) / (OCV_VOLTS[i + 1] - OCV_VOLTS[i])
                    return OCV_SOC_KNOTS[i] + t * (OCV_SOC_KNOTS[i + 1] - OCV_SOC_KNOTS[i])
                }
            }
            return OCV_SOC_KNOTS[OCV_SOC_KNOTS.size - 1]
        }
    }
}