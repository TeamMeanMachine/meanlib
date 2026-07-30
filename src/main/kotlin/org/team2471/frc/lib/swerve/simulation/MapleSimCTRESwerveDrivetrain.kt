package org.team2471.frc.lib.swerve.simulation

import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.hardware.TalonFXS
import com.ctre.phoenix6.sim.*
import com.ctre.phoenix6.swerve.SwerveModule
import com.ctre.phoenix6.swerve.SwerveModuleConstants
import com.ctre.phoenix6.swerve.SwerveModuleConstants.DriveMotorArrangement
import com.ctre.phoenix6.swerve.SwerveModuleConstants.SteerMotorArrangement
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.system.plant.DCMotor
import edu.wpi.first.math.util.Units
import edu.wpi.first.units.measure.Distance
import edu.wpi.first.units.measure.Mass
import edu.wpi.first.units.measure.Voltage
import org.ironmaple.simulation.SimulatedArena
import org.ironmaple.simulation.drivesims.COTS
import org.ironmaple.simulation.drivesims.SelfControlledSwerveDriveSimulation
import org.ironmaple.simulation.drivesims.SwerveDriveSimulation
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig
import org.ironmaple.simulation.drivesims.configs.SwerveModuleSimulationConfig
import org.team2471.frc.lib.units.*
import kotlin.math.roundToInt

/**
 *  CTRE simulated swerve drive using MapleSim
 *
 *  Uses MapleSim as the physics engine and applies velocity and position to the CTRE motors.
 *
 *  @see SelfControlledSwerveDriveSimulation
 *  @see com.ctre.phoenix6.swerve.SimSwerveDrivetrain
 */
