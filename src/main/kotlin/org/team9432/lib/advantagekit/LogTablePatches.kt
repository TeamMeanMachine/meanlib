package org.team9432.lib.advantagekit

import edu.wpi.first.units.Measure
import edu.wpi.first.units.MutableMeasure
import edu.wpi.first.units.Unit
import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.protobuf.Protobuf
import edu.wpi.first.util.struct.Struct
import edu.wpi.first.util.struct.StructSerializable
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.LogTable.LogValue
import us.hebi.quickbuf.ProtoMessage

// This is all just to get correct type inference working with kotlin
fun <T: StructSerializable> LogTable.kGet(key: String, defaultValue: T) = this.getFromWPISerializable(key, defaultValue)
private fun <T: WPISerializable> LogTable.getFromWPISerializable(key: String, defaultValue: T): T = this.get(key, defaultValue)

fun LogTable.kPut(key: String, value: LogValue) = put(key, value)
fun LogTable.kPut(key: String, value: ByteArray) = put(key, value)
fun LogTable.kPut(key: String, value: Boolean) = put(key, value)
fun LogTable.kPut(key: String, value: Int) = put(key, value)
fun LogTable.kPut(key: String, value: Long) = put(key, value)
fun LogTable.kPut(key: String, value: Float) = put(key, value)
fun LogTable.kPut(key: String, value: Double) = put(key, value)
fun LogTable.kPut(key: String, value: String) = put(key, value)
fun <E: Enum<E>> LogTable.kPut(key: String, value: E) = put(key, value)
fun <U: Unit<U>> LogTable.kPut(key: String, value: Measure<U>) = put(key, value)
fun LogTable.kPut(key: String, value: BooleanArray) = put(key, value)
fun LogTable.kPut(key: String, value: IntArray) = put(key, value)
fun LogTable.kPut(key: String, value: LongArray) = put(key, value)
fun LogTable.kPut(key: String, value: FloatArray) = put(key, value)
fun LogTable.kPut(key: String, value: DoubleArray) = put(key, value)
fun LogTable.kPut(key: String, value: Array<String>) = put(key, value)
fun <T> LogTable.kPut(key: String, struct: Struct<T>, value: T) = put(key, struct, value)
fun <T> LogTable.kPut(key: String, struct: Struct<T>, value: Array<T>) = put(key, struct, *value)
fun <T, MessageType: ProtoMessage<*>> LogTable.kPut(key: String, proto: Protobuf<T, MessageType>, value: T) = put(key, proto, value)
fun <T: WPISerializable> LogTable.kPut(key: String, value: T) = put(key, value)
fun <T: StructSerializable> LogTable.kPut(key: String, value: Array<T>) = put(key, *value)

fun LogTable.kGet(key: String): LogValue = get(key)
fun LogTable.kGet(key: String, defaultValue: ByteArray): ByteArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Boolean) = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Int) = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Long) = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Float) = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Double) = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: String): String = get(key, defaultValue)
fun <E: Enum<E>> LogTable.kGet(key: String, defaultValue: E): E = get(key, defaultValue)
fun <U: Unit<U>> LogTable.kGet(key: String, defaultValue: Measure<U>): Measure<U> = get(key, defaultValue)
fun <U: Unit<U>> LogTable.kGet(key: String, defaultValue: MutableMeasure<U>): MutableMeasure<U> = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: BooleanArray): BooleanArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: IntArray): IntArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: LongArray): LongArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: FloatArray): FloatArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: DoubleArray): DoubleArray = get(key, defaultValue)
fun LogTable.kGet(key: String, defaultValue: Array<String>): Array<String> = get(key, defaultValue)
fun <T> LogTable.kGet(key: String, struct: Struct<T>, defaultValue: T): T = get(key, struct, defaultValue)
fun <T> LogTable.kGet(key: String, struct: Struct<T>, defaultValue: Array<T>): Array<T> = get(key, struct, *defaultValue)
fun <T, MessageType: ProtoMessage<*>> LogTable.kGet(key: String, proto: Protobuf<T, MessageType>, defaultValue: T): T = get(key, proto, defaultValue)
fun <T: WPISerializable> LogTable.kGet(key: String, defaultValue: T): T = get(key, defaultValue)
fun <T: StructSerializable> LogTable.kGet(key: String, defaultValue: Array<T>): Array<T> = get(key, *defaultValue)
