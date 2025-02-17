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
    private val table = NetworkTableInstance.getDefault().getTable("PoseEstimator")
    private val odomPosePub = table.getStructTopic("Odometry Pose", Pose2d.struct).publish()
    private val posePub = table.getStructTopic("Pose", Pose2d.struct).publish()
    private val lastVisionPosePub = table.getStructTopic("Last Vision Pose", Pose2d.struct).publish()
    private val lastStdDevPub = table.getDoubleTopic("Last StdDev").publish()

    val debugModeEntry = table.getEntry("Debug Mode")
    val debugMode: Boolean
        get() = debugModeEntry.getBoolean(false)

    val latestPos: Vector2L
        get() = try {
            (offsetHistory[offsetHistory.lastKey()]?.let { odomPosHistory[odomPosHistory.lastKey()]?.plus(it) })
                ?: Vector2L.Zeros
        } catch (e: Exception) {
            Vector2L.Zeros
        }

    val odomPosHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)
    val offsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)

    var rawMeasuredVelocity = Twist2d()

    val filteredMeasuredVelocity
        get() = measuredVelocityFilter.latestTwist

    private var measuredVelocityFilter = MovingAverageTwist2d(5)

    // May be useful for shoot on the move.
//    var predictedVelocity = Twist2d()

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


        measuredVelocityFilter.reset()

//        predictedVelocity = Twist2d()

        inReset = false
    }

    fun updateOdometry(
        currentTimestampSeconds: Double,
        odometryPose: Vector2L,
        measuredVelocity: Twist2d,/* predictedVelocity: Twist2d*/
    ) {
        if (!inReset) {
            //        println(measuredVelocity)
            odomPosHistory[InterpolatingDouble(currentTimestampSeconds)] = odometryPose
            kalmanFilter.predict(VecBuilder.fill(0.0, 0.0), 0.02)
            this.rawMeasuredVelocity = measuredVelocity
            this.measuredVelocityFilter.update(measuredVelocity)
            //        this.predictedVelocity = predictedVelocity
            MeanLogger.recordOutput("odomPose", latestPos)
        }
    }

    // This is here just to be compatible with meanlib units/classes. Remove when switched to WpiLib
    fun updateOdometry(
        currentTimestampSeconds: Double,
        odometryPose: Vector2L,
        measuredTranslationalVelocity: Vector2L,
        measuredRotationalVelocity: Angle/*, predictedTranslationalVelocity: Vector2L, predictedRotationalVelocity: Angle*/
    ) {
        this.updateOdometry(
            currentTimestampSeconds,
            odometryPose,
            Twist2d(
                measuredTranslationalVelocity.x.asMeters,
                measuredTranslationalVelocity.y.asMeters,
                measuredRotationalVelocity.asDegrees
            ),
//        Twist2d(predictedTranslationalVelocity.x.asMeters, predictedTranslationalVelocity.y.asMeters, predictedRotationalVelocity.asDegrees)
        )
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

                if (debugMode) {
                    println("odomPose ${odometryPose}")
                }

                val odometryOffset = globalPose.pose.translation.asVector2().meters - odometryPose

                if (debugMode) {
                    println("odomOffset ${odometryOffset}")
                }

                val stdDevs = VecBuilder.fill(globalPose.stdDev, globalPose.stdDev)

                if (debugMode) {
                    println("stdDevs ${stdDevs}")
                }

                val covarianceMatrix = StateSpaceUtil.makeCovarianceMatrix(Nat.N2(), stdDevs)

                if (debugMode) {
                    println("Covariance: $covarianceMatrix")
                }

                kalmanFilter.correct(
                    VecBuilder.fill(0.0, 0.0),
                    VecBuilder.fill(odometryOffset.x.asMeters, odometryOffset.y.asMeters),
                    covarianceMatrix
                )

                if (debugMode) {
                    println("corrected")
                }

                offsetHistory[InterpolatingDouble(globalPose.timestampSeconds)] =
                    Vector2L(kalmanFilter.getXhat(0).meters, kalmanFilter.getXhat(1).meters)
                if (debugMode) {
                    println("pose: ${Vector2L(kalmanFilter.getXhat(0).meters, kalmanFilter.getXhat(1).meters)}")
                }
            } catch (e: Exception) {
                println("Error updating vision pose: $e")
            }
            lastVisionPosePub.set(globalPose.pose)
            lastStdDevPub.set(globalPose.stdDev)
        }
    }

    init {
        debugModeEntry.setBoolean(false)

        GlobalScope.launch {
            periodic {
                if (odomPosHistory.isNotEmpty()) {
                    odomPosePub.set(odomPosHistory.lastEntry().value.asMeters.toPose2d(0.0))
                }
                posePub.set(latestPos.asMeters.toPose2d(0.0))
            }
        }
    }
}