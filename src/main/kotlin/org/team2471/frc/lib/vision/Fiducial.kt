package org.team2471.frc.lib.vision

import edu.wpi.first.apriltag.AprilTag
import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.util.struct.Struct
import org.team2471.frc.lib.units.asMeters
import org.team2471.frc.lib.units.inches
import java.nio.ByteBuffer

// An ID of -1 indicates this is an unlabeled fiducial (e.g. retroreflective tape)
class Fiducial(val type: Type, val id: Int, val pose: Pose3d, val size: Double) {
    enum class Type(@JvmField val value: Int) {
        RETROREFLECTIVE(0),
        APRILTAG(1)
    }

    val x: Double
        get() = pose.x

    val y: Double
        get() = pose.y

    val z: Double
        get() = pose.z

    val xRot: Double
        get() = pose.rotation.x

    val yRot: Double
        get() = pose.rotation.y

    val zRot: Double
        get() = pose.rotation.z

    override fun toString(): String = "(${type.name}, ID: $id, Pose: $pose, Size: $size)"

    companion object {
        // Struct for serialization.
        val struct: FiducialStruct = FiducialStruct()

        private val aprilTagSize = 6.5.inches.asMeters // m

        fun constructFiducialList(aprilTags: List<AprilTag>): Array<Fiducial>  {
            return Array(aprilTags.size) {
                val tag = aprilTags[it]
                Fiducial(
                    Type.APRILTAG,
                    tag.ID,
                    tag.pose,
                    aprilTagSize
                )
            }
        }
    }
}

class FiducialStruct : Struct<Fiducial> {
    override fun getTypeClass(): Class<Fiducial> = Fiducial::class.java

    override fun getTypeName(): String = "Fiducial"

    override fun getSize(): Int =
        Pose3d.struct.size + Struct.kSizeInt32 * 2 + Struct.kSizeDouble

    override fun getSchema(): String =
        "int32 type;int32 id;Pose3d pose;double size;"

    override fun getNested(): Array<Struct<*>> = arrayOf(Pose3d.struct)

    override fun unpack(bb: ByteBuffer): Fiducial {
        val type = Fiducial.Type.entries[bb.getInt()]
        val id = bb.getInt()
        val pose = Pose3d.struct.unpack(bb)
        val size = bb.getDouble()
        return Fiducial(type, id, pose, size)
    }

    override fun pack(bb: ByteBuffer, value: Fiducial) {
        bb.putInt(value.type.value)
        bb.putInt(value.id)
        Pose3d.struct.pack(bb, value.pose)
        bb.putDouble(value.size)
    }

    override fun isImmutable(): Boolean = true
}
