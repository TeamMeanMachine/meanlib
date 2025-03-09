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

import edu.wpi.first.wpilibj.Notifier
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.RobotController
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.LogTable.LogValue
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.*

internal object RadioLogger {
    private const val requestPeriodSecs = 5.0
    private const val connectTimeout = 500
    private const val readTimeout = 500

//    private var statusURL: URL? = null
    private var notifier: Notifier? = null
    private val lock = Any()
    private var isConnected = false
    private var statusJson = ""

    fun periodic(table: LogTable) {
        if (notifier == null && RobotBase.isReal()) {
            start()
        }

        synchronized(lock) {
            table.put("Connected", isConnected)
            table.put("Status", LogValue(statusJson, "json"))
        }
    }

    private fun start() {
        // Get status URL
        val teamNumber = RobotController.getTeamNumber()
        val statusURLBuilder = StringBuilder()
        statusURLBuilder.append("http://10.")
        statusURLBuilder.append(teamNumber / 100)
        statusURLBuilder.append(".")
        statusURLBuilder.append(teamNumber % 100)
        statusURLBuilder.append(".1/status")
        var statusURL: URL? = null
        try {
            statusURL = URI(statusURLBuilder.toString()).toURL()
        } catch (e: MalformedURLException) {
            return
        } catch (e: URISyntaxException) {
            return
        }

        // Launch notifier
        notifier = Notifier {
            // Request status from radio
            val response = StringBuilder()
            try {
                val connection =
                    statusURL.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = connectTimeout
                connection.readTimeout = readTimeout

                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        response.append(line)
                    }
                }
            } catch (e: Exception) {
            }

            // Update status
            val responseStr = response.toString().replace("\\s+".toRegex(), "")
            synchronized(lock) {
                isConnected = responseStr.length > 0
                statusJson = responseStr
            }
        }
        notifier!!.setName("AdvantageKit_RadioLogger")
        notifier!!.startPeriodic(requestPeriodSecs)
    }
}