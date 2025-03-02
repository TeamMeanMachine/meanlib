package org.team2471.frc.lib.vision.quest

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.networktables.StructPublisher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asVector2
import org.team2471.frc.lib.math.meters
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.isReal
import org.team2471.frc.lib.util.isSim

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
            return Pose2d(inputs.pose.translation + field.translation, inputs.pose.rotation + field.rotation)
        }
        set(value) { field += value - pose}


    val isConnected: Boolean
        get() = inputs.isConnected

    suspend fun reset() {
        io.reset()
    }


    init {
        GlobalScope.launch {
            io.reset()
            periodic {
                io.updateInputs(inputs)
                posePublisher.set(pose)
                isConnectedEntry.setBoolean(inputs.isConnected)
            }
        }
    }
}