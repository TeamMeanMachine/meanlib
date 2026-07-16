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

import edu.wpi.first.networktables.NetworkTableInstance
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
import java.util.concurrent.ConcurrentHashMap
import java.util.function.*

/** Central class for recording and replaying log data.
 * [SimpleLogger] is thread safe, unlike [Logger]
 * */
object SimpleLogger { // REPLACES Logger.recordOutput

    private const val TABLE_ROOT = "AdvantageKit/RealOutputs"
    private val table = NetworkTableInstance.getDefault().getTable(TABLE_ROOT)

    // Publishers are cached per key so repeated calls each loop don't recreate
    // Topic/Publisher objects (which is both slow and leaks NT resources).
    private val publishers = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrCreate(key: String, create: () -> T): T =
        publishers.getOrPut(key) { create() as Any } as T

    // ---------- Booleans ----------

    fun recordOutput(key: String, value: Boolean) {
        getOrCreate(key) { table.getBooleanTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: BooleanSupplier) = recordOutput(key, value.asBoolean)

    fun recordOutput(key: String, value: BooleanArray) {
        getOrCreate(key) { table.getBooleanArrayTopic(key).publish() }.set(value)
    }

    // ---------- Integers / Longs ----------

    fun recordOutput(key: String, value: Int) {
        getOrCreate(key) { table.getIntegerTopic(key).publish() }.set(value.toLong())
    }

    fun recordOutput(key: String, value: IntSupplier) = recordOutput(key, value.asInt)

    fun recordOutput(key: String, value: IntArray) {
        getOrCreate(key) { table.getIntegerArrayTopic(key).publish() }
            .set(LongArray(value.size) { i -> value[i].toLong() })
    }

    fun recordOutput(key: String, value: Long) {
        getOrCreate(key) { table.getIntegerTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: LongSupplier) = recordOutput(key, value.asLong)

    fun recordOutput(key: String, value: LongArray) {
        getOrCreate(key) { table.getIntegerArrayTopic(key).publish() }.set(value)
    }

    // ---------- Floats / Doubles ----------

    fun recordOutput(key: String, value: Float) {
        getOrCreate(key) { table.getFloatTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: FloatArray) {
        getOrCreate(key) { table.getFloatArrayTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: Double) {
        getOrCreate(key) { table.getDoubleTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: DoubleSupplier) = recordOutput(key, value.asDouble)

    fun recordOutput(key: String, value: DoubleArray) {
        getOrCreate(key) { table.getDoubleArrayTopic(key).publish() }.set(value)
    }

    // ---------- Strings ----------

    fun recordOutput(key: String, value: String) {
        getOrCreate(key) { table.getStringTopic(key).publish() }.set(value)
    }

    fun recordOutput(key: String, value: Array<String>) {
        getOrCreate(key) { table.getStringArrayTopic(key).publish() }.set(value)
    }

    // ---------- Raw bytes ----------

    fun recordOutput(key: String, value: ByteArray) {
        getOrCreate(key) { table.getRawTopic(key).publish("raw") }.set(value)
    }

    // ---------- Enums ----------

    fun <E : Enum<E>> recordOutput(key: String, value: E) {
        recordOutput(key, value.name)
    }

    fun <E : Enum<E>> recordOutput(key: String, value: Array<E>) {
        recordOutput(key, Array(value.size) { i -> value[i].name })
    }

    // ---------- Struct-serializable objects (Pose2d, ChassisSpeeds, Rotation2d, etc.) ----------

    fun <T> recordOutput(key: String, struct: Struct<T>, value: T) {
        getOrCreate(key) { table.getStructTopic(key, struct).publish() }.set(value)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> recordOutput(key: String, struct: Struct<T>, vararg value: T) {
        getOrCreate(key) { table.getStructArrayTopic(key, struct).publish() }
            .set(value as Array<T>)
    }

    fun <U : edu.wpi.first.units.Unit> recordOutput(key: String, value: Measure<U>) {
        recordOutput(key, value.baseUnitMagnitude())
    }

    // Auto-detect struct serialization for StructSerializable types by reflecting on the
    // conventional public static `struct` field (mirrors what AdvantageKit's WPISerializable
    // overload does under the hood). Falls back to an explicit Struct<T> overload above if
    // your type doesn't expose one.
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : StructSerializable> recordOutput(key: String, value: T) {
        val struct = T::class.java.getField("struct").get(null) as Struct<T>
        recordOutput(key, struct, value)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : StructSerializable> recordOutput(key: String, vararg value: T) {
        val struct = T::class.java.getField("struct").get(null) as Struct<T>
        recordOutput(key, struct, *value)
    }
}