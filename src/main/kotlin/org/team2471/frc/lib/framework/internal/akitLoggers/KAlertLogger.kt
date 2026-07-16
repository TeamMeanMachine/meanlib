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

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.StringArraySubscriber
import java.util.HashMap

internal object KAlertLogger {
    private var groups: Map<String, Any>? = null
    private val errorSubscribers: MutableMap<String, StringArraySubscriber> = HashMap()
    private val warningSubscribers: MutableMap<String, StringArraySubscriber> = HashMap()
    private val infoSubscribers: MutableMap<String, StringArraySubscriber> = HashMap()

    init {
        try {
            val sendableAlertsClass = Class.forName("edu.wpi.first.wpilibj.Alert\$SendableAlerts")
            val groupsField = sendableAlertsClass.getDeclaredField("groups")
            groupsField.isAccessible = true
            groups = groupsField[null] as Map<String, Any>
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /** Log the current state of all alerts as outputs.  */
    fun periodic() {
        if (groups == null) return
        for (group in groups!!.keys) {
            SimpleLogger.recordOutput("$group/.type", "Alerts")

            // Create NetworkTables subscribers
            if (!errorSubscribers.containsKey(group)) {
                errorSubscribers[group] = NetworkTableInstance.getDefault()
                    .getStringArrayTopic("/SmartDashboard/$group/errors")
                    .subscribe(arrayOfNulls(0))
            }
            if (!warningSubscribers.containsKey(group)) {
                warningSubscribers[group] = NetworkTableInstance.getDefault()
                    .getStringArrayTopic("/SmartDashboard/$group/warnings")
                    .subscribe(arrayOfNulls(0))
            }
            if (!infoSubscribers.containsKey(group)) {
                infoSubscribers[group] = NetworkTableInstance.getDefault()
                    .getStringArrayTopic("/SmartDashboard/$group/infos")
                    .subscribe(arrayOfNulls(0))
            }

            // Get values
            SimpleLogger.recordOutput(
                "$group/errors", errorSubscribers[group]!!
                    .get()
            )
            SimpleLogger.recordOutput(
                "$group/warnings", warningSubscribers[group]!!
                    .get()
            )
            SimpleLogger.recordOutput(
                "$group/infos", infoSubscribers[group]!!
                    .get()
            )
        }
    }
}