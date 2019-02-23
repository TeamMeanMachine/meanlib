package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.DemandType
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import org.team2471.frc.lib.math.DoubleRange
import com.ctre.phoenix.motorcontrol.can.BaseMotorController as CTREMotorController
import kotlin.math.roundToInt
import com.ctre.phoenix.motorcontrol.can.TalonSRX as CTRETalonSRX
import com.ctre.phoenix.motorcontrol.can.VictorSPX as CTREVictorSPX

sealed class MotorControllerID

/**
 * The ID of a Talon SRX motor controller.
 *
 * @param value the Talon's CAN ID
 */
data class TalonID(val value: Int) : MotorControllerID()

/**
 * The ID of a Victor SPX motor controller.
 *
 * @param value the Victor's CAN ID
 */
data class VictorID(val value: Int) : MotorControllerID()

private fun CTREMotorController(id: MotorControllerID) = when (id) {
    is TalonID -> CTRETalonSRX(id.value)
    is VictorID -> CTREVictorSPX(id.value)
}

/**
 * A single motor controller or combination of motor controllers which follow a primary device.
 *
 * @param deviceId the [MotorControllerID] of the primary, "master" motor controller
 * @param followerIds optional [MotorControllerID]s of motor controllers which should follow the primary
 */
class MotorController(deviceId: MotorControllerID, vararg followerIds: MotorControllerID) {
    private val ctreMotorController = CTREMotorController(deviceId)

    private var feedbackCoefficient = 1.0

    private var rawOffset = 0

    private val followers = followerIds.map { id ->
        val follower = CTREMotorController(id)
        follower.follow(ctreMotorController)
        follower
    }.toTypedArray()

    /**
     * The current being drawn by the [MotorController].
     * Note that this will only work if the [MotorController] is a Talon SRX. Attempts to use this method on
     * non-Talons will result in an [IllegalStateException].
     *
     * @see CTREMotorController.getMotorOutputPercent
     */
    val current: Double
        get() {
            check(ctreMotorController is CTRETalonSRX) { "Current can only be read from talons" }
            return ctreMotorController.outputCurrent
        }

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see CTREMotorController.getSelectedSensorVelocity
     */
    val velocity: Double
        get() = ctreMotorController.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0

    /**
     * The output percent, from 0 to 1.
     *
     * @see CTREMotorController.getMotorOutputPercent
     */
    val output: Double
        get() = ctreMotorController.motorOutputPercent

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see CTREMotorController.getSelectedSensorPosition
     */
    var position: Double
        get() = (ctreMotorController.getSelectedSensorPosition(0) + rawOffset) * feedbackCoefficient
        set(value) {
            ctreMotorController.selectedSensorPosition = (value / feedbackCoefficient).roundToInt()
        }

    /**
     * The raw position of the selected sensor in encoder ticks.
     *
     * @see CTREMotorController.getSelectedSensorPosition
     */
    val rawPosition: Int
        get() = ctreMotorController.getSelectedSensorPosition(0)

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double
        get() = ctreMotorController.closedLoopError * feedbackCoefficient

    init {
        allTalons {
            it.configFactoryDefault(20)
            it.setNeutralMode(NeutralMode.Coast)
        }
        ctreMotorController.selectedSensorPosition = 0

    }

    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [MotorController]
     * @see CTREMotorController.set
     */
    fun setPercentOutput(percent: Double) = ctreMotorController.set(ControlMode.PercentOutput, percent)

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop positon setpoint
     * @see CTREMotorController.set
     */
    fun setPositionSetpoint(position: Double) =
        ctreMotorController.set(ControlMode.Position, (position / feedbackCoefficient) - rawOffset)

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop positon setpoint
     * @param feedForward the closed-loop feed forward
     * @see CTREMotorController.set
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.Position, position / feedbackCoefficient,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see CTREMotorController.set
     */
    fun setVelocitySetpoint(velocity: Double) =
        ctreMotorController.set(ControlMode.Velocity, velocity / feedbackCoefficient / 10.0)

    /**
     * Sets the closed-loop velocity setpoint with a specified [feedForward] value.
     *
     * @param velocity the closed-loop velocity setpoint
     * @param feedForward the closed-loop feed forward
     * @see CTREMotorController.set
     */
    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.Velocity, velocity / feedbackCoefficient / 10.0,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop current setpoint.
     *
     * @param current the closed-loop current setpoint
     * @see CTREMotorController.set
     */
    fun setCurrentSetpoint(current: Double) = ctreMotorController.set(ControlMode.Current, current)

