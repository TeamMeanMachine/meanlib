package org.team2471.frc.lib.sensors.analogInput

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.sensors.canCoder.LoggedCANCoder
import java.util.*

object MasterAnalogInput {
    private val syncAnalogInputs = Collections.synchronizedList(arrayListOf<LoggedAnalogInput>())
    init {
        println("MASTER AnalogInput INIT")
        GlobalScope.launch {
            periodic(0.02) {
                try {
                    syncAnalogInputs.forEach {
                        it.periodicLoop()
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun addAnalogInput(analogInput: LoggedAnalogInput) {
        syncAnalogInputs.add(analogInput)
    }
}