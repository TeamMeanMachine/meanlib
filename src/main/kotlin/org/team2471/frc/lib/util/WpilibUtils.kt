package org.team2471.frc.lib.util

import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.geometry.Rotation2d
import org.wpilib.math.geometry.Transform2d
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.math.kinematics.SwerveDriveKinematics
import org.wpilib.math.kinematics.SwerveModuleVelocity
import org.wpilib.units.measure.Angle


inline val ChassisVelocities.translation: Translation2d get() = Translation2d(this.vx, this.vy)
fun ChassisVelocities.toTransform2d(deltaSeconds: Double): Transform2d = Transform2d(this.translation, Rotation2d(this.omega)) * deltaSeconds
fun SwerveDriveKinematics.toChassisVelocitiesK(speeds: Array<SwerveModuleVelocity>): ChassisVelocities {
    return this.toChassisVelocities(*speeds) //Intellij sometimes thinks this is an error. which is lame...
}

fun Pose2d.changeRotation(newRotation: Rotation2d): Pose2d {
    return Pose2d(this.translation, newRotation)
}
fun Pose2d.addRotation(rotation: Rotation2d): Pose2d {
    return Pose2d(this.translation, this.rotation.plus(rotation))
}

fun Translation2d.angleTo(other: Translation2d): Angle {
    return (other - this).angle.measure
}
