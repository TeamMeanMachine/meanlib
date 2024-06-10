package org.team2471.frc.lib.actuators

import edu.wpi.first.math.system.plant.DCMotor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.coroutines.periodic
import org.team2471.frc.lib.math.DoubleRange
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode

sealed class MotorControllerID {abstract val value: Int; abstract val name: String}
/**
 * The ID of a Talon SRX motor controller.
 *
 * @param value the Talon's CAN ID
 * @param name log path of motor "Subsystem/Name"
 */
data class TalonID(override val value: Int, override val name: String) : MotorControllerID()

/**
 * The ID of a Spark MAX motor controller.
 *
 * @param value the SparkMax's CAN ID
 * @param name log path of motor "Subsystem/Name"
 */
data class SparkMaxID(override val value: Int, override val name: String) : MotorControllerID()

/**
 * The ID of a Talon FX motor controller.
 *
 * @param value the Falcon's CAN ID
 * @param name log path of motor "Subsystem/Name"
 */
data class FalconID(override val value: Int, override val name: String, val canBus: String? = null) : MotorControllerID()

private fun internalMotorController(id: MotorControllerID): MotorControllerIO = when (id) {
    is TalonID -> TalonFXWrapper(id.value)
    is FalconID -> TalonFXWrapper(id.value, id.canBus ?: "")
    is SparkMaxID -> SparkMaxWrapper(id.value)
}

/**
 * A single motor controller or combination of motor controllers which follow a primary device.
 *
 * @param deviceId the [MotorControllerID] of the primary, "master" motor controller
 * @param followerIds optional [MotorControllerID]s of motor controllers which should follow the primary
 */
class MotorController(deviceId: MotorControllerID, vararg followerIds: MotorControllerID) {
    private val io: MotorControllerIO = when(robotMode) {
        RobotMode.REAL -> internalMotorController(deviceId)
        RobotMode.REPLAY, RobotMode.SIM -> MotorControllerSim()
    }
    private val inputs = MotorControllerIO.MotorControllerIOInputs(deviceId.name)

    private var feedbackCoefficient = 1.0
        set(value) {
            io.setSimFeedbackCoefficient(value)
            field = value
        }

    var rawOffset = 0.0
        private set

    val followers = followerIds.map { id ->
        val follower = internalMotorController(id)
        follower.follow(io)
        follower
    }.toTypedArray()

    val name = deviceId.name


    fun processInputs() {
        io.updateInputs(inputs)
        Logger.processInputs("Motors", inputs)
    }

    init {
        GlobalScope.launch {
            periodic(0.05) {
                processInputs()
            }
        }
    }

    /**
     * The current being drawn by this [MotorController].
     * Note that this will only work if the [MotorController] is a Talon FX or Spark Max. Attempts
     * to use this method on any other motor controller will result in an [IllegalStateException].
     */
    val current: Double
        get() = io.current

    /**
     * The velocity calculated from the selected sensor (in units specified by
     * [ConfigScope.feedbackCoefficient] per second).
     *
     * @see CoreTalonFX.getRotorVelocity
     */
    val velocity: Double
        get() = io.getSelectedSensorVelocity() * feedbackCoefficient

    /**
     * The output percent, from 0 to 1.
     *
     * @see CoreTalonFX.getDutyCycle
     */
    val output: Double
        get() = io.outputPercent

    /**
     * The position of the selected sensor (in units specified by [ConfigScope.feedbackCoefficient]).
     *
     * @see CoreTalonFX.getRotorPosition
     */
    var position: Double
        get() = (io.getSelectedSensorPosition() + rawOffset) * feedbackCoefficient
        set(value) {
            io.setSelectedSensorPosition((value / feedbackCoefficient))
        }

    var analogPosition: Double
        get() = when (io) {
            is SparkMaxWrapper -> io.analogPosition
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }
        set(value) {}

    var analogAngle: Double
        get() = when(io) {
            is SparkMaxWrapper -> io.analogAngle
            else -> throw IllegalStateException("Current cannot be read from this motor controller")
        }
        set(value) {}

    /**
     * The raw position of the selected sensor in revolutions for Sparks at least.
     */
    val rawPosition: Double
        get() = io.getSelectedSensorPosition()

    /**
     * The closed loop error (in units specified by [ConfigScope.feedbackCoefficient]).
     */
    val closedLoopError: Double
        get() = io.getClosedLoopError() * feedbackCoefficient

