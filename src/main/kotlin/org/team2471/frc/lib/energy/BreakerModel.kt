// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.
package org.team2471.frc.lib.energy

import org.littletonrobotics.junction.Logger
import org.wpilib.math.interpolation.InterpolatingDoubleTreeMap
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

/**
 * Thermal model of the main breaker using Miner's-rule damage accumulation.
 *
 *
 * Niceness (0–1) scales down the effective trip threshold so the model starts throttling before
 * the actual trip point, providing headroom.
 */
class BreakerModel internal constructor(niceness: Double) {
    private val tripThreshold: Double

    var damageState = 0.0

    init {
        tripThreshold = 1.0 - niceness.coerceIn(0.0, 1.0)
    }

    fun calculateMaxCurrent(budgetPeriodSecs: Double): Double {
        val remaining = tripThreshold - damageState
        if (remaining <= 0.0) {
            return 0.0
        }

        val requiredTripTime = budgetPeriodSecs / remaining

        // Edge cases
        if (requiredTripTime >= SENTINEL_TRIP_TIME) {
            return Double.Companion.MAX_VALUE
        }
        if (requiredTripTime <= MIN_TRIP_TIME) {
            return I_NORM_PTS[I_NORM_PTS.size - 1] * I_RATED
        }

        return exp(logInverseTripTimeMap.get(ln(requiredTripTime))) * I_RATED
    }

    fun update(current: Double) {
        val dt = 0.02
        val normalizedI = current / I_RATED

        val cooling: Boolean
        if (normalizedI > 1.0) {
            damageState += dt / getTripTime(normalizedI)
            cooling = false
        } else {
            damageState *= exp(-dt / TAU_COOL)
            cooling = true
        }
        damageState = damageState.coerceIn(0.0, 1.0)

        Logger.recordOutput("BreakerModel/Cooling", cooling)
        Logger.recordOutput("BreakerModel/DamageState", damageState)
    }

    companion object {
        const val I_RATED: Double = 120.0
        private const val TAU_COOL = 60.0 // Cooldown time constant

        // Maximum hold times from breaker datasheet
        private val I_NORM_PTS = doubleArrayOf(1.35, 2.0, 2.25, 2.5, 3.0, 4.0, 5.0)
        private val TRIP_TIME_PTS = doubleArrayOf(30.0 * 60.0, 70.0, 38.0, 25.0, 15.0, 10.0, 7.0)
        private const val SENTINEL_TRIP_TIME = 1.0e6
        private val MIN_TRIP_TIME = TRIP_TIME_PTS[TRIP_TIME_PTS.size - 1]

        private val logTripTimeMap = InterpolatingDoubleTreeMap()
        private val logInverseTripTimeMap = InterpolatingDoubleTreeMap()

        init {
            logTripTimeMap.put(ln(1.0), ln(SENTINEL_TRIP_TIME))
            logInverseTripTimeMap.put(ln(SENTINEL_TRIP_TIME), ln(1.0))
            for (i in I_NORM_PTS.indices) {
                logTripTimeMap.put(ln(I_NORM_PTS[i]), ln(TRIP_TIME_PTS[i]))
                logInverseTripTimeMap.put(ln(TRIP_TIME_PTS[i]), ln(I_NORM_PTS[i]))
            }
        }

        /** Returns the interpolated trip time for the given normalized current.  */
        fun getTripTime(normalizedI: Double): Double {
            return exp(logTripTimeMap.get(ln(max(normalizedI, 1.0))))
        }
    }
}