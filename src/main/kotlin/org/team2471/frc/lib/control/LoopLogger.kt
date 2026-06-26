package org.team2471.frc.lib.control

import org.littletonrobotics.junction.Logger
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
        Logger.recordOutput("LoopLogger/LoopCount", loopsMap.size)
        resetTime = Timer.getMonotonicTimestamp()
    }

    /** Log the period and the time of the named loop at the current moment.  */
    fun record(loopName: String): Pair<Double, Double> {
        val loopIndex: Int = loopsMap.getOrPut(loopName) { loopsMap.size + duplicateLoopMaps.size }
        val now = Timer.getMonotonicTimestamp()
        val prevTime = prevTimes.put(loopName, now) ?: return Pair(0.0, 0.0) //put returns previous value
        val sinceReset = now - resetTime
        val period = now - prevTime

        if (prevTime > resetTime - 1e-10) { // Subtraction to prevent a same-frame timer resolution bug
            val endLoopIndex = duplicateLoopMaps.getOrPut("$loopName (end)") { loopsMap.size + duplicateLoopMaps.size}
            // Log end of loop sinceReset and the duration since the begging of the loopName pair
            Logger.recordOutput("LoopLogger/LoopDuration/$loopIndex $loopName", period) // Using period val to not redo math, this value isn't really the period of the loop
            Logger.recordOutput("LoopLogger/SinceReset/$endLoopIndex $loopName (end)", sinceReset)
        } else {
            // Log beginning of the loop
            Logger.recordOutput("LoopLogger/Period/$loopIndex $loopName", period)
            Logger.recordOutput("LoopLogger/SinceReset/$loopIndex $loopName", sinceReset)
        }


        return Pair(period, sinceReset)
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