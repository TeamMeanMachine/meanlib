package org.team2471.frc.lib.util

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Transform2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.math.kinematics.SwerveModuleState

inline val ChassisSpeeds.translation: Translation2d get() = Translation2d(this.vxMetersPerSecond, this.vyMetersPerSecond)
fun ChassisSpeeds.fieldToRobotCentric(heading: Rotation2d): ChassisSpeeds = ChassisSpeeds.fromFieldRelativeSpeeds(this, heading)
fun ChassisSpeeds.robotToFieldCentric(heading: Rotation2d): ChassisSpeeds = ChassisSpeeds.fromRobotRelativeSpeeds(this, heading)
fun ChassisSpeeds.toTransform2d(deltaSeconds: Double): Transform2d = Transform2d(this.translation, Rotation2d(this.omegaRadiansPerSecond)) * deltaSeconds
fun SwerveDriveKinematics.toChassisSpeedsK(speeds: Array<SwerveModuleState>): ChassisSpeeds {
    return this.toChassisSpeeds(*speeds) //Intellij sometimes thinks this is an error. which is lame...
}

fun Pose2d.changeRotation(newRotation: Rotation2d): Pose2d {
    return Pose2d(this.translation, newRotation)
}
fun Pose2d.addRotation(rotation: Rotation2d): Pose2d {
    return Pose2d(this.translation, this.rotation.plus(rotation))
}
