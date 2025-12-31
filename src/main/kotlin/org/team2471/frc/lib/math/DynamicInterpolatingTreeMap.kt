package org.team2471.frc.lib.math

import edu.wpi.first.math.interpolation.Interpolator
import edu.wpi.first.math.interpolation.InverseInterpolator
import java.util.Comparator
import java.util.TreeMap

/**
 * This is a copy of WPILib's InterpolatingTreeMap that allows for deleting of keys
 *
 * Interpolating Tree Maps are used to get values at points that are not defined by making a guess
 * from points that are defined. This uses linear interpolation.
 *
 * <p>{@code K} must implement {@link Comparable}, or a {@link Comparator} on {@code K} can be
 * provided.
 *
 * @param <K> The type of keys held in this map.
 * @param <V> The type of values held in this map.
 */

open class DynamicInterpolatingTreeMap<K, V> {
    private val map: TreeMap<K, V>

    private val inverseInterpolator: InverseInterpolator<K>
    private val interpolator: Interpolator<V>

    private val maxSize: Int

    /**
     * Constructs an InterpolatingTreeMap.
     *
     * @param inverseInterpolator Function to use for inverse interpolation of the keys.
     * @param interpolator Function to use for interpolation of the values.
     * @param maxSize The maximum size of the map. If zero, no maximum size will be enforced. Otherwise, the lowest key will be deleted if adding a key pushes the size over the specified value.
     */
    constructor(inverseInterpolator: InverseInterpolator<K>, interpolator: Interpolator<V>, maxSize: Int = 0) {
        map = TreeMap<K, V>()
        this.inverseInterpolator = inverseInterpolator
        this.interpolator = interpolator

        this.maxSize = maxSize
    }

    /**
     * Constructs an InterpolatingTreeMap using `comparator`.
     *
     * @param inverseInterpolator Function to use for inverse interpolation of the keys.
     * @param interpolator Function to use for interpolation of the values.
     * @param comparator Comparator to use on keys.
     * @param maxSize The maximum size of the map. If zero, no maximum size will be enforced. Otherwise, the lowest key will be deleted if adding a key pushes the size over the specified value.
     */
    constructor(
        inverseInterpolator: InverseInterpolator<K>,
        interpolator: Interpolator<V>,
        comparator: Comparator<K>,
        maxSize: Int = 0
    ) {
        this.inverseInterpolator = inverseInterpolator
        this.interpolator = interpolator
        map = TreeMap<K, V>(comparator)

        this.maxSize = maxSize
    }

    /**
     * Inserts a key-value pair.
     *
     * @param key The key.
     * @param value The value.
     */
    fun put(key: K, value: V) {
        map.put(key, value)

        if (maxSize > 0 && map.size > maxSize) {
            remove(map.firstKey())
        }
    }

    /**
     * Removes a key-value pair for the specified key.
     *
     * @param key The key.
     */
    fun remove(key: K) {
        map.remove(key)
    }

    /**
     * Replaces the value for the specified key only if it is currently mapped to some value.
     *
     * @param key The key.
     * @param newValue The new value.
     */
    fun replace(key: K, newValue: V) {
        map.replace(key, newValue)
    }

    /**
     * Returns the value associated with a given key.
     *
     *
     * If there's no matching key, the value returned will be an interpolation between the keys
     * before and after the provided one, using the [Interpolator] and [ ] provided.
     *
     * @param key The key.
     * @return The value associated with the given key.
     */
    fun get(key: K): V? {
        val `val` = map.get(key)
        if (`val` == null) {
            val ceilingKey = map.ceilingKey(key)
            val floorKey = map.floorKey(key)

            if (ceilingKey == null && floorKey == null) {
                return null
            }
            if (ceilingKey == null) {
                return map[floorKey]
            }
            if (floorKey == null) {
                return map[ceilingKey]
            }
            val floor = map[floorKey]
            val ceiling = map[ceilingKey]

            return interpolator.interpolate(
                floor, ceiling, inverseInterpolator.inverseInterpolate(floorKey, ceilingKey, key)
            )
        } else {
            return `val`
        }
    }

    /** Clears the contents.  */
    fun clear() {
        map.clear()
    }
}