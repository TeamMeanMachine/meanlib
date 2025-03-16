package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.coroutines.delay
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.math.Vector2L
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

    suspend fun reset(heading: Angle)
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

    private var headingOffset = Rotation2d()

    var isConnected = false

    val timestamp: Double
        get() = timestampEntry.get()

    private var prevTimestamp = 0.0

    init {
        GlobalScope.launch {
            periodic(3.0) {
                val ts = timestamp
                isConnected = ts != prevTimestamp
                prevTimestamp = ts
            }
        }
    }

    // Only call if quest is connected
    override suspend fun reset(heading: Angle) {
        if (questMisoEntry.get() != 99L) {
            questMosiEntry.set(1)
//            suspendUntil { questMisoEntry.get() == 99L }
            delay(0.25)
            questMosiEntry.set(0)
        }
        headingOffset = Rotation2d(heading.asWPIUnit)
    }

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        inputs.isConnected = isConnected
        isConnectedEntry.setBoolean(inputs.isConnected)

        val rawPos = positionsEntry.get()
        val rawRotation = anglesEntry.get()

        val rotation = Rotation2d(
            -rawRotation[1].degrees.asWPIUnit
        ) + robotToQuest.rotation

        inputs.pose = Pose2d(
            Translation2d(
                rawPos[2].toDouble(),
                -rawPos[0].toDouble()
            ) + robotToQuest.translation.rotateBy(rotation),
            rotation
        ).rotateBy(headingOffset)
    }
}

class QuestIOSim(): QuestIO {
    var pose = Pose2d()
    override suspend fun reset(heading: Angle) {}

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        inputs.isConnected = true
        inputs.pose = Pose2d(-SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.x, -SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.y, SwerveDrive.simulatedDrive.actualPoseInSimulationWorld.rotation)
        pose = inputs.pose
    }
}