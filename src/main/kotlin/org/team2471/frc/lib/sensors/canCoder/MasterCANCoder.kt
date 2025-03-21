package org.team2471.frc.lib.sensors.canCoder

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import java.util.*

object MasterCANCoder {
    private val syncCANCoder = Collections.synchronizedList(arrayListOf<LoggedCANCoder>())
    init {
        println("MASTER CANCoder INIT")
        GlobalScope.launch {
            periodic(0.02) {
                try {
                    syncCANCoder.forEach {
                        it.periodicLoop()
                    }
                } catch (_: Exception) { }
            }
        }
    }

    fun addCANCoder(cancoder: LoggedCANCoder) {
        syncCANCoder.add(cancoder)
    }
}