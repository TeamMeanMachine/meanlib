package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.motion_profiling.MotionCurve

class LimelightCamera(
    networkTable: NetworkTable,
    name: String,
    robotToCamera: Transform3d,
    ): Camera(networkTable, name, robotToCamera) {

    val limelightTable: NetworkTable = NetworkTableInstance.getDefault().getTable("limelight")

    // TODO(Unsure how limelight handles disconnects)
    override val isConnected = false

    override fun reset() {
        TODO("Not yet implemented")
    }

    override fun getEstimatedGlobalPose(referencePose: Pose2d, distStDevCurve: MotionCurve?): GlobalPose? {
        val poseArray = limelightTable.getEntry("botpose").getDoubleArray(doubleArrayOf(0.0))

        if (poseArray.size == 1) return null

        // Todo figure out how      limelight formats their posearray

        return null

    }
}