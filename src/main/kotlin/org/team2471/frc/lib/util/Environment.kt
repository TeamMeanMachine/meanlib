package org.team2471.frc.lib.util

import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.hal.AllianceStationID
import edu.wpi.first.hal.HAL
import java.text.SimpleDateFormat
import java.util.*

internal val ds = DriverStation.getInstance()

object Environment {
    val station: AllianceStation
    val fmsAttached: Boolean
    val sessionID: String

    init {
        // load alliance data
        var allianceStationID: AllianceStationID?
        while (true) {
            allianceStationID = HAL.getAllianceStation()
            if (allianceStationID != null) break
            else Thread.sleep(100)
        }

        station = when (allianceStationID!!) {
            AllianceStationID.Red1 -> AllianceStation(Alliance.RED, 1)
            AllianceStationID.Red2 -> AllianceStation(Alliance.RED, 2)
            AllianceStationID.Red3 -> AllianceStation(Alliance.RED, 3)
            AllianceStationID.Blue1 -> AllianceStation(Alliance.BLUE, 1)
            AllianceStationID.Blue2 -> AllianceStation(Alliance.BLUE, 2)
            AllianceStationID.Blue3 -> AllianceStation(Alliance.BLUE, 3)
        }

        fmsAttached = ds.isFMSAttached

        val sdf = SimpleDateFormat("MM-dd-h:mm:ss-a")
        sdf.timeZone = TimeZone.getTimeZone("America/Los_Angeles") // PST
        sessionID = sdf.format(Date()) +
                "-${allianceStationID.name.toLowerCase()}" +
                if (fmsAttached) ".fms" else ""
    }
}

enum class Alliance {
    RED,
    BLUE
}

data class AllianceStation(val alliance: Alliance, val station: Int)
