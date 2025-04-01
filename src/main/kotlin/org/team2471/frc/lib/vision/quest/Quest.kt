package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
import org.team2471.frc.lib.math.Vector2
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asMeters
import org.team2471.frc.lib.math.toTranslation2d
import org.team2471.frc.lib.units.*

@OptIn(DelicateCoroutinesApi::class)
class Quest(
    val robotToQuest: Transform2d,
    val isSim: Boolean,
    val outTable: NetworkTable = NetworkTableInstance.getDefault().getTable("questnav")
){
    private val posePublisher = outTable.getStructTopic("Quest Position", Pose2d.struct).publish()
    private val isConnectedEntry = outTable.getEntry("isConnected")

    private val io = if (isSim) QuestIOSim() else QuestIOReal(robotToQuest)
    private val inputs = QuestIO.QuestIOInputs()

    var pose: Pose2d
        get() = inputs.pose
        set(value) {
            io.setPosition(value.translation)
        }



    val isConnected: Boolean
        get() = inputs.isConnected

    fun resetHeading(heading: Angle) {
        io.setHeading(heading)
    }

    fun resetPosition(position: Vector2L) {
        io.setPosition(-(position.asMeters.toTranslation2d().rotateBy(-pose.rotation)))
    }

    fun resetPosition(position: Translation2d) {
        io.setPosition(-(position.rotateBy(-pose.rotation)))
    }

    init {
        GlobalScope.launch {
            io.setHeading(0.0.degrees)
            periodic {
                io.updateInputs(inputs)
                MeanLogger.processInputs("", inputs)


                MeanLogger.recordOutput("Quest/Pose", pose)
                posePublisher.set(pose)
                isConnectedEntry.setBoolean(inputs.isConnected)
            }
        }
    }
}