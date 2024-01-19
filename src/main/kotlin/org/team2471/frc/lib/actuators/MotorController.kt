package org.team2471.frc.lib.actuators

import com.ctre.phoenix6.configs.*
import com.ctre.phoenix6.controls.*
import com.ctre.phoenix6.hardware.TalonFX
import com.ctre.phoenix6.hardware.core.CoreTalonFX
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue
import com.ctre.phoenix6.signals.InvertedValue
import com.ctre.phoenix6.signals.NeutralModeValue
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.units.radians

sealed class MotorControllerID
/**
 * The ID of a Talon SRX motor controller.
 *
 * @param value the Talon's CAN ID
 */
data class TalonID(val value: Int) : MotorControllerID()

/**
 * The ID of a Spark MAX motor controller.
 *
 * @param value the SparkMax's CAN ID
 */
data class SparkMaxID(val value: Int) : MotorControllerID()

/**
 * The ID of a Talon FX motor controller.
 *
 * @param value the Falcon's CAN ID
 */
data class FalconID(val value: Int, val canBus:String? = null) : MotorControllerID()

private fun internalMotorController(id: MotorControllerID) = when (id) {
    is TalonID -> TalonFX(id.value)
    is FalconID -> if (id.canBus != null) TalonFX(id.value,id.canBus) else TalonFX(id.value)
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

    val followers = followerIds.map { id -> //untested
        val follower = internalMotorController(id)
        follower.setControl(StrictFollower(motorController.deviceID)) //untested
        follower
    }.toTypedArray()

    val motorID = deviceId

//    /**
//     * The selected PID slot.
//     */
//    var pidSlot: Int = 0
//        set(value) {
//            motorController.selectProfileSlot(value, 0)
//            field = value
//        }

    /**
     * The current being drawn by this [MotorController].
     * Note that this will only work if the [MotorController] is a Talon FX or Spark Max. Attempts
     * to use this method on any other motor controller will result in an [IllegalStateException].
     */
    val current: Double //untested
        get() = when (motorController) {
            is TalonFX -> motorController.statorCurrent.value //untested
            is SparkMaxWrapper ->  {
                motorController.current
            }
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see CoreTalonFX.getRotorVelocity
     */
    val velocity: Double //untested
        get() = motorController.rotorVelocity.value.radians.asDegrees * feedbackCoefficient * 10.0 //untested

    /**
     * The output percent, from 0 to 1.
     *
     * @see CoreTalonFX.getDutyCycle
     */
    val output: Double //untested
        get() = motorController.dutyCycle.value //untested

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see CoreTalonFX.getRotorPosition
     */
    var position: Double //untested
        get() = (motorController.rotorPosition.value.radians.asDegrees + rawOffset) * feedbackCoefficient //untested
        set(value) {
            motorController.setPosition((value / feedbackCoefficient), 0.0) //untested
        }

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
     * @see CoreTalonFX.getRotorPosition
     */
    val rawPosition: Double //untested
        get() = motorController.rotorPosition.value //untested

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double //untested
        get() = motorController.closedLoopError.value * feedbackCoefficient

    init {
        allMotorControllers {
            when (motorController) {
                is TalonFX -> motorController.configurator.apply(TalonFXConfiguration()) //untested
                is SparkMaxWrapper -> {
                    /*motorController.restoreFactoryDefaults()*/
                }
            }

            it.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast)) //untested
        }

        motorController.setPosition(0.0, 0.0) //untested
    }

//    fun hasfaults() {
        //return motorController.getFaults()
