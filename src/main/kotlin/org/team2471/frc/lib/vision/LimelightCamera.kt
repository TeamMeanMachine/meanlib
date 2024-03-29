package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Timer
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.meters

class LimelightCamera(
    networkTable: NetworkTable,
    name: String,
    robotToCamera: Transform3d,
    ): Camera(networkTable, name, robotToCamera) {

    // TODO(Unsure how limelight handles disconnects)
    override val isConnected: Boolean = true
//        get() = limelightTable exists

    override fun reset() {
        try {
            println("Implement reset")
        } catch (ex: Exception) {
            println("Error resetting cam $name: $ex")
        }
    }


    override fun getEstimatedGlobalPose(referencePose: Pose2d, distStDevCurve: MotionCurve?): GlobalPose? {
        val poseArray = LimelightHelpers.getBotPose3d_wpiBlue(name)
        var estimatedPose = Vector2L(poseArray.translation.x.meters, poseArray.translation.y.meters)

        if (estimatedPose == Vector2L(0.0.meters, 0.0.meters)) return null // estimatedPose returns origin if no tags seen

        lastGlobalPose = GlobalPose(estimatedPose, poseArray.rotation.z.degrees, 0.1, Timer.getFPGATimestamp())

        advantagePoseEntry.setAdvantagePose(estimatedPose, poseArray.rotation.z.degrees)
        // Todo figure out how      limelight formats their posearray

        return lastGlobalPose

    }
}