class MapleSimCTRESwerveDrivetrain(robotMass: Mass, bumperLengthX: Distance, bumperWidthY: Distance, initialPoseOnField: Pose2d, val pigeon2Sim: Pigeon2SimState, vararg val moduleConstants: SwerveModuleConstants<*, *, *>) :
    SelfControlledSwerveDriveSimulation(
        SwerveDriveSimulation(
            DriveTrainSimulationConfig(
                robotMass,
                bumperLengthX,
                bumperWidthY,
                moduleConstants.first().LocationX.meters,
                moduleConstants.first().LocationY.meters,
                COTS.ofPigeon2(),
                SwerveModuleSimulationConfig(
                    when (moduleConstants.first().DriveMotorType) {
                        DriveMotorArrangement.TalonFX_Integrated -> DCMotor.getKrakenX60Foc(1)
                        DriveMotorArrangement.TalonFXS_NEO_JST -> DCMotor.getNEO(1)
                        DriveMotorArrangement.TalonFXS_VORTEX_JST -> DCMotor.getNeoVortex(1)
                    },
                    when (moduleConstants.first().SteerMotorType) {
                        SteerMotorArrangement.TalonFX_Integrated -> DCMotor.getKrakenX44Foc(1)
                        SteerMotorArrangement.TalonFXS_Minion_JST -> DCMotor.getMinion(1)
                        SteerMotorArrangement.TalonFXS_NEO_JST -> DCMotor.getNEO(1)
                        SteerMotorArrangement.TalonFXS_VORTEX_JST -> DCMotor.getNeoVortex(1)
                        SteerMotorArrangement.TalonFXS_NEO550_JST -> DCMotor.getNeo550(1)
                        SteerMotorArrangement.TalonFXS_Brushed_AB, SteerMotorArrangement.TalonFXS_Brushed_AC, SteerMotorArrangement.TalonFXS_Brushed_BC -> DCMotor.getCIM(
                            1
                        )
                    },
                    moduleConstants.first().DriveMotorGearRatio,
                    moduleConstants.first().SteerMotorGearRatio,
                    moduleConstants.first().DriveFrictionVoltage.volts,
                    moduleConstants.first().SteerFrictionVoltage.volts,
                    moduleConstants.first().WheelRadius.meters,
                    moduleConstants.first().SteerInertia.kilogramSquareMeters,
                    1.2
                )
            ),
            initialPoseOnField
        )
    ) {

    init {
        SimulatedArena.getInstance().addDriveTrainSimulation(this.driveTrainSimulation)
    }

    fun updateCTRE(dtSeconds: Double, supplyVoltage: Voltage, vararg modulesToApply: SwerveModule<*, *, *>) {
        this.runSwerveStates(modulesToApply.map { it.targetState }.toTypedArray())
        SimulatedArena.overrideSimulationTimings(dtSeconds.seconds, ((dtSeconds / 0.005).roundToInt() + 1).coerceAtLeast(1)) // MapleSim recommends simulating at +200hz, adds iterations if dt is less than 200hz
        SimulatedArena.getInstance().simulationPeriodic()

        if (modulesToApply.size != moduleConstants.size) return

        /* Update our sim devices */
        for (i in moduleConstants.indices) {
            val driveMotor = modulesToApply[i].getDriveMotor()
            if (driveMotor is TalonFX) {
                val driveMotorSim: TalonFXSimState = driveMotor.getSimState()
                driveMotorSim.Orientation = if (moduleConstants[i].DriveMotorInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive

                driveMotorSim.setSupplyVoltage(supplyVoltage)
                driveMotorSim.setRawRotorPosition(this.driveTrainSimulation.modules[i].driveWheelFinalPosition * moduleConstants[i].DriveMotorGearRatio)
                driveMotorSim.setRotorVelocity(this.driveTrainSimulation.modules[i].driveWheelFinalSpeed * moduleConstants[i].DriveMotorGearRatio)
            } else if (driveMotor is TalonFXS) {
                val driveMotorSim: TalonFXSSimState = driveMotor.getSimState()
                driveMotorSim.MotorOrientation = if (moduleConstants[i].DriveMotorInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive

                driveMotorSim.setSupplyVoltage(supplyVoltage)
                driveMotorSim.setRawRotorPosition(this.driveTrainSimulation.modules[i].driveWheelFinalPosition * moduleConstants[i].DriveMotorGearRatio)
                driveMotorSim.setRotorVelocity(this.driveTrainSimulation.modules[i].driveWheelFinalSpeed * moduleConstants[i].DriveMotorGearRatio)
            }

            val steerMotor = modulesToApply[i].getSteerMotor()
            if (steerMotor is TalonFX) {
                val steerMotorSim: TalonFXSimState = steerMotor.getSimState()
                steerMotorSim.Orientation = if (moduleConstants[i].SteerMotorInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive

                steerMotorSim.setSupplyVoltage(supplyVoltage)

                steerMotorSim.setRawRotorPosition(this.driveTrainSimulation.modules[i].steerAbsoluteAngle * moduleConstants[i].SteerMotorGearRatio)
                steerMotorSim.setRotorVelocity(this.driveTrainSimulation.modules[i].steerAbsoluteEncoderSpeed * moduleConstants[i].SteerMotorGearRatio)
            } else if (steerMotor is TalonFXS) {
                val steerMotorSim: TalonFXSSimState = steerMotor.getSimState()
                steerMotorSim.MotorOrientation = if (moduleConstants[i].SteerMotorInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive
                steerMotorSim.ExtSensorOrientation = if (moduleConstants[i].SteerMotorInverted != moduleConstants[i].EncoderInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive
                steerMotorSim.PulseWidthSensorOffset = moduleConstants[i].EncoderOffset

                steerMotorSim.setSupplyVoltage(supplyVoltage)
                steerMotorSim.setRawRotorPosition(this.driveTrainSimulation.modules[i].steerAbsoluteAngle * moduleConstants[i].SteerMotorGearRatio)
                steerMotorSim.setRotorVelocity(this.driveTrainSimulation.modules[i].steerAbsoluteEncoderSpeed * moduleConstants[i].SteerMotorGearRatio)
                /* azimuth encoders see the mechanism, so don't account for the steer gearing */
                steerMotorSim.setPulseWidthPosition(this.driveTrainSimulation.modules[i].steerAbsoluteAngle)
                steerMotorSim.setPulseWidthVelocity(this.driveTrainSimulation.modules[i].steerAbsoluteEncoderSpeed)
            }

            val encoder = modulesToApply[i].getEncoder()
            if (encoder is CANcoder) {
                val encoderSim: CANcoderSimState = encoder.getSimState()
                encoderSim.Orientation = if (moduleConstants[i].EncoderInverted) ChassisReference.Clockwise_Positive else ChassisReference.CounterClockwise_Positive
                encoderSim.SensorOffset = moduleConstants[i].EncoderOffset

                encoderSim.setSupplyVoltage(supplyVoltage)

                /* azimuth encoders see the mechanism, so don't account for the steer gearing */
                encoderSim.setRawPosition(this.driveTrainSimulation.modules[i].steerAbsoluteAngle)
                encoderSim.setVelocity(this.driveTrainSimulation.modules[i].steerAbsoluteEncoderSpeed)
            }
        }

        val angularVelRadPerSec: Double = this.actualSpeedsRobotRelative.omegaRadiansPerSecond
        pigeon2Sim.setRawYaw(this.actualPoseInSimulationWorld.rotation.measure)
        pigeon2Sim.setAngularVelocityZ(Units.radiansToDegrees(angularVelRadPerSec))
    }
}