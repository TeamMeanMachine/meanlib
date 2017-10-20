package org.team2471.frc.lib.data

import edu.wpi.first.wpilibj.CircularBuffer
import org.team2471.frc.lib.math.lerp
import java.lang.Math.ceil
import java.lang.Math.floor

class InterpolatableCircularBuffer(size: Int) : CircularBuffer(size) {
    /**
     * Get the linear interpolation of elements between provided index relative to the start of the buffer.
     *
     * @return Linear interpolation of elements between index starting from front of buffer.
     */
    fun interpolate(index: Double) = lerp(
            this[floor(index).toInt()], // min
            this[ceil(index).toInt()], // max
            index % 1) // k

    /**
     * Get the element at the provided index relative to the start of the buffer.
     *
     * @return Element at index starting from front of buffer.
     */
    override operator fun get(index: Int) = super.get(index)

    /**
     * Alias for [interpolate]
     *
     * @see interpolate
     */
    operator fun get(index: Double) = interpolate(index)
}