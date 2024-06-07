package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.system.plant.DCMotor
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.AngularVelocity
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.perSecond
import org.team9432.lib.advantagekit.kGet
import org.team9432.lib.advantagekit.kPut

interface MotorControllerIO {
    val current: Double
    val outputPercent: Double

    open class MotorControllerIOInputs(val name: String): LoggableInputs {
        var position: Angle = 0.0.degrees
        var current: Double = 0.0
        var outputPercent: Double = 0.0
        var velocity: AngularVelocity = 0.0.degrees.perSecond

        override fun toLog(table: LogTable) {
            table.kPut("Motors/$name/Position", position.asDegrees)
            table.kPut("Motors/$name/Current", current)
            table.kPut("Motors/$name/OutputPercent", outputPercent)
            table.kPut("Motors/$name/Velocity", velocity.changePerSecond.asDegrees)
        }

        override fun fromLog(table: LogTable) {
            table.kGet("Motors/$name/Position", position.asDegrees)
            table.kGet("Motors/$name/Current", current)
            table.kGet("Motors/$name/OutputPercent", outputPercent)
            table.kGet("Motors/$name/Velocity", velocity.changePerSecond.asDegrees)
        }
    }

    fun updateInputs(inputs: MotorControllerIOInputs)

    fun brakeMode()
    fun burnFlash() {}
    fun closedLoopRamp(secondsToFull: Double)
    fun coastMode()
    fun config_kP(p: Double)
    fun config_kD(d: Double)
    fun config_kI(i: Double)
    fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int)
    fun encoderContinuous(continuous: Boolean) {}
    fun follow(followerID: MotorControllerIO)
    fun getClosedLoopError(): Double
    fun getPValue(): Double
    fun getDValue() : Double
    fun getIValue(): Double
    fun getInverted(): Boolean
    fun getSelectedSensorPosition(): Double
    fun getSelectedSensorVelocity(): Double
    fun motionMagic(acceleration: Double, cruisingVelocity: Double) {}
    fun openLoopRamp(secondsToFull: Double)
    fun peakOutputRange(range: DoubleRange) {}
    fun restoreFactoryDefaults()
    fun setInverted(invert: Boolean)
    fun setMotionMagicSetpoint(position: Double) {}
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) {}
    fun setSimMotor(motor: DCMotor) {}
    fun setSimMOI(jKgMetersSquared: Double) {}
    fun setSimFeedbackCoefficient(feedbackCoefficient: Double) {}
    fun setNeutralMode(neutralMode: NeutralModeValue?)
    fun setPercentOutput(percent: Double)
    fun setPositionSetpoint(position: Double)
    fun setPositionSetpoint(position: Double, feedForward: Double)
    fun setSelectedSensorPosition(sensorPos: Double)
    fun setStatusFramePeriod(periodMs: Int, timeoutSec: Double = 0.05) {}
    fun setVelocitySetpoint(velocity: Double)
    fun setVelocitySetpoint(velocity: Double, feedForward: Double)
    fun stop()
}