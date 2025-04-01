package org.team2471.frc.lib.vision

import com.team254.lib.util.InterpolatingDouble
import com.team254.lib.util.InterpolatingTreeMap
import edu.wpi.first.math.Matrix
import edu.wpi.first.math.Nat
import edu.wpi.first.math.StateSpaceUtil
import edu.wpi.first.math.VecBuilder
import edu.wpi.first.math.estimator.ExtendedKalmanFilter
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.numbers.N1
import edu.wpi.first.math.numbers.N2
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
import org.team2471.frc.lib.math.*
import org.team2471.frc.lib.units.asMeters
import org.team2471.frc.lib.units.meters
import org.team2471.frc.lib.util.getRealFPGATimestamp

// Huge thanks to 1678, thank you so much for pioneering this
// I would say you saved me a huge headache, but I've already been through that

class VisionPoseEstimator(
    odomStdDev: Matrix<N2, N1> = VecBuilder.fill(0.2, 0.2),
    defaultVisionStdDev: Matrix<N2, N1> = VecBuilder.fill(0.2, 0.2),
    questStdDev: Matrix<N2, N1> = VecBuilder.fill(0.2, 0.2)
) {

    val latestPos: Vector2L
        get() = try {
            if (isQuestConnected) {
                (questOffsetHistory.lastEntry().value.let { questPosHistory.lastEntry().value.plus(it) })
            } else {
                (odomOffsetHistory.lastEntry().value.let { odomPosHistory.lastEntry().value.plus(it) })
            }
        } catch (e: Exception) {
            Vector2L.Zeros
        }

    private var isQuestConnected: Boolean = false
    private var doesQuestExist: Boolean = false

    val odomPosHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)
    val odomOffsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)
    val questPosHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)
    val questOffsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L> = InterpolatingTreeMap(50)

    private var inReset = false
    private var odomHeartbeat = 0

    private val odomKalmanFilter = ExtendedKalmanFilter(
        Nat.N2(), // Dimensions of output (x, y)
        Nat.N2(), // Dimensions of predicted error shift (dx, dy) (always 0),
        Nat.N2(), // Dimensions of vision (x, y),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> u }, // The derivative of the output is predicted shift (always 0),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> x }, // The output is position (x, y),
        odomStdDev, // Standard deviation of position (uncertainty propagation with no vision),
        defaultVisionStdDev,// Standard deviation of vision measurements,
        0.02
    )

    private val questKalmanFilter = ExtendedKalmanFilter(
        Nat.N2(), // Dimensions of output (x, y)
        Nat.N2(), // Dimensions of predicted error shift (dx, dy) (always 0),
        Nat.N2(), // Dimensions of vision (x, y),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> u }, // The derivative of the output is predicted shift (always 0),
        { x: Matrix<N2?, N1?>?, u: Matrix<N2?, N1?>? -> x }, // The output is position (x, y),
        questStdDev, // Standard deviation of position (uncertainty propagation with no vision),
        defaultVisionStdDev,// Standard deviation of vision measurements,
        0.02
    )

    fun reset(
        newPos: Vector2L,
        currentTimestampSeconds: Double = getRealFPGATimestamp(),
        odometryReset: Boolean = false,
        questReset: Boolean = false
    ) {
        inReset = true

        resetHistories(odomKalmanFilter, odomPosHistory, odomOffsetHistory, newPos, currentTimestampSeconds, odometryReset)
        if (doesQuestExist) {
            resetHistories(questKalmanFilter, questPosHistory, questOffsetHistory, newPos, currentTimestampSeconds, questReset)
        }

//        val prevQuestPose =

//        predictedVelocity = Twist2d()

        inReset = false
    }

    private fun resetHistories(kalmanFilter: ExtendedKalmanFilter<N2, N2, N2>, baseHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L>, offsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L>, newPos: Vector2L, currentTimestampSeconds: Double, baseReset: Boolean) {
        offsetHistory.clear()
        kalmanFilter.reset()
        try {
            val prevPos = if (baseHistory.isNotEmpty()) Vector2L.Zeros else baseHistory.lastEntry().value

            if (baseReset) {
                baseHistory.clear()
                baseHistory[InterpolatingDouble(currentTimestampSeconds)] = newPos
                offsetHistory[InterpolatingDouble(currentTimestampSeconds)] = Vector2L.Zeros
                kalmanFilter.xhat = VecBuilder.fill(0.0, 0.0)
            } else {
                val newOffset = newPos - prevPos
                baseHistory.clear()
                baseHistory[InterpolatingDouble(currentTimestampSeconds)] = prevPos
                offsetHistory[InterpolatingDouble(currentTimestampSeconds)] = newOffset
                kalmanFilter.xhat = VecBuilder.fill(newOffset.x.asMeters, newOffset.y.asMeters)
            }
        } catch (_:Exception) {
            println("Reset Error D:")
        }
    }

    fun updateOdometry(
        currentTimestampSeconds: Double,
        odometryPose: Vector2L,
    ) {
        if (!inReset) {
            odomPosHistory[InterpolatingDouble(currentTimestampSeconds)] = odometryPose
            odomKalmanFilter.predict(VecBuilder.fill(0.0, 0.0), 0.02)
        }

        odomHeartbeat++
        MeanLogger.recordOutput("PoseEstimator/OdomHeartbeat", odomHeartbeat)

        if (odomOffsetHistory.isNotEmpty() && odomPosHistory.isNotEmpty()) {
            MeanLogger.recordOutput(
                "PoseEstimator/DriveOffsetPos",
                (odomOffsetHistory.lastEntry().value.let { odomPosHistory.lastEntry().value.plus(it) }).asMeters.toPose2d(
                    0.0
                )
            )
            MeanLogger.recordOutput("PoseEstimator/DrivePos", odomPosHistory.lastEntry().value.asMeters.toPose2d(0.0))
        }
    }

    fun updateQuest(
        questPose: Vector2L,
        isQuestConnected: Boolean,
        currentTimestampSeconds: Double = getRealFPGATimestamp()
    ) {
        this.isQuestConnected = isQuestConnected
        if (this.isQuestConnected) {
            doesQuestExist = true
            if (!inReset) {
                questPosHistory[InterpolatingDouble(currentTimestampSeconds)] = questPose
                questKalmanFilter.predict(VecBuilder.fill(0.0, 0.0), 0.02)
            }
        }
        if (questPosHistory.isNotEmpty() && questOffsetHistory.isNotEmpty()) {
            MeanLogger.recordOutput(
                "PoseEstimator/OffsetQuestPos",
                (questOffsetHistory.lastEntry().value.let { questPosHistory.lastEntry().value.plus(it) }).asMeters.toPose2d(
                    0.0
                )
            )

            MeanLogger.recordOutput("PoseEstimator/QuestPos", questPosHistory.lastEntry().value.asMeters.toPose2d(0.0))
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
            addVisionUpdateToHistory(globalPose, odomKalmanFilter, odomPosHistory, odomOffsetHistory)
//            if (doesQuestExist) {
                addVisionUpdateToHistory(globalPose, questKalmanFilter, questPosHistory, questOffsetHistory)
//            }
        }
    }

    private fun addVisionUpdateToHistory(globalPose: GlobalPose, kalmanFilter: ExtendedKalmanFilter<N2, N2, N2>, baseHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L>, offsetHistory: InterpolatingTreeMap<InterpolatingDouble, Vector2L>) {
        if (globalPose == GlobalPose.EmptyGlobalPose) {
            return
        }
        try {
            val basePose = baseHistory.getInterpolated(InterpolatingDouble(globalPose.timestampSeconds))
            val offset = globalPose.pose.translation.asVector2().meters - basePose

            val stdDevs = VecBuilder.fill(globalPose.stdDev, globalPose.stdDev)
            val covarianceMatrix = StateSpaceUtil.makeCovarianceMatrix(Nat.N2(), stdDevs)

            kalmanFilter.correct(
                VecBuilder.fill(0.0, 0.0),
                VecBuilder.fill(offset.x.asMeters, offset.y.asMeters),
                covarianceMatrix
            )


            offsetHistory[InterpolatingDouble(globalPose.timestampSeconds)] = Vector2L(kalmanFilter.getXhat(0).meters, kalmanFilter.getXhat(1).meters)
        } catch (e: Exception) {
//            println("Error updating vision pose")
//            e.printStackTrace()
        }
    }
}