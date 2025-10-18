package org.team2471.frc.lib.localization

import edu.wpi.first.math.MatBuilder
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N3
import edu.wpi.first.math.numbers.N8
import edu.wpi.first.util.struct.Struct
import java.nio.ByteBuffer
import java.util.*

/**
 * The CameraInfo class holds information about a camera's transform, camera matrix, and distortion
 * coefficients.
 *
 *
 * This class is used to encapsulate the camera's intrinsic and extrinsic parameters, which are
 * essential for camera calibration and image processing tasks in robotics.
 *
 *
 * It contains the following fields:
 *
 *
 *  * `m_transform` - The 3D transform representing the camera's position and orientation.
 *  * `m_cameraMatrix` - An optional 3x3 matrix representing the camera's intrinsic
 * parameters.
 *  * `m_distCoeffs` - An optional 8x1 matrix representing the camera's distortion
 * coefficients.
 *
 *
 *
 * The class provides getter methods to access these fields:
 *
 *
 *  * [.getTransform] - Returns the camera's transform.
 *  * [.getCameraMatrix] - Returns the camera's intrinsic matrix, if available.
 *  * [.getDistCoeffs] - Returns the camera's distortion coefficients, if available.
 *
 *
 *
 * Additionally, it includes a static nested class `CameraInfoStruct` for serialization
 * purposes.
 *
 * @param transform The 3D transform representing the camera's position and orientation.
 * @param cameraMatrix An optional 3x3 matrix representing the camera's intrinsic parameters.
 * @param distCoeffs An optional 8x1 matrix representing the camera's distortion coefficients.
 */
class CameraInfo(
    val transform: Transform3d,
    val cameraMatrix: Optional<Matrix<N3, N3>>,
    val distCoeffs: Optional<Matrix<N8, N1>>
) {
    companion object {
        // Struct for serialization.
        @JvmField
        val struct: CameraInfoStruct = CameraInfoStruct()
    }
}
/**
 * CameraInfoStruct is a structure that implements the Struct interface for the CameraInfo class. It
 * provides methods to get the type class, type name, size, schema, nested structures, and to pack
 * and unpack CameraInfo objects from a ByteBuffer.
 *
 *
 * The structure includes the following fields:
 *
 *
 *  * Transform3d transform
 *  * int32 hasCameraMatrix
 *  * double M00, M01, M02, M10, M11, M12, M20, M21, M22
 *  * int32 hasDistCoeffs
 *  * double d0, d1, d2, d3, d4, d5, d6, d7
 *
 * Used by NetworkTables to efficiently transmit camera calibration data between robot and driver
 * station.
 *
 * @see ByteBuffer
 * @see CameraInfo
 * @see Transform3d
 * @see Struct
 */
class CameraInfoStruct : Struct<CameraInfo> {
    override fun getTypeClass(): Class<CameraInfo> = CameraInfo::class.java

    override fun getTypeName(): String = "CameraInfo"

    override fun getSize(): Int = Transform3d.struct.size + Struct.kSizeInt32 * 2 + Struct.kSizeDouble * 17

    override fun getSchema(): String =
        ("Transform3d transform;"
                + "int32 hasCameraMatrix;"
                + "double M00;"
                + "double M01;"
                + "double M02;"
                + "double M10;"
                + "double M11;"
                + "double M12;"
                + "double M20;"
                + "double M21;"
                + "double M22;"
                + "int32 hasDistCoeffs;"
                + "double d0;"
                + "double d1;"
                + "double d2;"
                + "double d3;"
                + "double d4;"
                + "double d5;"
                + "double d6;"
                + "double d7;")

    override fun getNested(): Array<Struct<*>> = arrayOf(Transform3d.struct)

    /**
     * Unpacks camera information from a ByteBuffer into a CameraInfo object.
     *
     * @param bb The ByteBuffer containing the packed camera information data
     * @return A new CameraInfo object containing the unpacked camera transformation, optional camera
     * matrix, and optional distortion coefficients
     *
     * The ByteBuffer should contain, in order: - A Transform3d struct - A boolean flag for
     * camera matrix presence - Nine doubles representing a 3x3 camera matrix (if present) - A
     * boolean flag for distortion coefficients presence - Eight doubles representing distortion
     * coefficients (if present)
     */
    override fun unpack(bb: ByteBuffer): CameraInfo {
        val transform = Transform3d.struct.unpack(bb)
        val hasCameraMatrix = bb.getInt() != 0
        val m00 = bb.getDouble()
        val m01 = bb.getDouble()
        val m02 = bb.getDouble()
        val m10 = bb.getDouble()
        val m11 = bb.getDouble()
        val m12 = bb.getDouble()
        val m20 = bb.getDouble()
        val m21 = bb.getDouble()
        val m22 = bb.getDouble()
        val hasDistCoeffs = bb.getInt() != 0
        val d0 = bb.getDouble()
        val d1 = bb.getDouble()
        val d2 = bb.getDouble()
        val d3 = bb.getDouble()
        val d4 = bb.getDouble()
        val d5 = bb.getDouble()
        val d6 = bb.getDouble()
        val d7 = bb.getDouble()
        return CameraInfo(
            transform,
            if (hasCameraMatrix) {
                Optional.of(MatBuilder.fill(Nat.N3(), Nat.N3(), m00, m01, m02, m10, m11, m12, m20, m21, m22))
            } else {
                Optional.empty<Matrix<N3, N3>>()
            },
            if (hasDistCoeffs) {
                Optional.of(MatBuilder.fill(Nat.N8(), Nat.N1(), d0, d1, d2, d3, d4, d5, d6, d7))
            } else {
                Optional.empty<Matrix<N8, N1>>()
            }
        )
    }

    /**
     * Packs camera information into a ByteBuffer. This method serializes a CameraInfo object,
     * including its transformation matrix, camera matrix (if present), and distortion coefficients
     * (if present).
     *
     * @param bb The ByteBuffer to pack the data into
     * @param value The CameraInfo object containing the data to be packed
     */
    override fun pack(bb: ByteBuffer, value: CameraInfo) {
        Transform3d.struct.pack(bb, value.transform)
        val hasCameraMatrix = value.cameraMatrix.isPresent
        bb.putInt(if (hasCameraMatrix) 1 else 0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(0, 0) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(0, 1) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(0, 2) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(1, 0) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(1, 1) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(1, 2) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(2, 0) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(2, 1) else 0.0)
        bb.putDouble(if (hasCameraMatrix) value.cameraMatrix.get().get(2, 2) else 0.0)
        val hasDistCoeffs = value.distCoeffs.isPresent
        bb.putInt(if (hasDistCoeffs) 1 else 0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(0, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(1, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(2, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(3, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(4, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(5, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(6, 0) else 0.0)
        bb.putDouble(if (hasDistCoeffs) value.distCoeffs.get().get(7, 0) else 0.0)
    }

    override fun isImmutable(): Boolean = true
}
