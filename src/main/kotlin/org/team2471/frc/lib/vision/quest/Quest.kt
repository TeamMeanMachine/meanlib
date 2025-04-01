package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLogger
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

    var pose: Pose2d = Pose2d()
        get() {
            // negatives bc quest is upside down. change later
            return Pose2d(inputs.pose.translation + field.translation, inputs.pose.rotation + field.rotation)
        }
        set(value) { field = Pose2d(value.translation + inputs.pose.translation, Rotation2d()) }


    val isConnected: Boolean
        get() = inputs.isConnected

    fun resetHeading(heading: Angle) {
        io.resetHeading(heading)
    }

    init {
        GlobalScope.launch {
            io.resetHeading(0.0.degrees)
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