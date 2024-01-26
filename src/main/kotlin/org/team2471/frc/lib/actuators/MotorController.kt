package org.team2471.frc.lib.actuators

import org.team2471.frc.lib.math.DoubleRange

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

private fun internalMotorController(id: MotorControllerID): IMotorController = when (id) {
    is TalonID -> TalonFXWrapper(id.value)
    is FalconID -> if (id.canBus != null) TalonFXWrapper(id.value,id.canBus) else TalonFXWrapper(id.value)
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
        set(value) {
            motorController.feedbackCoefficient = value
            field = value
        }
    private var rawOffset = 0

    val followers = followerIds.map { id -> //untested
        val follower = internalMotorController(id)
        follower.follow(motorController)
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
        get() = motorController.current

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see CoreTalonFX.getRotorVelocity
     */
    val velocity: Double //untested
        get() = motorController.getSelectedSensorVelocity(0) * feedbackCoefficient * 10.0 //untested

    /**
     * The output percent, from 0 to 1.
     *
     * @see CoreTalonFX.getDutyCycle
     */
    val output: Double //untested
        get() = motorController.motorOutputPercent //untested

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see CoreTalonFX.getRotorPosition
     */
    var position: Double //untested
        get() = (motorController.getSelectedSensorPosition(0) + rawOffset) * feedbackCoefficient //untested
        set(value) {
            motorController.setSelectedSensorPosition((value / feedbackCoefficient), 0) //untested
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
        get() = motorController.getSelectedSensorPosition(0) //untested

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double //untested
        get() = motorController.getClosedLoopError(0) * feedbackCoefficient

    init {
        allMotorControllers {
            it.restoreFactoryDefaults()
            it.coastMode()
        }

        motorController.setSelectedSensorPosition(0.0, 0) //untested
    }

    fun setStatusFramePeriod(periodHz: Int, timeoutMs: Int = 100) = allMotorControllers { it.setStatusFramePeriod(periodHz, timeoutMs) } //untested

    fun setFollowerStatusFramePeriod(periodHz: Int, timeoutMs: Int = 100) = allFollowers { it.setStatusFramePeriod(periodHz, timeoutMs) } //untested
    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [internalMotorController]
     * @see CoreTalonFX.setControl
     * @see DutyCycleOut
     */
    fun setPercentOutput(percent: Double) = motorController.setPercentOutput(percent) //untested

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop position setpoint
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle
     */
    fun setPositionSetpoint(position: Double) { //untested
        motorController.setPositionSetpoint((position / feedbackCoefficient) - rawOffset)
    }

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop position setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle.withFeedForward
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) { //untested
        motorController.setPositionSetpoint((position / feedbackCoefficient) - rawOffset, feedForward)
    }

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see CoreTalonFX.setControl
     * @see VelocityDutyCycle
     */
    fun setVelocitySetpoint(velocity: Double) { //untested
        motorController.setVelocitySetpoint(velocity / feedbackCoefficient / 10.0)
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
        motorController.setVelocitySetpoint(velocity / feedbackCoefficient / 10.0, feedForward)

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see CoreTalonFX.setControl
     * @see MotionMagicDutyCycle
     */
    fun setMotionMagicSetpoint(position: Double) { //untested
        println("magicSetpoint = " + (position / feedbackCoefficient - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: $feedbackCoefficient.toInt() rawOffset: $rawOffset")
        motorController.setMotionMagicSetpoint(position / feedbackCoefficient - rawOffset)
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
        motorController.setMotionMagicSetpoint((position / feedbackCoefficient) - rawOffset, feedForward)

    /**
     * Neutralizes the motor output.
     *
     * @see CoreTalonFX.setControl
     */
    fun stop() { //untested
        motorController.stop()
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

    private inline fun allMotorControllers(body: (IMotorController) -> Unit) {
        body(motorController)
        followers.forEach(body)
    }
    private inline fun allFollowers(body: (IMotorController) -> Unit) {
        followers.forEach(body)
    }
    fun setRawOffset(offset: Double) {  //untested
        when (motorController) {
            is SparkMaxWrapper -> {
                println("before offset")
                rawOffset = ((offset / feedbackCoefficient).toInt() - motorController.getSelectedSensorPosition(0)).toInt()
                println("Motor Angle: ${motorController.analogAngle}; rawOffset: $rawOffset. Hi.")
            }
            is TalonFXWrapper -> {
//                println("Set raw offset to $rawOffset")
                rawOffset = ((offset / feedbackCoefficient).toInt() - motorController.getSelectedSensorPosition(0)).toInt()

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
    fun brakeMode() = allMotorControllers { it.brakeMode() } //untested

    /**
     * Enables coast mode.
     *
     * @see CoreTalonFX.getConfigurator
     * @see MotorOutputConfigs.withNeutralMode
     */
    fun coastMode() = allMotorControllers { it.coastMode() } //untested

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
        fun inverted(invert: Boolean) = allMotorControllers {//I am very doubtful this will work
            it.setInverted(invert)
        }

        /**
         * Sets whether the motor followers should be inverted relative to the main motor.
         *
         * @param invertedValue whether the motor should be inverted
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.Inverted
         */
//        fun followersInverted(invertedValue: InvertedValue) = allFollowers { //untested
//            val motorConfig = MotorOutputConfigs()
//            motorConfig.Inverted = invertedValue
//            it.configurator.apply(motorConfig)
//        }

        fun followersInverted(invert: Boolean) = allFollowers {//I am very doubtful this will work
            it.setInverted(invert)
        }

        /**
         * Enables brake mode.
         *
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withNeutralMode
         */
        fun brakeMode() = allMotorControllers { it.brakeMode() } //untested

        /**
         * Enables coast mode.
         *
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withNeutralMode
         */
        fun coastMode() = allMotorControllers { it.coastMode() } //untested

        /**
         * Sets the amount of time required for closed loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CoreTalonFX.getConfigurator
         * @see ClosedLoopRampsConfigs.withDutyCycleClosedLoopRampPeriod
         */
        fun closedLoopRamp(secondsToFull: Double) { //untested
            motorController.closedLoopRamp(secondsToFull)
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
            motorController.openLoopRamp(secondsToFull)
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
            motorController.peakOutputRange(range)
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
            motorController.motionMagic(acceleration, cruisingVelocity)
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
                controller.currentLimit(continuousLimit, peakLimit, peakDuration)
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
            motorController.encoderContinuous(continuous)
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) { //untested
                motorController.config_kP(p / feedbackCoefficient * 1024.0)
            }

            fun i(i: Double) { //untested
                motorController.config_kI(i / feedbackCoefficient * 1024.0)
            }

            fun d(d: Double) { //untested
                motorController.config_kD(d / feedbackCoefficient * 1024.0)
            }
        }
    }
}