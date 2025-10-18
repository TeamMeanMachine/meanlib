package org.team2471.frc.lib.localization

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.util.struct.Struct
import java.nio.ByteBuffer

/**
 * The PoseEstimate class represents an estimate of a robot's pose (position and orientation) at a
 * given point in time. It includes an identifier, the pose itself, and a flag indicating whether
 * vision data was used in the estimation.
 */
class PoseEstimate @JvmOverloads constructor(
    val id: Int = 0,
    val pose: Pose2d? = null,
    val hasVision: Boolean = false
) {
    override fun toString(): String = String.format("PoseEstimate(%s, %s, %s)", this.id, this.pose, hasVision)

    companion object {
        // Struct for serialization.
        @JvmField
        val struct: PoseEstimateStruct = PoseEstimateStruct()
    }
}
/**
 * PoseEstimateStruct is a class that implements the Struct interface for the PoseEstimate type. It
 * provides methods to get the type class, type name, size, schema, and nested structures. It also
 * provides methods to pack and unpack PoseEstimate objects to and from ByteBuffers.
 *
 *
 * The PoseEstimateStruct class is immutable and ensures that the PoseEstimate objects are
 * correctly serialized and deserialized with the appropriate schema.
 */
class PoseEstimateStruct : Struct<PoseEstimate> {
    override fun getTypeClass(): Class<PoseEstimate> = PoseEstimate::class.java

    override fun getTypeName(): String = "PoseEstimate"

    override fun getSize(): Int = Pose2d.struct.size + Struct.kSizeInt32 * 2

    override fun getSchema(): String = "int32 id;" + "Pose2d pose;" + "int32 hasVision;"

    override fun getNested(): Array<Struct<*>> = arrayOf(Pose2d.struct)

    override fun unpack(bb: ByteBuffer): PoseEstimate {
        val id = bb.getInt()
        val pose = Pose2d.struct.unpack(bb)
        val hasVision = bb.getInt() != 0
        return PoseEstimate(id, pose, hasVision)
    }

    override fun pack(bb: ByteBuffer, value: PoseEstimate) {
        bb.putInt(value.id)
        Pose2d.struct.pack(bb, value.pose)
        bb.putInt(if (value.hasVision) 1 else 0)
    }

    override fun isImmutable(): Boolean = true
}