    init {
        allMotorControllers {
            it.restoreFactoryDefaults()
            it.coastMode()
        }

        io.setSelectedSensorPosition(0.0)
    }

    fun setStatusFramePeriod(periodHz: Int, timeoutSec: Double = 0.05) = allMotorControllers { it.setStatusFramePeriod(periodHz, timeoutSec) }

    fun setFollowerStatusFramePeriod(periodHz: Int, timeoutSec: Double = 0.05) = allFollowers { it.setStatusFramePeriod(periodHz, timeoutSec) }
    /**
     * Sets the percent output.
     *
     * @param percent the percent output at which to set the [internalMotorController]
     * @see CoreTalonFX.setControl
     * @see DutyCycleOut
     */
    fun setPercentOutput(percent: Double) = io.setPercentOutput(percent)

    /**
     * Sets the closed-loop position setpoint.
     *
     * @param position the closed-loop position setpoint
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle
     */
    fun setPositionSetpoint(position: Double) {
//        println("setting position setpoint ${(position / feedbackCoefficient) - rawOffset}")
        io.setPositionSetpoint((position / feedbackCoefficient) - rawOffset)
    }

    /**
     * Sets the closed-loop position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop position setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see PositionDutyCycle.withFeedForward
     */
    fun setPositionSetpoint(position: Double, feedForward: Double) {
        io.setPositionSetpoint((position / feedbackCoefficient) - rawOffset, feedForward)
    }

    /**
     * Sets the closed-loop velocity setpoint.
     *
     * @param velocity the closed-loop velocity setpoint
     * @see CoreTalonFX.setControl
     * @see VelocityDutyCycle
     */
    fun setVelocitySetpoint(velocity: Double) {
        io.setVelocitySetpoint(velocity / feedbackCoefficient / 10.0)
    }

    /**
     * Sets the closed-loop velocity setpoint with a specified [feedForward] value.
     *
     * @param velocity the closed-loop velocity setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see VelocityDutyCycle.withFeedForward
     */
    fun setVelocitySetpoint(velocity: Double, feedForward: Double) =
        io.setVelocitySetpoint(velocity / feedbackCoefficient /*/ 10.0 from s to 100ms*/, feedForward / feedbackCoefficient)

    /**
     * Sets the closed-loop Motion Magic position setpoint.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @see CoreTalonFX.setControl
     * @see MotionMagicDutyCycle
     */
    fun setMotionMagicSetpoint(position: Double) {
        println("magicSetpoint = " + (position / feedbackCoefficient - rawOffset) + " rawPosition: $rawPosition position: ${position.toInt()} feedbackCoefficient: $feedbackCoefficient.toInt() rawOffset: $rawOffset")
        io.setMotionMagicSetpoint(position / feedbackCoefficient - rawOffset)
    }
    /**
     * Sets the closed-loop Motion Magic position setpoint with a specified [feedForward] value.
     *
     * @param position the closed-loop Motion Magic position setpoint
     * @param feedForward the closed-loop feed forward
     * @see CoreTalonFX.setControl
     * @see MotionMagicDutyCycle.withFeedForward
     */
    fun setMotionMagicSetpoint(position: Double, feedForward: Double) =
        io.setMotionMagicSetpoint((position / feedbackCoefficient) - rawOffset, feedForward)

    /**
     * Attempt to get encoder plugged directly into SparkMAX. Has not worked yet.
     *
     * @param countPerRev the counts per revolution of the alternate encoder. Can be found in the Alternate Encoder SparkMAX guide
     */
    fun getAlternateEncoder(countPerRev: Int): Double {
            return when (io) {
                is SparkMaxWrapper -> {
//                    println("In alternate encoder spark max")
                    io.getAlternateEncoder(countPerRev)
                }
                else -> throw IllegalStateException("No alternate encoder from this motor controller")
            }
        }

    /**
     * Neutralizes the motor output.
     *
     * @see CoreTalonFX.setControl
     */
    fun stop() {
        io.stop()
    }


    fun setP(p: Double) {
        io.config_kP(p / feedbackCoefficient * 1024.0)
    }
    fun setD(d: Double) {
        io.config_kD(d / feedbackCoefficient * 1024.0)
    }

    fun getP() : Double = io.getPValue() * feedbackCoefficient / 1024.0
    fun getD(): Double = io.getDValue() * feedbackCoefficient / 1024.0
    fun getI(): Double = io.getIValue() * feedbackCoefficient / 1024.0


