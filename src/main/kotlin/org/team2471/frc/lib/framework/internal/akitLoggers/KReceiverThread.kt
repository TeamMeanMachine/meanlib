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

import org.littletonrobotics.junction.LogDataReceiver
import org.littletonrobotics.junction.LogTable
import java.util.concurrent.BlockingQueue

class KReceiverThread internal constructor(queue: BlockingQueue<LogTable>) : Thread("AdvantageKit_LogReceiver") {
    private val queue: BlockingQueue<LogTable>
    private val dataReceivers: MutableList<LogDataReceiver> = ArrayList()

    init {
        this.isDaemon = true
        this.queue = queue
    }

    fun addDataReceiver(dataReceiver: LogDataReceiver) {
        dataReceivers.add(dataReceiver)
    }

    override fun run() {
        // Start data receivers
        for (i in dataReceivers.indices) {
            dataReceivers[i].start()
        }

        try {
            while (true) {
                val entry = queue.take() // Wait for data

                // Send data to receivers
                for (i in dataReceivers.indices) {
                    dataReceivers[i].putTable(entry)
                }
            }
        } catch (exception: InterruptedException) {
            // End all data receivers

            var i = 0
            while (i < dataReceivers.size) {
                dataReceivers[i].end()
                i++
            }
        }
    }
}