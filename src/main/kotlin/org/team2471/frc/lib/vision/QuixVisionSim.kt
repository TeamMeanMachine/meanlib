package org.team2471.frc.lib.vision

import org.team2471.frc.lib.util.isReal
import org.team2471.frc.lib.vision.photonVision.PhotonVisionCamera
import org.photonvision.estimation.TargetModel
import org.photonvision.simulation.VisionSystemSim
import org.photonvision.simulation.VisionTargetSim
import org.wpilib.math.geometry.Pose2d
import org.wpilib.smartdashboard.Field2d

object QuixVisionSim {
//    private val m_visionSim = VisionSystemSim("main") // TODO: FIX 2027

    fun addCamera(camera: PhotonVisionCamera) {
//        m_visionSim.addCamera(camera.cameraSim, camera.transform) //TODO: UNCOMMENT WHEN PHOTONVISION UPDATES TO 2027
//        println("vision sim has ${m_visionSim.cameraSims.size} cameras")
    }

    fun resetSimPose(pose: Pose2d) {
//        m_visionSim.resetRobotPose(pose) //TODO: UNCOMMENT WHEN PHOTONVISION UPDATES TO 2027
    }

    fun updatePose(pose: Pose2d) {
//        m_visionSim.update(pose) //TODO: UNCOMMENT WHEN PHOTONVISION UPDATES TO 2027
    }

    fun setTargets(tags: Array<Fiducial>) {
        if (!isReal) {
            tags.forEach {
//                m_visionSim.addVisionTargets("apriltag", VisionTargetSim(it.pose, TargetModel.kAprilTag36h11, it.id)) //TODO: UNCOMMENT WHEN PHOTONVISION UPDATES TO 2027
            }
        }
    }

    val simField: Field2d
        get() = Field2d()//m_visionSim.debugField //TODO: UNCOMMENT WHEN PHOTONVISION UPDATES TO 2027
}