    /**
     * Sets the closed-loop current setpoint with a specified [feedForward] value.
     *
     * @param current the closed-loop current setpoint
     * @param feedForward the closed-loop feed forward
     * @see CTREMotorController.set
     */
    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        ctreMotorController.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see CTREMotorController.set
     */
    fun setMotionMagicSetpoint(position: Double) =
        ctreMotorController.set(ControlMode.MotionMagic, position / feedbackCoefficient)

    /**
     * Sets the closed-loop Motion Magic position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @param feedForward the closed-loop feed forward
     * @see CTREMotorController.set
     */
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        ctreMotorController.set(
            ControlMode.MotionMagic, (position / feedbackCoefficient) - rawOffset,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Neutralizes the motor output.
     *
     * @see CTREMotorController.neutralOutput
     */
    fun stop() {
        ctreMotorController.neutralOutput()
    }

    /**
     * Configures the [MotorController] with instructions specified in the [body].
     *
     * @param timeoutMs the timeout to use on various motor functions
     * @param body the function which configures this [MotorController]
     */
    inline fun config(timeoutMs: Int = Int.MAX_VALUE, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allTalons(body: (CTREMotorController) -> Unit) {
        body(ctreMotorController)
        followers.forEach(body)
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        /**
         * The primary, "master" [MotorController].
         */
        val ctreController get() = ctreMotorController

        /**
         * An array of [MotorController]s which follow [ctreController].
         */
        val ctreFollowers get() = followers

        /**
         * A coefficient applied to the attached encoder's raw value in order to convert it into a
         * desired unit of measurement. For example, if 7126 encoder ticks equals 1 foot of drive
         * distance on your drivetrain, [feedbackCoefficient] should be set to `1.0/7126.0`.
         */
        var feedbackCoefficient: Double
            get() = this@MotorController.feedbackCoefficient
            set(value) {
                this@MotorController.feedbackCoefficient = value
            }

        /**
         * Sets whether the motor should be inverted.
         *
         * @param inverted whether the motor should be inverted
         * @see CTREMotorController.setInverted
         */
        fun inverted(inverted: Boolean) = allTalons { it.inverted = inverted }

        /**
         * Enables brake mode.
         *
         * @see CTREMotorController.setNeutralMode
         */
        fun brakeMode() = allTalons { it.setNeutralMode(NeutralMode.Brake) }

        /**
         * Enables coast mode.
         *
         * @see CTREMotorController.setNeutralMode
         */
        fun coastMode() = allTalons { it.setNeutralMode(NeutralMode.Coast) }

        /**
         * Sets the phase of the sensor.
         *
         * @param inverted whether the phase of the sensor should be inverted
         * @see CTREMotorController.setSensorPhase
         */
        fun sensorPhase(inverted: Boolean) = ctreMotorController.setSensorPhase(inverted)

        /**
         * Sets the amount of time required for closed loop control of the [MotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CTREMotorController.configClosedloopRamp
         */
        fun closedLoopRamp(secondsToFull: Double) {
            ctreMotorController.configClosedloopRamp(secondsToFull)
        }

        /**
         * Sets the amount of time required for open loop control of the [MotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CTREMotorController.configOpenloopRamp
         */
        fun openLoopRamp(secondsToFull: Double) {
            ctreMotorController.configOpenloopRamp(secondsToFull)
        }

        /**
         * Sets the minimum allowable output of the [MotorController].
         *
         * @param range the range of minimum values, e.g. -0.2..0.2 would mean minimum output of 0.2
         * @see CTREMotorController.configNominalOutputReverse
         * @see CTREMotorController.configNominalOutputForward
         */
        fun nominalOutputRange(range: DoubleRange) {
            ctreMotorController.configNominalOutputReverse(range.start, timeoutMs)
            ctreMotorController.configNominalOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the maximum allowable output of the [MotorController].
         *
         * @param range the range of maximum values, e.g. -0.8..0.8 would mean maximum output of 0.8
         * @see CTREMotorController.configPeakOutputReverse
         * @see CTREMotorController.configPeakOutputForward
         */
        fun peakOutputRange(range: DoubleRange) {
            ctreMotorController.configPeakOutputReverse(range.start, timeoutMs)
            ctreMotorController.configPeakOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the [acceleration] and [cruisingVelocity] for use in Motion Magic closed loop control.
         *
         * @param acceleration the target acceleration for Motion Magic to use
         * @param cruisingVelocity the peak target velocity for Motion Magic to use
         * @see CTREMotorController.configMotionAcceleration
         * @see CTREMotorController.configMotionCruiseVelocity
         */
        fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
            val srxAcceleration = (acceleration / feedbackCoefficient / 10.0).toInt()
            val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0).toInt()
            ctreMotorController.configMotionAcceleration(srxAcceleration)
            ctreMotorController.configMotionCruiseVelocity(srxCruisingVelocity)
        }

        /**
         * Sets a raw offset, in encoder ticks, to the selected sensor.
         *
         * @param ticks the number of ticks offset to add to the selected sensor
         */
        fun rawOffset(ticks: Int) {
            rawOffset = ticks
        }

        inline fun pid(slot: Int = 0, body: PIDConfigScope.() -> Unit) = body(PIDConfigScope(slot))

        /**
         * Selects a specific PID slot.
         *
         * @see CTREMotorController.selectProfileSlot
         */
        fun pidSlot(slot: Int) = ctreMotorController.selectProfileSlot(slot, 0)

        /**
         * Limits the current to a [continuousLimit], [peakLimit] and [peakDuration].
         *
         * @param continuousLimit the continuous allowable current-draw
         * @param peakLimit the peak allowable current
         * @param peakDuration the peak allowable duration
         * @see CTRETalonSRX.configContinuousCurrentLimit
         * @see CTRETalonSRX.configPeakCurrentLimit
         * @see CTRETalonSRX.configPeakCurrentDuration
         */
        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
            // apply to following
            allTalons { controller ->
                if (controller is CTRETalonSRX) {
                    controller.configContinuousCurrentLimit(continuousLimit, timeoutMs)
                    controller.configPeakCurrentLimit(peakLimit, timeoutMs)
                    controller.configPeakCurrentDuration(peakDuration, timeoutMs)
                    controller.enableCurrentLimit(true)
                }
            }
        }

        /**
         * Sets the [FeedbackDevice] to use for closed loop and sensor feedback.
         *
         * @param feedbackDevice the [FeedbackDevice] to use
         * @see CTREMotorController.configSelectedFeedbackSensor
         */
        fun encoderType(feedbackDevice: FeedbackDevice) {
            ctreMotorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
        }

        /**
         * Sets whether the feedback of the encoder is continuous (i.e. should not wrap back to 0 after
         * a full revolution).
         *
         * @param continuous whether the encoder should be treated as continuous
         * @see CTREMotorController.configFeedbackNotContinuous
         */
        fun encoderContinuous(continuous: Boolean) {
            ctreMotorController.configFeedbackNotContinuous(!continuous, timeoutMs)
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                ctreMotorController.config_kP(slot, p / feedbackCoefficient, timeoutMs)
            }

            fun i(i: Double) {
                ctreMotorController.config_kI(slot, i / feedbackCoefficient, timeoutMs)
            }

            fun d(d: Double) {
                ctreMotorController.config_kD(slot, d / feedbackCoefficient, timeoutMs)
            }

            fun f(f: Double) {
                ctreMotorController.config_kF(slot, f / feedbackCoefficient, timeoutMs)
            }

            fun iZone(iZone: Double) {
                ctreMotorController.config_IntegralZone(slot, (iZone / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun allowableError(allowableError: Double) {
                ctreMotorController.configAllowableClosedloopError(slot, (allowableError / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                ctreMotorController.configMaxIntegralAccumulator(slot, maxIntegralAccumulator / feedbackCoefficient, timeoutMs)
            }

            fun closedLoopPeakOutput(closedLoopPeakOutput: Double) {
                ctreMotorController.configClosedLoopPeakOutput(slot, closedLoopPeakOutput, timeoutMs)
            }

            fun closedLoopPeriod(closedLoopPeriod: Int) {
                ctreMotorController.configClosedLoopPeriod(slot, closedLoopPeriod, timeoutMs)
            }
        }
    }
}
