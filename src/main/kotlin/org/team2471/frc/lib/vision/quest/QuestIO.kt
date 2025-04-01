package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.*

interface QuestIO {
    class QuestIOInputs : LoggableInputs {

        var isConnected: Boolean = false

        var pose: Pose2d = Pose2d()

        override fun toLog(table: LogTable) {
            table.put("Quest/isConnected", isConnected)
            table.put("Quest/pose", pose)
        }

        override fun fromLog(table: LogTable) {
            isConnected = table.get("Quest/isConnected", isConnected)
            // idk a better way
            pose = table.get("Quest/pose", pose).first()
        }

    }

    fun resetHeading(heading: Angle)
    fun updateInputs(inputs: QuestIOInputs)
}

class QuestIOReal(val robotToQuest: Transform2d): QuestIO {
    private val table = NetworkTableInstance.getDefault().getTable("questnav")

    private val batteryPercentEntry = table.getDoubleTopic("batteryPercent").subscribe(0.0)
    private val frameCountEntry = table.getIntegerTopic("frameCount").subscribe(0)
    private val timestampEntry = table.getDoubleTopic("timestamp").subscribe(0.0)
    private val anglesEntry = table.getFloatArrayTopic("eulerAngles").subscribe(floatArrayOf(0.0f, 0.0f, 0.0f))
    private val positionsEntry = table.getFloatArrayTopic("position").subscribe(floatArrayOf(0.0f, 0.0f, 0.0f))
    private val isConnectedEntry = table.getEntry("isConnected")
    private val questQuaternion = table.getFloatArrayTopic("quaternion").subscribe(floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f));

    private val questMisoEntry = table.getIntegerTopic("miso").subscribe(0)
    private val questMosiEntry = table.getIntegerTopic("mosi").publish()

    private val heartbeatSub = table.getDoubleTopic("heartbeat/quest_to_robot").subscribe(0.0)
    private val heartbeatPub = table.getDoubleTopic("heartbeat/robot_to_quest").publish()
    private var lastHeartbeatId = 0.0;

    private var headingOffset = robotToQuest.rotation
    private var pose = Pose2d()
    private var wasConnected = false

    var isConnected = false

    val timestamp: Double
        get() = timestampEntry.get()

    private var prevTimestamp = 0.0

    private fun doHeartbeat() {
        val requestId = heartbeatSub.get()

        if (requestId > 0 && requestId != lastHeartbeatId) {
            heartbeatPub.set(requestId)
            lastHeartbeatId = requestId
        }
    }

    init {
        GlobalScope.launch {
            periodic(3.0) {
                val ts = timestamp
                isConnected = ts != prevTimestamp
                if (isConnected && !wasConnected) {
                    questMosiEntry.set(1)
                }
                if (isConnected) {
                    wasConnected = true
                }
                prevTimestamp = ts

            }
        }
        GlobalScope.launch {
            periodic {
                doHeartbeat()
            }
        }
    }

    override fun resetHeading(heading: Angle) {
        headingOffset = Rotation2d(heading.asWPIUnit) - pose.rotation
    }

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        inputs.isConnected = isConnected
        isConnectedEntry.setBoolean(inputs.isConnected)

        val rawPos = positionsEntry.get()
        val rawRotation = anglesEntry.get()

        val rotation = (Rotation2d(
            -rawRotation[1].degrees.asWPIUnit
        ) + robotToQuest.rotation)

        pose = Pose2d(
            Translation2d(
                rawPos[2].toDouble(),
                -rawPos[0].toDouble()
            ) + robotToQuest.translation.rotateBy(rotation),
            rotation
        )

        MeanLogger.recordOutput("Quest/VeryRawQuestPose", Pose2d(Translation2d(rawPos[2].toDouble(), -rawPos[0].toDouble()), Rotation2d(-rawRotation[1].degrees.asWPIUnit)))
        MeanLogger.recordOutput("Quest/RawQuestPose", pose)
        inputs.pose = Pose2d(pose.translation, pose.rotation.rotateBy(headingOffset))
        MeanLogger.recordOutput("Quest/AfterHeadingOffsetPose", inputs.pose)

    }
}

class QuestIOSim: QuestIO {
    var pose = Pose2d()
    override fun resetHeading(heading: Angle) {}

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        inputs.isConnected = true
        inputs.pose = Pose2d(-SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.x, -SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.y, SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.rotation)
        pose = inputs.pose
    }
}