//    }
//    fun getFollowerStatusFramePeriod(frame: StatusFrame, timeoutMs: Int = 100) : Int {
//        return if (followers.isNotEmpty()) {
//            followers[0].getStatusFramePeriod(frame, timeoutMs)
//        } else {
//            -1
//        }
//    }
    fun setStatusFramePeriod(periodHz: Double, timeoutMs: Double = 100.0) = allMotorControllers { it.position.setUpdateFrequency(periodHz, timeoutMs) } //untested

    fun setFollowerStatusFramePeriod(periodHz: Double, timeoutMs: Double = 100.0) = allFollowers { it.position.setUpdateFrequency(periodHz, timeoutMs) } //untested
    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [internalMotorController]
     * @see CoreTalonFX.setControl
     * @see DutyCycleOut
     */
    fun setPercentOutput(percent: Double) = motorController.setControl(DutyCycleOut(percent)) //untested

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop positon setpoint
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle
     */
    fun setPositionSetpoint(position: Double) { //untested
        motorController.setControl(PositionDutyCycle((position / feedbackCoefficient) - rawOffset))
    }

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop positon setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle.withFeedForward
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) { //untested
        motorController.setControl(PositionDutyCycle((position / feedbackCoefficient) - rawOffset).withFeedForward(feedForward))
    }

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see CoreTalonFX.setControl
     * @see VelocityDutyCycle
     */
    fun setVelocitySetpoint(velocity: Double) { //untested
        motorController.setControl(VelocityDutyCycle(velocity / feedbackCoefficient / 10.0))
    }

    /**
     * Sets the closed-loop velocity setpoint with a specified [feedForward] value.
     *
     * @param velocity the closed-loop velocity setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see VelocityDutyCycle.withFeedForward
     */
    fun setVelocitySetpoint(velocity: Double, feedForward: Double) = //untested
        motorController.setControl(
            VelocityDutyCycle(velocity / feedbackCoefficient / 10.0).withFeedForward(feedForward)
        )

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see CoreTalonFX.setControl
     * @see MotionMagicDutyCycle
     */
    fun setMotionMagicSetpoint(position: Double) { //untested
        println("magicSetpoint = " + (position / feedbackCoefficient - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: $feedbackCoefficient.toInt() rawOffset: $rawOffset")
        motorController.setControl(MotionMagicDutyCycle(position / feedbackCoefficient - rawOffset))
    }
    /**
     * Sets the closed-loop Motion Magic position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see MotionMagicDutyCycle.withFeedForward
     */
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) = //untested
        motorController.setControl(
            MotionMagicDutyCycle((position / feedbackCoefficient) - rawOffset).withFeedForward(feedForward)
        )

    /**
     * Neutralizes the motor output.
     *
     * @see CoreTalonFX.setControl
     */
    fun stop() { //untested
        motorController.setControl(NeutralOut())
    }

    /**
     * Configures the [CoreTalonFX] with instructions specified in the [body].
     *
     * @param timeoutMs the timeout to use on various motor functions
     * @param body the function which configures this [CoreTalonFX]
     */
    inline fun config(timeoutMs: Int = 100, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))

    }

    private inline fun allMotorControllers(body: (CoreTalonFX) -> Unit) {
        body(motorController)
        followers.forEach(body)
    }
    private inline fun allFollowers(body: (CoreTalonFX) -> Unit) {
        followers.forEach(body)
    }
    fun setRawOffset(offset: Double) {  //untested
        when (motorController) {
            is SparkMaxWrapper -> {
                rawOffset = ((offset / feedbackCoefficient).toInt() - motorController.getSelectedSensorPosition()).toInt()
                println("Motor Angle: ${motorController.analogAngle}; rawOffset: $rawOffset. Hi.")
            }
            is TalonFX -> {
//                println("Set raw offset to $rawOffset")
                rawOffset = ((offset / feedbackCoefficient).toInt() - motorController.position.value).toInt()

            }
            else -> {
                println("NO SET RAW OFFSET FOR THIS MOTOR CONTROLLER")
            }
        }
    }

    fun restoreFactoryDefaults() {
        when (motorController) {
            is SparkMaxWrapper -> {
                motorController.restoreFactoryDefaults()
                println("before followers defaults restored")
//                (followers[0] as SparkMaxWrapper).restoreFactoryDefaults()
//                println("followers factory defaults restored")
            } else -> {
                println("restoreFactoryDefaults does not work for this motor controller.")
            }
        }
    }

    /**
     * Enables brake mode.
     *
     * @see CoreTalonFX.getConfigurator
     * @see MotorOutputConfigs.withNeutralMode
     */
    fun brakeMode() = allMotorControllers { it.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake)) } //untested

    /**
     * Enables coast mode.
     *
     * @see CoreTalonFX.getConfigurator
     * @see MotorOutputConfigs.withNeutralMode
     */
    fun coastMode() = allMotorControllers { it.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast)) } //untested

    inner class ConfigScope(private val timeoutMs: Int) {
        /**
         * The primary, "master" [CoreTalonFX].
         */
        val ctreController get() = motorController

        /**
         * An array of [CoreTalonFX]s which follow [ctreController].
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

        // burns spark max to retain settings between boot
        fun burnSettings(){
            if (motorController is SparkMaxWrapper) {
                motorController.burnFlash()
            } else {
                println("This motor controller does not burn settings.")
            }
        }

        /**
         * Initializes the incremental encoder to match the analog encoder.
         */
        fun setRawOffsetConfig(offset: Double) {
            setRawOffset(offset)
        }

        /**
         * Sets whether the motor should be inverted.
         *
         * @param invertedValue whether the motor should be inverted
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.Inverted
         */
        fun inverted(invertedValue: InvertedValue) = allMotorControllers { //untested
            val motorConfig = MotorOutputConfigs()
            motorConfig.Inverted = invertedValue
            it.configurator.apply(motorConfig)
        }
        fun inverted(invert: Boolean) = allMotorControllers {//I am very doubtful this will work
            if (invert) {
                val config = MotorOutputConfigs()
                it.configurator.refresh(config) //.refresh populates the passed in config??
                config.Inverted = when (config.Inverted) {
                    InvertedValue.CounterClockwise_Positive -> InvertedValue.Clockwise_Positive
                    InvertedValue.Clockwise_Positive -> InvertedValue.CounterClockwise_Positive
                    else -> config.Inverted
                }
                it.configurator.apply(config)
            } else {
                it.configurator.apply(MotorOutputConfigs())
            }
        }

        /**
         * Sets whether the motor followers should be inverted relative to the main motor.
         *
         * @param invertedValue whether the motor should be inverted
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.Inverted
         */
        fun followersInverted(invertedValue: InvertedValue) = allFollowers { //untested
            val motorConfig = MotorOutputConfigs()
            motorConfig.Inverted = invertedValue
            it.configurator.apply(motorConfig)
        }

        fun followersInverted(invert: Boolean) = allFollowers {//I am very doubtful this will work
            if (invert) {
                val config = MotorOutputConfigs()
                it.configurator.refresh(config) //.refresh populates the passed in config??
                config.Inverted = when (config.Inverted) {
                    InvertedValue.CounterClockwise_Positive -> InvertedValue.Clockwise_Positive
                    InvertedValue.Clockwise_Positive -> InvertedValue.CounterClockwise_Positive
                    else -> config.Inverted
                }
                it.configurator.apply(config)
            } else {
                it.configurator.apply(MotorOutputConfigs())
            }
        }

        /**
         * Enables brake mode.
         *
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withNeutralMode
         */
        fun brakeMode() = allMotorControllers { it.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Brake)) } //untested

        /**
         * Enables coast mode.
         *
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withNeutralMode
         */
        fun coastMode() = allMotorControllers { it.configurator.apply(MotorOutputConfigs().withNeutralMode(NeutralModeValue.Coast)) } //untested

        /**
         * Sets the amount of time required for closed loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CoreTalonFX.getConfigurator
         * @see ClosedLoopRampsConfigs.withDutyCycleClosedLoopRampPeriod
         */
        fun closedLoopRamp(secondsToFull: Double) { //untested
            motorController.configurator.apply(ClosedLoopRampsConfigs().withDutyCycleClosedLoopRampPeriod(secondsToFull)) //untested
        }

        /**
         * Sets the amount of time required for open loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CoreTalonFX.getConfigurator
         * @see OpenLoopRampsConfigs.withDutyCycleOpenLoopRampPeriod
         */
        fun openLoopRamp(secondsToFull: Double) { //untested
            motorController.configurator.apply(OpenLoopRampsConfigs().withDutyCycleOpenLoopRampPeriod(secondsToFull), timeoutMs * 1000.0) //untested
        }

        /**
         * Sets the maximum allowable output of the [internalMotorController].
         *
         * @param range the range of maximum values, e.g. -0.8..0.8 would mean maximum output of 0.8
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withPeakReverseDutyCycle
         * @see MotorOutputConfigs.withPeakForwardDutyCycle
         */
        fun peakOutputRange(range: DoubleRange) { //untested
            motorController.configurator.apply(MotorOutputConfigs().withPeakForwardDutyCycle(range.start), timeoutMs * 1000.0) //untested
            motorController.configurator.apply(MotorOutputConfigs().withPeakReverseDutyCycle(range.endInclusive), timeoutMs * 1000.0)
        }

        /**
         * Sets the [acceleration] and [cruisingVelocity] for use in Motion Magic closed loop control.
         *
         * @param acceleration the target acceleration for Motion Magic to use
         * @param cruisingVelocity the peak target velocity for Motion Magic to use
         * @see CoreTalonFX.getConfigurator
         * @see MotionMagicConfigs.withMotionMagicAcceleration
         * @see MotionMagicConfigs.withMotionMagicCruiseVelocity
         */
        fun motionMagic(acceleration: Double, cruisingVelocity: Double) { //untested
            val srxAcceleration = (acceleration / feedbackCoefficient / 10.0)
            val srxCruisingVelocity = (cruisingVelocity / feedbackCoefficient / 10.0)
            motorController.configurator.apply(MotionMagicConfigs().withMotionMagicAcceleration(srxAcceleration), 0.020)
            motorController.configurator.apply(MotionMagicConfigs().withMotionMagicCruiseVelocity(srxCruisingVelocity), 0.020)
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

//        /**
//         * Selects a specific PID slot.
//         *
//         * @see internalMotorController.selectProfileSlot
//         */
//        fun pidSlot(slot: Int) = motorController.selectProfileSlot(slot, 0)

        /**
         * Limits the current to a [continuousLimit], [peakLimit] and [peakDuration].
         *
         * @param continuousLimit the continuous allowable current-draw
         * @param peakLimit the peak allowable current
         * @param peakDuration the peak allowable duration
         * @see TalonFX.getConfigurator
         * @see CurrentLimitsConfigs.withSupplyCurrentLimit
         * @see CurrentLimitsConfigs.withStatorCurrentLimit
         * @see CurrentLimitsConfigs.withSupplyTimeThreshold
         * @see CurrentLimitsConfigs.withStatorCurrentLimitEnable
         * @see CurrentLimitsConfigs.withSupplyCurrentLimitEnable
         * @see SparkMaxWrapper.setCurrentLimit
         */
        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) { //untested
            // apply to following
            allMotorControllers { controller ->
//                if (controller is CTRETalonSRX) {
//                    controller.configContinuousCurrentLimit(continuousLimit, timeoutMs)
//                    controller.configPeakCurrentLimit(peakLimit, timeoutMs)
//                    controller.configPeakCurrentDuration(peakDuration, timeoutMs)
//                    controller.enableCurrentLimit(true)
//                }
                if (controller is TalonFX) {
                    controller.configurator.apply(CurrentLimitsConfigs().withSupplyCurrentLimit(continuousLimit.toDouble()).withStatorCurrentLimit(peakLimit.toDouble()).withSupplyTimeThreshold(peakDuration.toDouble()).withStatorCurrentLimitEnable(true).withSupplyCurrentLimitEnable(true))
                }
                if (controller is SparkMaxWrapper) {
                    controller.setCurrentLimit(peakLimit)
                }
            }
        }

        /**
         * Sets the [FeedbackSensorSourceValue] to use for closed loop and sensor feedback.
         *
         * @param feedbackDevice the [FeedbackSensorSourceValue] to use
         * @see CoreTalonFX.getConfigurator
         * @see FeedbackConfigs.withFeedbackSensorSource
         */
        fun encoderType(feedbackDevice: FeedbackSensorSourceValue) { //untested
            when (motorController) {
//                is CTRETalonSRX -> motorController.configSelectedFeedbackSensor(feedbackDevice, 0, timeoutMs)'
                is TalonFX -> motorController.configurator.apply(FeedbackConfigs().withFeedbackSensorSource(feedbackDevice), timeoutMs * 1000.0)
            }
        }

        /**
         * Sets whether the feedback of the encoder is continuous (i.e. should not wrap back to 0 after
         * a full revolution).
         *
         * @param continuous whether the encoder should be treated as continuous
         * @see CoreTalonFX.getConfigurator
         * @see ClosedLoopGeneralConfigs.ContinuousWrap
         */
        fun encoderContinuous(continuous: Boolean) { //untested
            when (motorController) {
//                is CTRETalonSRX -> motorController.configFeedbackNotContinuous(!continuous, timeoutMs)
                is TalonFX -> {
                    val config = ClosedLoopGeneralConfigs()
                    config.ContinuousWrap = continuous
                    motorController.configurator.apply(config)
                }
                else -> {}
            }
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) { //untested
                motorController.configurator.apply(Slot0Configs().withKP(p / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
            }

            fun i(i: Double) { //untested
                motorController.configurator.apply(Slot0Configs().withKI(i / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
            }

            fun d(d: Double) { //untested
                println("d=$d")
                motorController.configurator.apply(Slot0Configs().withKP(d / feedbackCoefficient * 1024.0), timeoutMs * 1000.0)
            }
        }
    }

}