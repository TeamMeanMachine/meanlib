package org.team2471.frc.lib.math

import edu.wpi.first.util.struct.Struct
import org.team2471.frc.lib.units.inches
import java.nio.ByteBuffer

class Vector2LStruct: Struct<Vector2L> {
    override fun getTypeClass(): Class<Vector2L> {
        return Vector2L::class.java
    }

    override fun getTypeString(): String {
        return "struct:Vector2L"
    }

    override fun getSize(): Int {
       return 16
    }

    override fun getSchema(): String {
        return "double xInches;double yInches"
    }

    override fun unpack(byteBuffer: ByteBuffer): Vector2L {
        val xInches = byteBuffer.getDouble()
        val yInches = byteBuffer.getDouble()
        return Vector2L(xInches.inches, yInches.inches)
    }

    override fun pack(byteBuffer: ByteBuffer, value: Vector2L) {
        byteBuffer.putDouble(value.x.asInches)
        byteBuffer.putDouble(value.y.asInches)
    }
}
