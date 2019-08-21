package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.*
import org.team2471.frc.lib.math.DoubleRange
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


/**
 * The ID of a Spark MAX motor controller.
 *
 * @param value the SparkMax's CAN ID
 */
data class SparkMaxID(val value: Int) : MotorControllerID()



private fun MotorController(id: MotorControllerID) = when (id) {
    is TalonID -> CTRETalonSRX(id.value)
    is VictorID -> CTREVictorSPX(id.value)
    is SparkMaxID -> SparkMaxWrapper(id.value)
}


/**
 * A single motor controller or combination of motor controllers which follow a primary device.
 *
 * @param deviceId the [MotorControllerID] of the primary, "master" motor controller
 * @param followerIds optional [MotorControllerID]s of motor controllers which should follow the primary
 */
class MotorController(deviceId: MotorControllerID, vararg followerIds: MotorControllerID) {
    private val iMotorController = MotorController(deviceId)

    private var feedbackCoefficient = 1.0

    private var rawOffset = 0

    private val followers = followerIds.map { id ->
        val follower = MotorController(id)
        follower.follow(iMotorController)
        follower
    }.toTypedArray()

    /**
     * The selected PID slot.
     */
    var pidSlot: Int = 0
        set(value) {
            iMotorController.selectProfileSlot(value, 0)
            field = value
        }

    /**
     * The current being drawn by the [MotorController].
     * Note that this will only work if the [MotorController] is a Talon SRX. Attempts to use this method on
     * non-Talons will result in an [IllegalStateException].
     *
     * @see MotorController.getMotorOutputPercent
     */
    val current: Double
        get() {
            check(iMotorController is CTRETalonSRX) { "Current can only be read from talons" }
            return iMotorController.outputCurrent
        }

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see MotorController.getSelectedSensorVelocity
     */
    val velocity: Double
        get() = iMotorController.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0

    /**
     * The output percent, from 0 to 1.
     *
     * @see MotorController.getMotorOutputPercent
     */
    val output: Double
        get() = iMotorController.motorOutputPercent

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see MotorController.getSelectedSensorPosition
     */
    var position: Double
        get() = (iMotorController.getSelectedSensorPosition(0) + rawOffset) * feedbackCoefficient
        set(value) {
            iMotorController.setSelectedSensorPosition((value / feedbackCoefficient).roundToInt(), 0, 0)
        }

    /**
     * The raw position of the selected sensor in encoder ticks.
     *
     * @see MotorController.getSelectedSensorPosition
     */
    val rawPosition: Int
        get() = iMotorController.getSelectedSensorPosition(0)

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double
        get() {
            return iMotorController.getClosedLoopError(0) * feedbackCoefficient
        }


