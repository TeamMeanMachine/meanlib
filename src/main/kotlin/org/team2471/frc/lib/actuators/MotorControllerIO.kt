package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.signals.NeutralModeValue
import edu.wpi.first.math.system.plant.DCMotor
import org.littletonrobotics.junction.LogTable
import org.littletonrobotics.junction.inputs.LoggableInputs
import org.team2471.frc.lib.math.DoubleRange
import org.team9432.lib.advantagekit.kGet
import org.team9432.lib.advantagekit.kPut

interface MotorControllerIO {
    val current: Double
    val outputPercent: Double

    open class MotorControllerIOInputs(val name: String): LoggableInputs {
        var position: Double = 0.0
        var current: Double = 0.0
        var outputPercent: Double = 0.0
        var velocity: Double = 0.0

        override fun toLog(table: LogTable) {
            table.kPut("$name/Position", position)
            table.kPut("$name/Current", current)
            table.kPut("$name/OutputPercent", outputPercent)
            table.kPut("$name/Velocity", velocity)
        }

        override fun fromLog(table: LogTable) {
            table.kGet("$name/Position", position)
            table.kGet("$name/Current", current)
            table.kGet("$name/OutputPercent", outputPercent)
            table.kGet("$name/Velocity", velocity)
        }
    }

    fun updateInputs(inputs: MotorControllerIOInputs)

    fun brakeMode()
    fun burnFlash() {}
    fun closedLoopRamp(secondsToFull: Double)
    fun coastMode()
    fun config_kP(p: Double, simP: Double? = 0.0)
    fun config_kD(d: Double, simD: Double? = 0.0)
    fun config_kI(i: Double, simI: Double? = 0.0)
    fun configSim(motor: DCMotor, jKgMetersSquared: Double) {}
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