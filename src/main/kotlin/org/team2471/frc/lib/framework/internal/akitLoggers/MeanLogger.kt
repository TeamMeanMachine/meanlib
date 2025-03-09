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

import edu.wpi.first.units.Measure
import edu.wpi.first.units.Unit
import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.struct.Struct
import edu.wpi.first.util.struct.StructSerializable
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.RobotController
import org.littletonrobotics.conduit.ConduitApi
import org.littletonrobotics.junction.*
import org.littletonrobotics.junction.console.ConsoleSource
import org.littletonrobotics.junction.console.RIOConsoleSource
import org.littletonrobotics.junction.console.SimConsoleSource
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.littletonrobotics.junction.inputs.LoggedDriverStation
import org.littletonrobotics.junction.inputs.LoggedPowerDistribution
import org.littletonrobotics.junction.inputs.LoggedSystemStats
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d
import org.littletonrobotics.junction.networktables.LoggedNetworkInput
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asMeters
import org.team2471.frc.lib.math.toPose2d
import org.team2471.frc.lib.math.toTranslation2d
import org.team2471.frc.lib.units.Angle
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.function.*

/** Central class for recording and replaying log data.
 * [MeanLogger] is thread safe, unlike [Logger]
 * */
object MeanLogger {
    private const val receiverQueueCapcity = 500 // 10s at 50Hz

    val lock = Any()

    private var running = false
    private var cycleCount: Long = 0
    private val entry = LogTable(0)
    private var outputTable: LogTable? = null
    private val metadata: MutableMap<String, String> = HashMap()
    private var console: ConsoleSource? = null
    private val dashboardInputs: MutableList<LoggedNetworkInput> = ArrayList()
    private var urclSupplier: Supplier<Array<ByteBuffer>>? = null
    private var enableConsole = true

    private var replaySource: LogReplaySource? = null
    private val receiverQueue: BlockingQueue<LogTable> = ArrayBlockingQueue(receiverQueueCapcity)
    private val receiverThread = KReceiverThread(receiverQueue)

    /**
     * Returns the state of the receiver queue fault. This is tripped when the
     * receiver queue fills up, meaning that data is no longer being saved.
     */
    var receiverQueueFault: Boolean = false
        private set

    /**
     * Sets the source to use for replaying data. Use null to disable replay. This
     * method only works during setup before starting to log.
     */
    fun setReplaySource(replaySource: LogReplaySource?) {
        if (!running) {
            this.replaySource = replaySource
        }
    }

    /**
     * Adds a new data receiver to process real or replayed data. This method only
     * works during setup before starting to log.
     */
    fun addDataReceiver(dataReceiver: LogDataReceiver?) {
        if (!running && dataReceiver != null) {
            receiverThread.addDataReceiver(dataReceiver)
        }
    }

    /**
     * Registers a new dashboard input to be included in the periodic loop. This
     * function should not be called by the user.
     */
    fun registerDashboardInput(dashboardInput: LoggedNetworkInput) {
        dashboardInputs.add(dashboardInput)
    }

    /**
     * Registers a log supplier for [URCL](https://github.com/Mechanical-Advantage/URCL) (Unofficial
     * REV-Compatible Logger). This method should be called during setup before
     * starting to log. Example
     * usage shown below.
     *
     * <pre>
     * `Logger.registerURCL(URCL.startExternal());`
    </pre> *
     */
    fun registerURCL(logSupplier: Supplier<Array<ByteBuffer>>?) {
        urclSupplier = logSupplier
    }

    /**
     * Records a metadata value. This method only works during setup before starting
     * to log, then data will be recorded during the first cycle.
     *
     * @param key   The name used to identify this metadata field.
     * @param value The value of the metadata field.
     */
    fun recordMetadata(key: String, value: String) {
        if (!running) {
            metadata[key] = value
        }
    }

    /**
     * Disables automatic console capture.
     */
    fun disableConsoleCapture() {
        enableConsole = false
    }

    /**
     * Returns whether a replay source is currently being used.
     */
    fun hasReplaySource(): Boolean {
        return replaySource != null
    }

