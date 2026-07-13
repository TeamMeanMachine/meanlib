package org.team2471.frc.lib.logging

import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.logging.SimpleLogger
import org.team2471.frc.lib.math.round
import org.wpilib.system.Timer

/**
 * Publishes loop times to NetworkTables.
 *
 * Period is the time between the named loop
 * SinceReset is the time between reset was called and when the loop got recorded.
 * LoopDuration is the duration between two loops with the same name if [record] is called twice with the same name in the same reset period
 */
object LoopLogger {
    private val prevTimes = mutableMapOf<String, Double>()
    private var resetTime = Double.MAX_VALUE
    private val loopsMap = mutableMapOf<String, Int>()
    private val duplicateLoopMaps = mutableMapOf<String, Int>()

    fun reset() {
        SimpleLogger.recordOutput("LoopLogger/LoopCount", loopsMap.size)
        resetTime = Timer.getMonotonicTimestamp()
    }

    /**
     * Log the period and the time of the named loop at the current moment.
     *
     * If the named loop has already been recorded in the same frame, the duration between the two loops is also logged.
     */
    fun record(loopName: String) {
        val loopIndex: Int = loopsMap.getOrPut(loopName) { loopsMap.size + duplicateLoopMaps.size }
        val now = Timer.getMonotonicTimestamp()
        val prevTime = prevTimes.put(loopName, now) ?: return //put returns previous value
        val sinceReset = now - resetTime
        val period = (now - prevTime)

        if (prevTime > resetTime - 1e-10) { // Check if the loop has already been triggered in this frame. Subtraction to prevent a same-frame timer resolution bug
            val endLoopIndex = duplicateLoopMaps.getOrPut("$loopName (end)") { loopsMap.size + duplicateLoopMaps.size}
            // Log end of loop sinceReset and the duration since the begging of the loopName pair
            SimpleLogger.recordOutput("LoopLogger/LoopDuration/$loopIndex $loopName", period.round(6)) // Using period val to not redo math, this value isn't really the period of the loop
            SimpleLogger.recordOutput("LoopLogger/SinceReset/$endLoopIndex $loopName (end)", sinceReset.round(6)) // Rounding to 6 digits to avoid logging high-resolution doubles
        } else {
            // Log beginning of the loop
            SimpleLogger.recordOutput("LoopLogger/Period/$loopIndex $loopName", period.round(6))
            SimpleLogger.recordOutput("LoopLogger/SinceReset/$loopIndex $loopName", sinceReset.round(6))
        }
    }

    /**
     * Executes the given block and returns elapsed time in seconds.
     */
    inline fun measureTimeMonotonic(body: () -> Unit): Double {
        val start = Timer.getMonotonicTimestamp()
        body()
        return Timer.getMonotonicTimestamp() - start
    }
}