package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.Pose2d
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs

interface LimelightCameraIO {
    class LimelightCameraInputs: LoggableInputs {
        var mt2Result: LimelightHelpers.PoseEstimate?
            get() = if (mt2HasResult) {
                LimelightHelpers.PoseEstimate(
                    mt2Pose,
                    mt2TimestampSeconds,
                    mt2Latency,
                    mt2TagCount,
                    mt2TagSpan,
                    mt2AvgTagDist,
                    mt2AvgTagArea,
                    mt2RawFiducials
                )
            } else {
                null
            }
            set(value) {
                if (value != null) {
                    mt2Pose = value.pose
                    mt2TimestampSeconds = value.timestampSeconds
                    mt2Latency = value.latency
                    mt2TagCount = value.tagCount
                    mt2TagSpan = value.tagSpan
                    mt2AvgTagDist = value.avgTagDist
                    mt2AvgTagArea = value.avgTagArea
                    mt2RawFiducials = value.rawFiducials
                } else {
                    mt2HasResult = false
                    mt2HasRawFiducials = false
                }
            }



        var mt2HasResult: Boolean = false
        var mt2HasRawFiducials: Boolean = false

        var mt2Pose: Pose2d = Pose2d()
        var mt2TimestampSeconds: Double = 0.0
        var mt2Latency: Double = 0.0
        var mt2TagCount: Int = 0
        var mt2TagSpan: Double = 0.0
        var mt2AvgTagDist: Double = 0.0
        var mt2AvgTagArea: Double = 0.0

        var mt2RawFiducials: Array<LimelightHelpers.RawFiducial?>?
            get() = if (mt2HasRawFiducials) {
                Array(rfNum) { index ->
                    LimelightHelpers.RawFiducial(
                        rfId[index],
                        rfTxnc[index],
                        rfTync[index],
                        rfTa[index],
                        rfdistToCamera[index],
                        rfdistToRobot[index],
                        rfAmbiguity[index]
                    )
                }
            } else {
                null
            }
            set(value) {
                if (value != null) {
                    rfNum = value.size
                    rfId = value.map { it?.id ?: 0 }.toIntArray()
                    rfTxnc = value.map { it?.txnc ?: 0.0 }.toDoubleArray()
                    rfTync = value.map { it?.tync ?: 0.0 }.toDoubleArray()
                    rfTa = value.map { it?.ta ?: 0.0 }.toDoubleArray()
                    rfdistToCamera = value.map { it?.distToCamera ?: 0.0 }.toDoubleArray()
                    rfdistToRobot = value.map { it?.distToRobot ?: 0.0 }.toDoubleArray()
                    rfAmbiguity = value.map { it?.ambiguity ?: 0.0 }.toDoubleArray()
                } else {
                    mt2HasRawFiducials = false
                }
            }

        var rfNum: Int = 0
        var rfId: IntArray = intArrayOf(0)
        var rfTxnc: DoubleArray = doubleArrayOf(0.0)
        var rfTync: DoubleArray = doubleArrayOf(0.0)
        var rfTa: DoubleArray = doubleArrayOf(0.0)
        var rfdistToCamera: DoubleArray = doubleArrayOf(0.0)
        var rfdistToRobot: DoubleArray = doubleArrayOf(0.0)
        var rfAmbiguity: DoubleArray = doubleArrayOf(0.0)


        override fun toLog(table: LogTable) {
            table.put("hasResult", mt2HasResult)
            table.put("hasFiducials", mt2HasRawFiducials)

            table.put("pose", mt2Pose)
            table.put("timestampSeconds", mt2TimestampSeconds)
            table.put("latency", mt2Latency)
            table.put("tagCount", mt2TagCount)
            table.put("tagSpan", mt2TagSpan)
            table.put("avgTagDist", mt2AvgTagDist)
            table.put("avgTagArea", mt2AvgTagArea)

            table.put("rfNum", rfNum)
            table.put("rfID", rfId)
            table.put("rfTXNC ", rfTxnc)
            table.put("rfTYNC", rfTync)
            table.put("rfTA", rfTa)
            table.put("rfDistToCam", rfdistToCamera)
            table.put("rfDistToBot", rfdistToRobot)
            table.put("rfAmbiguity", rfAmbiguity)
        }

        override fun fromLog(table: LogTable) {
            table.get("hasResult", mt2HasResult)
            table.get("hasFiducials", mt2HasRawFiducials)

            table.get("pose", mt2Pose)
            table.get("timestampSeconds", mt2TimestampSeconds)
            table.get("latency", mt2Latency)
            table.get("tagCount", mt2TagCount)
            table.get("tagSpan", mt2TagSpan)
            table.get("avgTagDist", mt2AvgTagDist)
            table.get("avgTagArea", mt2AvgTagArea)

            table.get("rfNum", rfNum)
            table.get("rfID", rfId)
            table.get("rfTXNC ", rfTxnc)
            table.get("rfTYNC", rfTync)
            table.get("rfTA", rfTa)
            table.get("rfDistToCam", rfdistToCamera)
            table.get("rfDistToBot", rfdistToRobot)
            table.get("rfAmbiguity", rfAmbiguity)
        }

    }

    fun updateInputs(inputs: LimelightCameraInputs) {}
    fun reset() {}
}