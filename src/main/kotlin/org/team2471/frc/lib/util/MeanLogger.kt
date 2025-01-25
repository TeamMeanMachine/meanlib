package org.team2471.frc.lib.util

import edu.wpi.first.units.Measure
import edu.wpi.first.util.WPISerializable
import edu.wpi.first.util.struct.Struct
import edu.wpi.first.util.struct.StructSerializable
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asMeters
import org.team2471.frc.lib.math.toPose2d
import org.team2471.frc.lib.math.toTranslation2d
import org.team2471.frc.lib.units.Angle
import java.util.function.BooleanSupplier
import java.util.function.DoubleSupplier
import java.util.function.IntSupplier
import java.util.function.LongSupplier

/**
 * Thread safe kotlin wrapper for [Logger.recordOutput]
 */
object MeanLogger {

    private val lock = Any()

    fun recordOutput(key: String?, value: ByteArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<ByteArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Boolean) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: BooleanSupplier) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: BooleanArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<BooleanArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Int) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: IntSupplier) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: IntArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<IntArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Long) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: LongSupplier) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: LongArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<LongArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Float) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: FloatArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<FloatArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Double) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: DoubleSupplier) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: DoubleArray?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<DoubleArray?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: String?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<String?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Array<Array<String?>?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <E : Enum<E>?> recordOutput(key: String?, value: E) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <E : Enum<E>?> recordOutput(key: String?, value: Array<E>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <E : Enum<E>?> recordOutput(key: String?, value: Array<Array<E>?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <U : edu.wpi.first.units.Unit?> recordOutput(key: String?, value: Measure<U>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <T> recordOutput(key: String?, struct: Struct<T>?, value: T) {
        synchronized(lock) {
            Logger.recordOutput(key, struct, value)
        }
    }

    fun <T> recordOutput(key: String?, struct: Struct<T>?, vararg value: T) {
        synchronized(lock) {
            Logger.recordOutput(key, struct, *value)
        }
    }

    fun <T> recordOutput(key: String?, struct: Struct<T>?, value: Array<Array<T>?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, struct, value)
        }
    }

    fun <T : WPISerializable?> recordOutput(key: String?, value: T) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <T : StructSerializable?> recordOutput(key: String?, vararg value: T) {
        synchronized(lock) {
            Logger.recordOutput(key, *value)
        }
    }

    fun <T : StructSerializable?> recordOutput(key: String?, value: Array<Array<T>?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <R : Record?> recordOutput(key: String?, value: R) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun <R : Record?> recordOutput(key: String?, vararg value: R) {
        synchronized(lock) {
            Logger.recordOutput(key, *value)
        }
    }

    fun <R : Record?> recordOutput(key: String?, value: Array<Array<R>?>?) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: LoggedMechanism2d) {
        synchronized(lock) {
            Logger.recordOutput(key, value)
        }
    }

    fun recordOutput(key: String?, value: Vector2L) {
        synchronized(lock) {
            Logger.recordOutput(key, value.asMeters.toTranslation2d())
        }
    }

    fun recordOutput(key: String?, value: Vector2L, angle: Angle) {
        synchronized(lock) {
            Logger.recordOutput(key, value.asMeters.toPose2d(angle))
        }
    }
}