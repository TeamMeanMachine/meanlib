package org.team2471.frc.lib.units

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.util.struct.Struct
import java.nio.ByteBuffer

/**
 * Represents a struct for handling Angle objects in byte buffer operations.
 * It encodes it the exact same way as a Rotation2d, so that it can be compatible with apps like AdvantageScope
 */
class AngleStruct : Struct<Angle> {
    /**
     * Gets the class object of the Angle type.
     *
     * @return The Class object representing the Angle type.
     */
    override fun getTypeClass(): Class<Angle> {
        return Angle::class.java
    }

    override fun getTypeName(): String {
        return typeClass.name
    }

    /**
     * Gets the string representation of the struct type.
     *
     * @return A string representing the struct type as "struct:Rotation2d".
     */
    override fun getTypeString(): String {
        return "struct:Rotation2d"
    }

    /**
     * Gets the size of the struct in bytes.
     *
     * @return The size of the struct, which is 8 bytes (size of a double).
     */
    override fun getSize(): Int {
        return 8
    }

    /**
     * Gets the schema of the struct.
     *
     * @return A string representing the schema.
     */
    override fun getSchema(): String {
        return "double value"
    }

    /**
     * Unpacks an Angle object from a ByteBuffer.
     *
     * @param bb The ByteBuffer containing the packed angle value.
     * @return An Angle object created from the unpacked double value.
     */
    override fun unpack(bb: ByteBuffer): Angle {
        return bb.getDouble().radians
    }

    /**
     * Packs an Angle object into a ByteBuffer.
     *
     * @param bb The ByteBuffer to pack the angle value into.
     * @param value The Angle object to be packed.
     */
    override fun pack(bb: ByteBuffer, value: Angle) {
        bb.putDouble(value.asRadians)
    }
}