    /**
     * Starts running the logging system, including any data receivers or the replay
     * source.
     */
    fun start() {
        if (!running) {
            running = true

            // Exit if LoggedRobot not present
            val stackTrace = Thread.currentThread().stackTrace
            var isValid = false
            for (element in stackTrace) {
                try {
                    val elementClass = Class.forName(element.className)
                    if (MeanLoggedRobot::class.java.isAssignableFrom(elementClass)) {
                        isValid = true
                        break
                    }
                } catch (e: ClassNotFoundException) {
                }
            }
            if (!isValid) {
                DriverStation.reportError(
                    "The main robot class must inherit from MeanLoggedRobot when using AdvantageKit. For more details, check the AdvantageKit installation documentation: https://docs.advantagekit.org/installation\n\n*** EXITING DUE TO INVALID ADVANTAGEKIT INSTALLATION, SEE ABOVE. ***",
                    false
                )
                System.exit(1)
            }

            // Start console capture
            if (enableConsole) {
                if (RobotBase.isReal()) {
                    console = RIOConsoleSource()
                } else {
                    console = SimConsoleSource()
                }
            }

            // Start replay source
            if (replaySource != null) {
                replaySource!!.start()
            }

            // Create output table
            if (replaySource == null) {
                outputTable = entry.getSubtable("RealOutputs")
            } else {
                outputTable = entry.getSubtable("ReplayOutputs")
            }

            // Record metadata
            val metadataTable = entry.getSubtable(if (replaySource == null) "RealMetadata" else "ReplayMetadata")
            for ((key, value) in metadata) {
                metadataTable.put(key, value)
            }

            // Start receiver thread
            receiverThread.start()

            // Update RobotController to AdvantageKit timestamp
            RobotController.setTimeSource { timestamp }

            // Start first periodic cycle
            periodicBeforeUser()
        }
    }