    /**
     * Configures the [CoreTalonFX] with instructions specified in the [body].
     *
     * @param timeoutMs the timeout to use on various motor functions
     * @param body the function which configures this [CoreTalonFX]
     */
    inline fun config(timeoutMs: Int = 100, body: ConfigScope.() -> Unit) = apply {
        body(ConfigScope(timeoutMs))

    }

    private inline fun allMotorControllers(body: (MotorControllerIO) -> Unit) {
        body(io)
        followers.forEach(body)
    }
    private inline fun allFollowers(body: (MotorControllerIO) -> Unit) {
        followers.forEach(body)
    }
    fun setRawOffset(offset: Double) {
        rawOffset = ((offset / feedbackCoefficient) - io.getSelectedSensorPosition())
//        println("offset: $offset fc: ${feedbackCoefficient.roundToInt()} pos: ${motorController.getSelectedSensorPosition()}")
    }

    fun restoreFactoryDefaults() {
        io.restoreFactoryDefaults()
    }

    /**
     * Enables brake mode.
     *
     * @see CoreTalonFX.getConfigurator
     * @see MotorOutputConfigs.withNeutralMode
     */
    fun brakeMode() = allMotorControllers { it.brakeMode() }

    /**
     * Enables coast mode.
     *
     * @see CoreTalonFX.getConfigurator
     * @see MotorOutputConfigs.withNeutralMode
     */
    fun coastMode() = allMotorControllers { it.coastMode() }

    inner class ConfigScope(private val timeoutMs: Int) {
        /**
         * The primary, "master" [CoreTalonFX].
         */
        val ctreController get() = io

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

        fun setSimMotor(motor: DCMotor) {
            io.setSimMotor(motor)
        }
        fun setSimMOI(jKgMetersSquared: Double) {
            io.setSimMOI(jKgMetersSquared)
        }

        fun setSimMotorAndMOI(motor: DCMotor, jKgMetersSquared: Double) {
            setSimMotor(motor)
            setSimMOI(jKgMetersSquared)
        }

        // burns spark max to retain settings between boot
        fun burnSettings() {
            io.burnFlash()
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
//        fun followersInverted(invertedValue: InvertedValue) = allFollowers {
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
        fun brakeMode() = allMotorControllers { it.brakeMode() }

        /**
         * Enables coast mode.
         *
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withNeutralMode
         */
        fun coastMode() = allMotorControllers { it.coastMode() }

        /**
         * Sets the amount of time required for closed loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CoreTalonFX.getConfigurator
         * @see ClosedLoopRampsConfigs.withDutyCycleClosedLoopRampPeriod
         */
        fun closedLoopRamp(secondsToFull: Double) {
            io.closedLoopRamp(secondsToFull)
        }

        /**
         * Sets the amount of time required for open loop control of the [internalMotorController] to go
         * from neutral output to full power.
         *
         * @param secondsToFull minimum desired time to go from neutral to full throttle
         * @see CoreTalonFX.getConfigurator
         * @see OpenLoopRampsConfigs.withDutyCycleOpenLoopRampPeriod
         */
        fun openLoopRamp(secondsToFull: Double) {
            io.openLoopRamp(secondsToFull)
        }

        /**
         * Sets the maximum allowable output of the [internalMotorController].
         *
         * @param range the range of maximum values, e.g. -0.8..0.8 would mean maximum output of 0.8
         * @see CoreTalonFX.getConfigurator
         * @see MotorOutputConfigs.withPeakReverseDutyCycle
         * @see MotorOutputConfigs.withPeakForwardDutyCycle
         */
        fun peakOutputRange(range: DoubleRange) {
            io.peakOutputRange(range)
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
        fun motionMagic(acceleration: Double, cruisingVelocity: Double) {
            io.motionMagic(acceleration, cruisingVelocity)
        }

        /**
         * Sets a raw offset, in encoder ticks, to the selected sensor.
         *
         * @param ticks the number of ticks offset to add to the selected sensor
         */
        fun rawOffset(ticks: Double) {
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
        fun currentLimit(continuousLimit: Int, peakLimit: Int, peakDuration: Int) {
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
        fun encoderContinuous(continuous: Boolean) {
            io.encoderContinuous(continuous)
        }

        inner class PIDConfigScope(private val slot: Int) {
            fun p(p: Double) {
                io.config_kP(p / feedbackCoefficient * 1024.0)
            }

            fun i(i: Double) {
                io.config_kI(i / feedbackCoefficient * 1024.0)
            }

            fun d(d: Double) {
                io.config_kD(d / feedbackCoefficient * 1024.0)
            }
        }
    }
}