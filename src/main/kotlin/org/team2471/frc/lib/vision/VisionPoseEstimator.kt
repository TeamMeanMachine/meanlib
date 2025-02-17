package org.team2471.frc.lib.vision

import com.team254.lib.util.InterpolatingDouble
import com.team254.lib.util.InterpolatingTreeMap
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.StateSpaceUtil
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.estimator.ExtendedKalmanFilter
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Twist2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N2
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.asMeters
import org.team2471.frc.lib.units.meters
import org.team2471.frc.lib.util.MeanLogger
import org.team2471.frc.lib.util.MovingAverageTwist2d
import org.team2471.frc.lib.util.getRealFPGATimestamp

// Huge thanks to 1678, thank you so much for pioneering this
// I would say you saved me a huge headache, but I've already been through that

class VisionPoseEstimator(
    val defaultStateStdDevs: Matrix<N2, N1> = VecBuilder.fill(0.2, 0.2),
    val defaultMeasurementStdDevs: Matrix<N2, N1> = VecBuilder.fill(0.2, 0.2)
) {

    val latestPos: Vector2L
        get() = try {
            (offsetHistory[offsetHistory.lastKey()]?.let { odomPosHistory[odomPosHistory.lastKey()]?.plus(it) })
                ?: Vector2L.Zeros
        } catch (e: Exception) {
            Vector2L.Zeros
        }

    val odomPosHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)
    val offsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)

    var inReset = false
    val isEnabled = false

    val kalmanFilter = ExtendedKalmanFilter(
        Nat.N2(), // Dimensions of output (x, y)
        Nat.N2(), // Dimensions of predicted error shift (dx, dy) (always 0),
        Nat.N2(), // Dimensions of vision (x, y),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> u }, // The derivative of the output is predicted shift (always 0),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> x }, // The output is position (x, y),
        defaultStateStdDevs, // Standard deviation of position (uncertainty propagation with no vision),
        defaultMeasurementStdDevs,// Standard deviation of vision measurements,
        0.02
    )

    fun reset(
        newPos: Vector2L,
        currentTimestampSeconds: Double = getRealFPGATimestamp(),
        odometryReset: Boolean = false
    ) {
        inReset = true
        val prevOdomPose = odomPosHistory.lastEntry()

        offsetHistory.clear()
        if (odometryReset) {
            odomPosHistory.clear()
            odomPosHistory[InterpolatingDouble(currentTimestampSeconds)] = newPos
            offsetHistory[InterpolatingDouble(currentTimestampSeconds)] = Vector2L.Zeros
        } else {
            offsetHistory[InterpolatingDouble(currentTimestampSeconds)] = newPos - prevOdomPose.value
        }

//        predictedVelocity = Twist2d()

        inReset = false
    }

    fun updateOdometry(
        currentTimestampSeconds: Double,
        odometryPose: Vector2L,
    ) {
        if (!inReset) {
            //        println(measuredVelocity)
            odomPosHistory[InterpolatingDouble(currentTimestampSeconds)] = odometryPose
            kalmanFilter.predict(VecBuilder.fill(0.0, 0.0), 0.02)
            //        this.predictedVelocity = predictedVelocity
            MeanLogger.recordOutput("odomPose", latestPos)
        }
    }

    fun addVisionUpdates(vararg globalPoses: GlobalPose) {
        for (globalPose in globalPoses) {
            addVisionUpdate(globalPose)
        }
    }

    fun addVisionUpdates(globalPoses: Collection<GlobalPose>) {
        for (globalPose in globalPoses) {
            addVisionUpdate(globalPose)
        }
    }

    fun addVisionUpdate(globalPose: GlobalPose) {
        if (!inReset) {
            if (globalPose == GlobalPose.EmptyGlobalPose) {
                return
            }
            try {
                val odometryPose = odomPosHistory.getInterpolated(InterpolatingDouble(globalPose.timestampSeconds))
                val odometryOffset = globalPose.pose.translation.asVector2().meters - odometryPose

                val stdDevs = VecBuilder.fill(globalPose.stdDev, globalPose.stdDev)
                val covarianceMatrix = StateSpaceUtil.makeCovarianceMatrix(Nat.N2(), stdDevs)

                kalmanFilter.correct(
                    VecBuilder.fill(0.0, 0.0),
                    VecBuilder.fill(odometryOffset.x.asMeters, odometryOffset.y.asMeters),
                    covarianceMatrix
                )


                offsetHistory[InterpolatingDouble(globalPose.timestampSeconds)] =
                    Vector2L(kalmanFilter.getXhat(0).meters, kalmanFilter.getXhat(1).meters)
            } catch (e: Exception) {
                println("Error updating vision pose: $e")
            }
        }
    }
}