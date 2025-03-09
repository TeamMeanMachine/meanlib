// Copyright 2021-2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// version 3 as published by the Free Software Foundation or
// available in the root directory of this project.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
package org.team2471.frc.lib.framework.internal.akitLoggers

import edu.wpi.first.hal.DriverStationJNI
import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.hal.NotifierJNI
import edu.wpi.first.wpilibj.IterativeRobotBase
import edu.wpi.first.wpilibj.RobotController
import org.littletonrobotics.junction.AutoLogOutputManager
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory

/**
 * LoggedRobot implements the IterativeRobotBase robot program framework.
 *
 *
 *
 * The LoggedRobot class is intended to be subclassed by a user creating a robot
 * program, and will call all required AdvantageKit periodic methods.
 *
 *
 *
 * periodic() functions from the base class are called on an interval by a
 * Notifier instance.
 */
open class MeanLoggedRobot protected constructor(period: Double = defaultPeriodSecs) : IterativeRobotBase(period) {
    private val notifier = NotifierJNI.initializeNotifier()
    private val periodUs = (period * 1000000).toLong()
    private var nextCycleUs: Long = 0
    private val gcStatsCollector = GcStatsCollector()

    private var useTiming = true

    /**
     * Constructor for LoggedRobot.
     *
     * @param period Period in seconds.
     */
    /** Constructor for LoggedRobot.  */
    init {
        NotifierJNI.setNotifierName(notifier, "LoggedRobot")

        HAL.report(FRCNetComm.tResourceType.kResourceType_Framework, FRCNetComm.tInstances.kFramework_AdvantageKit)
    }

    protected open fun finalize() {
        NotifierJNI.stopNotifier(notifier)
        NotifierJNI.cleanNotifier(notifier)
    }

    /** Provide an alternate "main loop" via startCompetition().  */
    override fun startCompetition() {
        // Robot init methods
        robotInit()
        if (isSimulation()) {
            simulationInit()
        }
        val initEnd = RobotController.getFPGATime() // Includes Robot constructor and robotInit

        // Register auto logged outputs
        AutoLogOutputManager.addObject(this)

        // Save data from init cycle
        MeanLogger.periodicAfterUser(initEnd, 0)

        // Tell the DS that the robot is ready to be enabled
        println("********** Robot program startup complete **********")
        DriverStationJNI.observeUserProgramStarting()

        // Loop forever, calling the appropriate mode-dependent function
        while (true) {
            if (useTiming) {
                val currentTimeUs = RobotController.getFPGATime()
                if (nextCycleUs < currentTimeUs) {
                    // Loop overrun, start next cycle immediately
                    nextCycleUs = currentTimeUs
                } else {
                    // Wait before next cycle
                    NotifierJNI.updateNotifierAlarm(notifier, nextCycleUs)
                    if (NotifierJNI.waitForNotifierAlarm(notifier) == 0L) {
                        // Break the loop if the notifier was stopped
                        MeanLogger.end()
                        break
                    }
                }
                nextCycleUs += periodUs
            }

            val periodicBeforeStart = RobotController.getFPGATime()
            MeanLogger.periodicBeforeUser()
            val userCodeStart = RobotController.getFPGATime()
            loopFunc()
            val userCodeEnd = RobotController.getFPGATime()

            gcStatsCollector.update()
            MeanLogger.periodicAfterUser(userCodeEnd - userCodeStart, userCodeStart - periodicBeforeStart)
        }
    }

    /** Ends the main loop in startCompetition().  */
    override fun endCompetition() {
        NotifierJNI.stopNotifier(notifier)
    }

    /** Sets whether to use standard timing or run as fast as possible.  */
    fun setUseTiming(useTiming: Boolean) {
        this.useTiming = useTiming
    }

    private class GcStatsCollector {
        private val gcBeans: List<GarbageCollectorMXBean> = ManagementFactory.getGarbageCollectorMXBeans()
        private val lastTimes = LongArray(gcBeans.size)
        private val lastCounts = LongArray(gcBeans.size)

        fun update() {
            var accumTime: Long = 0
            var accumCounts: Long = 0
            for (i in gcBeans.indices) {
                val gcTime = gcBeans[i].collectionTime
                val gcCount = gcBeans[i].collectionCount
                accumTime += gcTime - lastTimes[i]
                accumCounts += gcCount - lastCounts[i]

                lastTimes[i] = gcTime
                lastCounts[i] = gcCount
            }

            MeanLogger.recordOutput("LoggedRobot/GCTimeMS", accumTime.toDouble())
            MeanLogger.recordOutput("LoggedRobot/GCCounts", accumCounts.toDouble())
        }
    }

    companion object {
        const val defaultPeriodSecs: Double = 0.02
    }
}