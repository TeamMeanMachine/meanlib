package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.geometry.Transform3d
import edu.wpi.first.math.geometry.Translation3d
import edu.wpi.first.networktables.NetworkTableInstance
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.coroutines.suspendUntil
import org.team2471.frc.lib.motion.following.SwerveDrive
import org.team2471.frc.lib.units.asWPIUnit
import org.team2471.frc.lib.units.radians

interface QuestIO {
    class QuestIOInputs : LoggableInputs {

        var isConnected: Boolean = false

        var pose: Pose3d = Pose3d()

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

    suspend fun reset()
    fun updateInputs(inputs: QuestIOInputs)
}

class QuestIOReal(val robotToQuest: Transform3d): QuestIO {
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

    val timestamp: Double
        get() = timestampEntry.get()

    private var prevTimestamp = 0.0

    // Only call if quest is connected
    override suspend fun reset() {
        if (questMisoEntry.get() != 99L) {
            questMosiEntry.set(1)
        }
        suspendUntil { questMisoEntry.get() == 99L }
        questMosiEntry.set(0)
    }

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        val ts = timestamp
        inputs.isConnected = ts != prevTimestamp
        prevTimestamp = ts
        isConnectedEntry.setBoolean(inputs.isConnected)

        val rawPos = positionsEntry.get()
        val rawRotation = anglesEntry.get()

        val rotation = Rotation3d(
            rawRotation[2].radians.asWPIUnit,
            rawRotation[0].radians.asWPIUnit,
            rawRotation[1].radians.asWPIUnit
        )

        inputs.pose = Pose3d(
            Translation3d(
                rawPos[2].toDouble(),
                -rawPos[0].toDouble(),
                rawPos[1].toDouble()
            ) - robotToQuest.translation.rotateBy(rotation),
            rotation
        )
    }
}

class QuestIOSim(): QuestIO {
    override suspend fun reset() {}

    override fun updateInputs(inputs: QuestIO.QuestIOInputs) {
        inputs.isConnected = true
        inputs.pose = Pose3d(SwerveDrive.Companion.simulatedDrive.actualPoseInSimulationWorld)
    }
}