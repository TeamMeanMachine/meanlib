package org.team2471.frc.lib.vision

import edu.wpi.first.math.geometry.*
import edu.wpi.first.networktables.NetworkTable
import edu.wpi.first.wpilibj.Timer
import org.photonvision.PhotonCamera
import org.photonvision.PhotonPoseEstimator
import org.team2471.frc.lib.math.Vector2L
import org.team2471.frc.lib.math.asMeters
import org.team2471.frc.lib.math.setAdvantagePose
import org.team2471.frc.lib.math.setAdvantagePoses
import org.team2471.frc.lib.motion_profiling.MotionCurve
import org.team2471.frc.lib.units.*
import kotlin.math.abs
import kotlin.math.pow

abstract class GenericCamera(val networkTable: NetworkTable, val name: String) {


    val advantagePoseEntry = networkTable.getEntry("April Advantage Pos $name")
    val targetPoseEntry = networkTable.getEntry("April Target Pos $name")
    val stDevEntry = networkTable.getEntry("stDev $name")
    val stDevMultiplierEntry = networkTable.getEntry("stDev Multiplier $name")
    val isConnectedEntry = networkTable.getEntry("isConnected $name")

    abstract val isConnected: Boolean

    open val photonCam: PhotonCamera? = null

    var lastGlobalPose: GlobalPose? = null

    abstract fun reset()

    abstract fun getEstimatedGlobalPose(): GlobalPose?
}