    /**
     * Ends the logging system, including any data receivers or the replay source.
     */
    fun end() {
        if (running) {
            running = false
            if (console != null) {
                try {
                    console!!.close()
                } catch (e: Exception) {
                    DriverStation.reportError("[AdvantageKit] Failed to stop console capture.", true)
                }
            }
            if (replaySource != null) {
                replaySource!!.end()
            }
            receiverThread.interrupt()
            try {
                receiverThread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            RobotController.setTimeSource { RobotController.getFPGATime() }
        }
    }

    /**
     * Periodic method to be called during the constructor of Robot and each loop
     * cycle. Updates timestamp, replay entry, and dashboard inputs.
     */
    fun periodicBeforeUser() {
        cycleCount++
        if (running) {
            // Get next entry
            val entryUpdateStart = RobotController.getFPGATime()
            if (replaySource == null) {
                synchronized(entry) {
                    entry.timestamp = RobotController.getFPGATime()
                }
            } else {
                if (!replaySource!!.updateTable(entry)) {
                    end()
                    System.exit(0)
                }
            }

            // Update Driver Station
            val dsStart = RobotController.getFPGATime()
            if (hasReplaySource()) {
                LoggedDriverStation.replayFromLog(entry.getSubtable("DriverStation"))
            }

            // Update dashboard inputs
            val dashboardInputsStart = RobotController.getFPGATime()
            for (i in dashboardInputs.indices) {
                dashboardInputs[i].periodic()
            }
            val dashboardInputsEnd = RobotController.getFPGATime()

            // Record timing data
            recordOutput("Logger/EntryUpdateMS", (dsStart - entryUpdateStart) / 1000.0)
            if (hasReplaySource()) {
                recordOutput("Logger/DriverStationMS", (dashboardInputsStart - dsStart) / 1000.0)
            }
            recordOutput("Logger/DashboardInputsMS", (dashboardInputsEnd - dashboardInputsStart) / 1000.0)
        }
    }

    /**
     * Periodic method to be called after the constructor of Robot and each loop
     * cycle. Updates default log values and sends data to data receivers. Running
     * this after user code allows IO operations to occur between cycles rather than
     * interferring with the main thread.
     */
    fun periodicAfterUser(userCodeLength: Long, periodicBeforeLength: Long) {
        if (running) {
            // Capture conduit data
            val conduit = ConduitApi.getInstance()
            val conduitCaptureStart = RobotController.getFPGATime()
            conduit.captureData()

            // Update Driver Station
            val dsStart = RobotController.getFPGATime()
            if (!hasReplaySource()) {
                LoggedDriverStation.saveToLog(entry.getSubtable("DriverStation"))
            }

            // Save other conduit inputs
            val conduitSaveStart = RobotController.getFPGATime()
            if (!hasReplaySource()) {
                LoggedSystemStats.saveToLog(entry.getSubtable("SystemStats"))
                val loggedPowerDistribution = LoggedPowerDistribution.getInstance()
                loggedPowerDistribution?.saveToLog(entry.getSubtable("PowerDistribution"))
                if (urclSupplier != null && RobotBase.isReal()) {
                    val buffers = urclSupplier!!.get()
                    if (buffers.size == 3) {
                        for (i in 0..2) {
                            buffers[i].rewind()
                            val bytes = ByteArray(buffers[i].remaining())
                            buffers[i][bytes]
                            when (i) {
                                0 -> entry.put("URCL/Raw/Persistent", LogTable.LogValue(bytes, "URCLr3_persistent"))
                                1 -> entry.put("URCL/Raw/Periodic", LogTable.LogValue(bytes, "URCLr3_periodic"))
                                2 -> entry.put("URCL/Raw/Aliases", LogTable.LogValue(bytes, "URCLr3_aliases"))
                            }
                        }
                    }
                }
            }

            // Update automatic outputs from user code
            val autoLogStart = RobotController.getFPGATime()
            KAutoLogOutputManager.periodic()
            val alertLogStart = RobotController.getFPGATime()
            KAlertLogger.periodic()
            val radioLogStart = RobotController.getFPGATime()
            if (!hasReplaySource()) {
                RadioLogger.periodic(entry.getSubtable("RadioStatus"))
            }
            val consoleCaptureStart = RobotController.getFPGATime()
            if (enableConsole) {
                val consoleData = console!!.newData
                if (!consoleData.isEmpty()) {
                    recordOutput("Console", consoleData.trim { it <= ' ' })
                }
            }
            val consoleCaptureEnd = RobotController.getFPGATime()

            // Record timing data
            recordOutput("Logger/ConduitCaptureMS", (dsStart - conduitCaptureStart) / 1000.0)
            if (!hasReplaySource()) {
                recordOutput("Logger/DriverStationMS", (conduitSaveStart - dsStart) / 1000.0)
            }
            recordOutput("Logger/ConduitSaveMS", (autoLogStart - conduitSaveStart) / 1000.0)
            recordOutput("Logger/AutoLogMS", (alertLogStart - autoLogStart) / 1000.0)
            recordOutput("Logger/AlertLogMS", (radioLogStart - alertLogStart) / 1000.0)
            recordOutput("Logger/RadioLogMS", (consoleCaptureStart - radioLogStart) / 1000.0)
            recordOutput("Logger/ConsoleMS", (consoleCaptureEnd - consoleCaptureStart) / 1000.0)
            recordOutput("LoggedRobot/UserCodeMS", userCodeLength / 1000.0)
            val periodicAfterLength = consoleCaptureEnd - conduitCaptureStart
            recordOutput("LoggedRobot/LogPeriodicMS", (periodicBeforeLength + periodicAfterLength) / 1000.0)
            recordOutput(
                "LoggedRobot/FullCycleMS",
                (periodicBeforeLength + userCodeLength + periodicAfterLength) / 1000.0
            )
            recordOutput("Logger/QueuedCycles", receiverQueue.size)

            try {
                // Send a copy of the data to the receivers. The original object will be
                // kept and updated with the next timestamp (and new data if replaying).
                // synchronized is used to prevent concurrent modification.
                synchronized(lock) {
                    receiverQueue.add(LogTable.clone(entry))
                }
                receiverQueueFault = false
            } catch (exception: IllegalStateException) {
                receiverQueueFault = true
                DriverStation.reportError(
                    "[AdvantageKit] Capacity of receiver queue exceeded, data will NOT be logged.",
                    false
                )
            }
        }
    }

    val timestamp: Long
        /**
         * Returns the current FPGA timestamp or replayed time based on the current log
         * entry (microseconds).
         */
        get() {
            synchronized(entry) {
                return if (!running || entry == null) {
                    RobotController.getFPGATime()
                } else {
                    entry.timestamp
                }
            }
        }

    @get:Deprecated("Use {@code RobotController.getFPGATime()} instead.")
    val realTimestamp: Long
        /**
         * Returns the true FPGA timestamp in microseconds, regardless of the timestamp
         * used for logging. Useful for analyzing performance. DO NOT USE this method
         * for any logic which might need to be replayed.
         *
         */
        get() = RobotController.getFPGATime()

    /**
     * Runs the provided callback function every N loop cycles. This method can be
     * used
     * to update inputs or log outputs at a lower rate than the standard loop cycle.
     *
     *
     *
     * **Note that this method must be called periodically to continue running the
     * callback function**.
     */
    fun runEveryN(n: Int, function: Runnable) {
        if (cycleCount % n == 0L) {
            function.run()
        }
    }

    /**
     * Processes a set of inputs, logging them on the real robot or updating them in
     * the simulator. This should be called every loop cycle after updating the
     * inputs from the hardware (if applicable).
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key    The name used to identify this set of inputs.
     * @param inputs The inputs to log or update.
     */
    fun processInputs(key: String?, inputs: LoggableInputs) {
        if (running) {
            if (replaySource == null) {
                inputs.toLog(entry.getSubtable(key))
            } else {
                inputs.fromLog(entry.getSubtable(key))
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: ByteArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<ByteArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Boolean) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: BooleanSupplier) {
        if (running) {
            synchronized(lock) {
                try {outputTable?.put(key, value.asBoolean)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: BooleanArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<BooleanArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Int) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: IntSupplier) {
        if (running) {
            synchronized(lock) {
                try {outputTable?.put(key, value.asInt)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: IntArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<IntArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Long) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: LongSupplier) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value.asLong)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: LongArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<LongArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Float) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: FloatArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<FloatArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Double) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: DoubleSupplier) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value.asDouble)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: DoubleArray?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<DoubleArray?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: String?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<String?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: Array<Array<String?>?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <E : Enum<E>?> recordOutput(key: String?, value: E) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <E : Enum<E>?> recordOutput(key: String?, value: Array<E>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <E : Enum<E>?> recordOutput(key: String?, value: Array<Array<E>?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <U : Unit?> recordOutput(key: String?, value: Measure<U>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     *
     *
     * This method serializes a single object as a struct. Example usage:
     * `recordOutput("MyPose", Pose2d.struct, new Pose2d())`
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <T> recordOutput(key: String?, struct: Struct<T>?, value: T) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, struct, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes an array of objects as a struct. Example usage:
     * `recordOutput("MyPoses", Pose2d.struct, new Pose2d(), new Pose2d());
     * recordOutput("MyPoses", Pose2d.struct, new Pose2d[] {new Pose2d(), new
     * Pose2d()});
    ` *
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <T> recordOutput(key: String?, struct: Struct<T>?, vararg value: T) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, struct, *value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun <T> recordOutput(key: String?, struct: Struct<T>?, value: Array<Array<T>?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, struct, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes a single object as a protobuf. Protobuf should only be
     * used for objects that do not support struct serialization. Example usage:
     * `recordOutput("MyPose", Pose2d.proto, new Pose2d())`
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
//    fun <T, MessageType : ProtoMessage<*>?> recordOutput(
//        key: String?, proto: Protobuf<T, MessageType>?,
//        value: T
//    ) {
//        if (running) {
//            outputTable?.put(key, proto, value)
//        }
//    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes a single object as a struct or protobuf automatically.
     * Struct is preferred if both methods are supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <T>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </T> */
    fun <T : WPISerializable?> recordOutput(key: String?, value: T) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes an array of objects as a struct automatically.
     * Top-level protobuf arrays are not supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <T>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </T> */
    fun <T : StructSerializable?> recordOutput(key: String?, vararg value: T) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, *value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes an array of objects as a struct automatically.
     * Top-level protobuf arrays are not supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <T>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </T> */
    fun <T : StructSerializable?> recordOutput(key: String?, value: Array<Array<T>?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes a single object as a struct or protobuf automatically.
     * Struct is preferred if both methods are supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <R>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </R> */
    fun <R : Record?> recordOutput(key: String?, value: R) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes an array of objects as a struct automatically.
     * Top-level protobuf arrays are not supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <R>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </R> */
    fun <R : Record?> recordOutput(key: String?, vararg value: R) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, *value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * This method serializes an array of objects as a struct automatically.
     * Top-level protobuf arrays are not supported.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param <R>   The type
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
    </R> */
    fun <R : Record?> recordOutput(key: String?, value: Array<Array<R>?>?) {
        if (running) {
            synchronized(lock) {
                try {outputTable!!.put(key, value)} catch (_:Exception) {}
            }
        }
    }

    /**
     * Records a single output field for easy access when viewing the log. On the
     * simulator, use this method to record extra data based on the original inputs.
     *
     *
     *
     * The current position of the Mechanism2d is logged once as a set of nested
     * fields. If the position is updated, this method must be called again.
     *
     *
     *
     * This method is **not thread-safe** and should only be called from the
     * main thread. See the "Common Issues" page in the documentation for more
     * details.
     *
     * @param key   The name of the field to record. It will be stored under
     * "/RealOutputs" or "/ReplayOutputs"
     * @param value The value of the field.
     */
    fun recordOutput(key: String?, value: LoggedMechanism2d) {
        if (running) {
            try {value.logOutput(outputTable!!.getSubtable(key))} catch (_:Exception) {}
        }
    }

    fun recordOutput(key: String?, value: Vector2L) {
        synchronized(lock) {
            try {outputTable!!.put(key, value.asMeters.toTranslation2d())} catch (_:Exception) {}
        }
    }


    fun recordOutput(key: String?, value: Vector2L, angle: Angle) {
        synchronized(lock) {
            try {outputTable!!.put(key, value.asMeters.toPose2d(angle))} catch (_:Exception) {}
        }
    }
}