    init {
        allMotorControllers {
            when (iMotorController) {
                is CTRETalonSRX -> {
                    val ctre = it as CTRETalonSRX
                    ctre.configFactoryDefault()
                }
                is CTREVictorSPX -> {
                    val ctre = it as CTREVictorSPX
                    ctre.configFactoryDefault()
                }
                is SparkMaxWrapper -> {
                    val sparkMax = it as SparkMaxWrapper
                    sparkMax.restoreFactoryDefaults()
                }
            }
            it.setNeutralMode(NeutralMode.Coast)
        }
        iMotorController.setSelectedSensorPosition(0, 0, 0)
    }

    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [MotorController]
     * @see MotorController.set
     */
    fun setPercentOutput(percent: Double) = iMotorController.set(ControlMode.PercentOutput, percent)

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop positon setpoint
     * @see MotorController.set
     */
    fun setPositionSetpoint(position: Double) =
        iMotorController.set(ControlMode.Position, (position / feedbackCoefficient) - rawOffset)

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop positon setpoint
     * @param feedForward the closed-loop feed forward
     * @see MotorController.set
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) =
        iMotorController.set(
            ControlMode.Position, position / feedbackCoefficient - rawOffset,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see MotorController.set
     */
    fun setVelocitySetpoint(velocity: Double) =
        iMotorController.set(ControlMode.Velocity, velocity / feedbackCoefficient / 10.0)

    /**
     * Sets the closed-loop velocity setpoint with a specified [feedForward] value.
     *
     * @param velocity the closed-loop velocity setpoint
     * @param feedForward the closed-loop feed forward
     * @see MotorController.set
     */
    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        iMotorController.set(
            ControlMode.Velocity, velocity / feedbackCoefficient / 10.0,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop current setpoint.
     *
     * @param current the closed-loop current setpoint
     * @see MotorController.set
     */
    fun setCurrentSetpoint(current: Double) = iMotorController.set(ControlMode.Current, current)

    /**
     * Sets the closed-loop current setpoint with a specified [feedForward] value.
     *
     * @param current the closed-loop current setpoint
     * @param feedForward the closed-loop feed forward
     * @see MotorController.set
     */
    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        iMotorController.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see MotorController.set
     */
    fun setMotionMagicSetpoint(position: Double) =
        iMotorController.set(ControlMode.MotionMagic, position / feedbackCoefficient - rawOffset)

    /**
     * Sets the closed-loop Motion Magic position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @param feedForward the closed-loop feed forward
     * @see MotorController.set
     */
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        iMotorController.set(
            ControlMode.MotionMagic, (position / feedbackCoefficient) - rawOffset,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Neutralizes the motor output.
     *
     * @see MotorController.neutralOutput
     */
    fun stop() {
        iMotorController.neutralOutput()
    }

    /**
     * Configures the [MotorController] with instructions specified in the [body].
     *
     * @param timeoutMs the timeout to use on various motor functions
     * @param body the function which configures this [MotorController]
     */
    inline fun config(timeoutMs: Int = 100, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allMotorControllers(body: (IMotorController) -> Unit) {
        body(iMotorController)
        followers.forEach(body)
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        /**
         * The primary, "master" [MotorController].
         */
        val ctreController get() = iMotorController

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
         * @see MotorController.setInverted
         */
        fun inverted(inverted: Boolean) = allMotorControllers { it.inverted = inverted }

        /**
         * Enables brake mode.
         *
         * @see MotorController.setNeutralMode
         */
        fun brakeMode() = allMotorControllers { it.setNeutralMode(NeutralMode.Brake) }

        /**
         * Enables coast mode.
         *
         * @see MotorController.setNeutralMode
         */
        fun coastMode() = allMotorControllers { it.setNeutralMode(NeutralMode.Coast) }

        /**
         * Sets the phase of the sensor.
         *
         * @param inverted whether the phase of the sensor should be inverted
         * @see MotorController.setSensorPhase
         */
        fun sensorPhase(inverted: Boolean) = iMotorController.setSensorPhase(inverted)

        /**
         * Sets the amount of time required for closed loop control of the [MotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see MotorController.configClosedloopRamp
         */
        fun closedLoopRamp(secondsToFull: Double) {
            iMotorController.configClosedloopRamp(secondsToFull, 20)
        }

        /**
         * Sets the amount of time required for open loop control of the [MotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see MotorController.configOpenloopRamp
         */
        fun openLoopRamp(secondsToFull: Double) {
            iMotorController.configOpenloopRamp(secondsToFull, 20)
        }

        /**
         * Sets the minimum allowable output of the [MotorController].
         *
         * @param range the range of minimum values, e.g. -0.2..0.2 would mean minimum output of 0.2
         * @see MotorController.configNominalOutputReverse
         * @see MotorController.configNominalOutputForward
         */
        fun nominalOutputRange(range: DoubleRange) {
            iMotorController.configNominalOutputReverse(range.start, timeoutMs)
            iMotorController.configNominalOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the maximum allowable output of the [MotorController].
         *
         * @param range the range of maximum values, e.g. -0.8..0.8 would mean maximum output of 0.8
         * @see MotorController.configPeakOutputReverse
         * @see MotorController.configPeakOutputForward
         */
        fun peakOutputRange(range: DoubleRange) {
            iMotorController.configPeakOutputReverse(range.start, timeoutMs)
            iMotorController.configPeakOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the [acceleration] and [cruisingVelocity] for use in Motion Magic closed loop control.
         *
         * @param acceleration the target acceleration for Motion Magic to use
         * @param cruisingVelocity the peak target velocity for Motion Magic to use
         * @see MotorController.configMotionAcceleration
         * @see MotorController.configMotionCruiseVelocity
         */
        fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
            val srxAcceleration = (acceleration / feedbackCoefficient / 10.0).toInt()
            val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0).toInt()
            iMotorController.configMotionAcceleration(srxAcceleration, 20)
            iMotorController.configMotionCruiseVelocity(srxCruisingVelocity, 20)
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
         * @see MotorController.selectProfileSlot
         */
        fun pidSlot(slot: Int) = iMotorController.selectProfileSlot(slot, 0)

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
            allMotorControllers { controller ->
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
         * @see MotorController.configSelectedFeedbackSensor
         */
        fun encoderType(feedbackDevice: RemoteFeedbackDevice) {
            iMotorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
        }

        /**
         * Sets whether the feedback of the encoder is continuous (i.e. should not wrap back to 0 after
         * a full revolution).
         *
         * @param continuous whether the encoder should be treated as continuous
         * @see MotorController.configFeedbackNotContinuous
         */
        fun encoderContinuous(continuous: Boolean) {
            // iMotorController.configFeedbackNotContinuous(!continuous, timeoutMs)  //TODO: figure
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                iMotorController.config_kP(slot, p / feedbackCoefficient, timeoutMs)
            }

            fun i(i: Double) {
                iMotorController.config_kI(slot, i / feedbackCoefficient, timeoutMs)
            }

            fun d(d: Double) {
                iMotorController.config_kD(slot, d / feedbackCoefficient, timeoutMs)
            }

            fun f(f: Double) {
                iMotorController.config_kF(slot, f / feedbackCoefficient / 10.0, timeoutMs)
            }

            fun iZone(iZone: Double) {
                iMotorController.config_IntegralZone(slot, (iZone / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun allowableError(allowableError: Double) {
                iMotorController.configAllowableClosedloopError(slot, (allowableError / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                iMotorController.configMaxIntegralAccumulator(slot, maxIntegralAccumulator / feedbackCoefficient, timeoutMs)
            }

            fun closedLoopPeakOutput(closedLoopPeakOutput: Double) {
                iMotorController.configClosedLoopPeakOutput(slot, closedLoopPeakOutput, timeoutMs)
            }

            fun closedLoopPeriod(closedLoopPeriod: Int) {
                iMotorController.configClosedLoopPeriod(slot, closedLoopPeriod, timeoutMs)
            }
        }
    }
}
