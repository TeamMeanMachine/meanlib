package org.team2471.frc.lib.actuators

import com.ctre.phoenix.motorcontrol.*
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.Angle
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

private fun internalMotorController(id: MotorControllerID) = when (id) {
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
    private val motorController = internalMotorController(deviceId)

    private var feedbackCoefficient = 1.0
    private var rawOffset = 0

    private val followers = followerIds.map { id ->
        val follower = internalMotorController(id)
        follower.follow(motorController)
        follower
    }.toTypedArray()

    /**
     * The selected PID slot.
     */
    var pidSlot: Int = 0
        set(value) {
            motorController.selectProfileSlot(value, 0)
            field = value
        }

    /**
     * The current being drawn by this [MotorController].
     * Note that this will only work if the [MotorController] is a Talon SRX or Spark Max. Attempts
     * to use this method on any other motor controller will result in an [IllegalStateException].
     */
    val current: Double
        get() = when (motorController) {
            is CTRETalonSRX -> motorController.outputCurrent
            is SparkMaxWrapper -> motorController.current
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see internalMotorController.getSelectedSensorVelocity
     */
    val velocity: Double
        get() = motorController.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0

    /**
     * The output percent, from 0 to 1.
     *
     * @see internalMotorController.getMotorOutputPercent
     */
    val output: Double
        get() = motorController.motorOutputPercent

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see internalMotorController.getSelectedSensorPosition
     */
    var position: Double
        get() = (motorController.getSelectedSensorPosition(0) + rawOffset) * feedbackCoefficient
        set(value) {
            motorController.setSelectedSensorPosition((value / feedbackCoefficient).roundToInt(), 0, 0)
        }

    var angle: Double
        get() = position* 360.0/3.036 - 15.65
        set(value) {}

    var analogPosition: Double
        get() = when (motorController) {
            is SparkMaxWrapper -> motorController.analogPosition
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }
        set(value) {}

    var analogAngle: Double
        get() = when(motorController) {
            is SparkMaxWrapper -> motorController.analogAngle
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }
        set(value) {}

    /**
     * The raw position of the selected sensor in encoder ticks.
     *
     * @see internalMotorController.getSelectedSensorPosition
     */
    val rawPosition: Int
        get() = motorController.getSelectedSensorPosition(0)

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double
        get() = motorController.getClosedLoopError(0) * feedbackCoefficient

    init {
        allMotorControllers {
            when (motorController) {
                is CTRETalonSRX -> motorController.configFactoryDefault()
                is CTREVictorSPX -> motorController.configFactoryDefault()
                is SparkMaxWrapper -> {
                    /*motorController.restoreFactoryDefaults()*/
                }
            }

            it.setNeutralMode(NeutralMode.Coast)
        }

        motorController.setSelectedSensorPosition(0, 0, 0)
    }

    fun hasfaults() {
        //return motorController.getFaults()
    }

    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [internalMotorController]
     * @see internalMotorController.set
     */
    fun setPercentOutput(percent: Double) = motorController.set(ControlMode.PercentOutput, percent)

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop positon setpoint
     * @see internalMotorController.set
     */
    fun setPositionSetpoint(position: Double) =
        motorController.set(ControlMode.Position, (position / feedbackCoefficient) - rawOffset)

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop positon setpoint
     * @param feedForward the closed-loop feed forward
     * @see internalMotorController.set
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) =
        motorController.set(
            ControlMode.Position, position / feedbackCoefficient - rawOffset,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see internalMotorController.set
     */
    fun setVelocitySetpoint(velocity: Double) {
        if (motorController is SparkMaxWrapper) {
            return motorController.set(ControlMode.Velocity, velocity / motorController.maxRPM)
        } else {
            return motorController.set(ControlMode.Velocity, velocity / feedbackCoefficient / 10.0)
        }
    }

    /**
     * Sets the closed-loop velocity setpoint with a specified [feedForward] value.
     *
     * @param velocity the closed-loop velocity setpoint
     * @param feedForward the closed-loop feed forward
     * @see internalMotorController.set
     */
    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        motorController.set(
            ControlMode.Velocity, velocity / feedbackCoefficient / 10.0,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Sets the closed-loop current setpoint.
     *
     * @param current the closed-loop current setpoint
     * @see internalMotorController.set
     */
    fun setCurrentSetpoint(current: Double) = motorController.set(ControlMode.Current, current)

    /**
     * Sets the closed-loop current setpoint with a specified [feedForward] value.
     *
     * @param current the closed-loop current setpoint
     * @param feedForward the closed-loop feed forward
     * @see internalMotorController.set
     */
    fun setCurrentSetpoint(current: Double, feedForward: Double) =
        motorController.set(ControlMode.Current, current, DemandType.ArbitraryFeedForward, feedForward)

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see internalMotorController.set
     */
    fun setMotionMagicSetpoint(position: Double) = {
        println("magicSetpoint = " + (position / feedbackCoefficient - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: $feedbackCoefficient.toInt() rawOffset: $rawOffset")
        motorController.set(ControlMode.MotionMagic, position / feedbackCoefficient - rawOffset)
    }
    /**
     * Sets the closed-loop Motion Magic position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @param feedForward the closed-loop feed forward
     * @see internalMotorController.set
     */
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        motorController.set(
            ControlMode.MotionMagic,
            (position / feedbackCoefficient) - rawOffset,
            DemandType.ArbitraryFeedForward, feedForward
        )

    /**
     * Neutralizes the motor output.
     *
     * @see internalMotorController.neutralOutput
     */
    fun stop() {
        motorController.neutralOutput()
    }

    /**
     * Configures the [internalMotorController] with instructions specified in the [body].
     *
     * @param timeoutMs the timeout to use on various motor functions
     * @param body the function which configures this [internalMotorController]
     */
    inline fun config(timeoutMs: Int = 100, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))
    }

    private inline fun allMotorControllers(body: (IMotorController) -> Unit) {
        body(motorController)
        followers.forEach(body)
    }
    fun setRawOffset(analogAngle: Angle) {
        when (motorController) {
            is SparkMaxWrapper -> {
                rawOffset = (analogAngle.asDegrees / feedbackCoefficient).toInt() - motorController.getSelectedSensorPosition(0)
                println("Motor Angle: ${motorController.analogAngle}; rawOffset: $rawOffset. Hi.")
            }
        }
    }

    inner class ConfigScope(private val timeoutMs: Int) {
        /**
         * The primary, "master" [internalMotorController].
         */
        val ctreController get() = motorController

        /**
         * An array of [internalMotorController]s which follow [ctreController].
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
         * Initializes the incremental encoder to match the analog encoder.
         */
        fun setRawOffsetConfig(offsetAngle: Angle) {
            setRawOffset(offsetAngle)
        }

        /**
         * Sets whether the motor should be inverted.
         *
         * @param inverted whether the motor should be inverted
         * @see internalMotorController.setInverted
         */
        fun inverted(inverted: Boolean) = allMotorControllers { it.inverted = inverted }

        /**
        * makes the encoder reading backwards
        *
        * @param PhaseSensor false is forwards, true is backwards
        */
        fun setSensorPhase(PhaseSensor: Boolean) {
            when(motorController) {
                is SparkMaxWrapper -> {
                    motorController.setSensorPhase(PhaseSensor)
                }
            }
        }

        /**
         * Enables brake mode.
         *
         * @see internalMotorController.setNeutralMode
         */
        fun brakeMode() = allMotorControllers { it.setNeutralMode(NeutralMode.Brake) }

        /**
         * Enables coast mode.
         *
         * @see internalMotorController.setNeutralMode
         */
        fun coastMode() = allMotorControllers { it.setNeutralMode(NeutralMode.Coast) }

        /**
         * Sets the phase of the sensor.
         *
         * @param inverted whether the phase of the sensor should be inverted
         * @see internalMotorController.setSensorPhase
         */
        fun sensorPhase(inverted: Boolean) = motorController.setSensorPhase(inverted)

        /**
         * Sets the amount of time required for closed loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see internalMotorController.configClosedloopRamp
         */
        fun closedLoopRamp(secondsToFull: Double) {
            motorController.configClosedloopRamp(secondsToFull, 20)
        }

        /**
         * Sets the amount of time required for open loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see internalMotorController.configOpenloopRamp
         */
        fun openLoopRamp(secondsToFull: Double) {
            motorController.configOpenloopRamp(secondsToFull, 20)
        }

        /**
         * Sets the minimum allowable output of the [internalMotorController].
         *
         * @param range the range of minimum values, e.g. -0.2..0.2 would mean minimum output of 0.2
         * @see internalMotorController.configNominalOutputReverse
         * @see internalMotorController.configNominalOutputForward
         */
        fun nominalOutputRange(range: DoubleRange) {
            motorController.configNominalOutputReverse(range.start, timeoutMs)
            motorController.configNominalOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the maximum allowable output of the [internalMotorController].
         *
         * @param range the range of maximum values, e.g. -0.8..0.8 would mean maximum output of 0.8
         * @see internalMotorController.configPeakOutputReverse
         * @see internalMotorController.configPeakOutputForward
         */
        fun peakOutputRange(range: DoubleRange) {
            motorController.configPeakOutputReverse(range.start, timeoutMs)
            motorController.configPeakOutputForward(range.endInclusive, timeoutMs)
        }

        /**
         * Sets the [acceleration] and [cruisingVelocity] for use in Motion Magic closed loop control.
         *
         * @param acceleration the target acceleration for Motion Magic to use
         * @param cruisingVelocity the peak target velocity for Motion Magic to use
         * @see internalMotorController.configMotionAcceleration
         * @see internalMotorController.configMotionCruiseVelocity
         */
        fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
            val srxAcceleration = (acceleration / feedbackCoefficient / 10.0).toInt()
            val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0).toInt()
            motorController.configMotionAcceleration(srxAcceleration, 20)
            motorController.configMotionCruiseVelocity(srxCruisingVelocity, 20)
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
         * @see internalMotorController.selectProfileSlot
         */
        fun pidSlot(slot: Int) = motorController.selectProfileSlot(slot, 0)

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
         * @see internalMotorController.configSelectedFeedbackSensor
         */
        fun encoderType(feedbackDevice: FeedbackDevice) {
            when (motorController) {
                is CTREVictorSPX -> motorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
                is CTRETalonSRX -> motorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
                is SparkMaxWrapper -> motorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)
            }
        }

        /**
         * Sets whether the feedback of the encoder is continuous (i.e. should not wrap back to 0 after
         * a full revolution).
         *
         * @param continuous whether the encoder should be treated as continuous
         * @see internalMotorController.configFeedbackNotContinuous
         */
        fun encoderContinuous(continuous: Boolean) {
            when (motorController) {
                is CTRETalonSRX -> motorController.configFeedbackNotContinuous(!continuous, timeoutMs)
                else -> {}
            }
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                motorController.config_kP(slot, p / feedbackCoefficient * 1024.0, timeoutMs)
            }

            fun i(i: Double) {
                motorController.config_kI(slot, i / feedbackCoefficient * 1024.0, timeoutMs)
            }

            fun d(d: Double) {
                motorController.config_kD(slot, d / feedbackCoefficient * 1024.0, timeoutMs)
            }

            fun f(f: Double) {
                motorController.config_kF(slot, f / feedbackCoefficient / 10.0, timeoutMs)
            }

            fun iZone(iZone: Double) {
                motorController.config_IntegralZone(slot, (iZone / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun allowableError(allowableError: Double) {
                motorController.configAllowableClosedloopError(slot, (allowableError / feedbackCoefficient).toInt(), timeoutMs)
            }

            fun maxIntegralAccumulator(maxIntegralAccumulator: Double) {
                motorController.configMaxIntegralAccumulator(slot, maxIntegralAccumulator / feedbackCoefficient, timeoutMs)
            }

            fun closedLoopPeakOutput(closedLoopPeakOutput: Double) {
                motorController.configClosedLoopPeakOutput(slot, closedLoopPeakOutput, timeoutMs)
            }

            fun closedLoopPeriod(closedLoopPeriod: Int) {
                motorController.configClosedLoopPeriod(slot, closedLoopPeriod, timeoutMs)
            }
        }
    }

}
