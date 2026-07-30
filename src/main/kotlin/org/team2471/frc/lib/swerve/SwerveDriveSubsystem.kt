package org.team2471.frc.lib.swerve

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import com.ctre.phoenix6.BaseStatusSignal
import com.ctre.phoenix6.CANBus
import com.ctre.phoenix6.StatusSignalCollection
import com.ctre.phoenix6.configs.CANcoderConfiguration
import com.ctre.phoenix6.configs.TalonFXConfiguration
import com.ctre.phoenix6.hardware.CANcoder
import com.ctre.phoenix6.hardware.Pigeon2
import com.ctre.phoenix6.swerve.SwerveDrivetrain
import com.ctre.phoenix6.swerve.SwerveDrivetrainConstants
import com.ctre.phoenix6.swerve.SwerveModule.DriveRequestType
import com.ctre.phoenix6.swerve.SwerveModuleConstants
import com.ctre.phoenix6.swerve.SwerveRequest.*
import com.ctre.phoenix6.swerve.utility.PhoenixPIDController
import com.therekrab.autopilot.APConstraints
import com.therekrab.autopilot.APProfile
import com.therekrab.autopilot.APTarget
import com.therekrab.autopilot.Autopilot
import org.littletonrobotics.junction.AutoLogOutput
import org.team2471.frc.lib.commands.MechanismBase
import org.team2471.frc.lib.commands.addPeriodic
import org.team2471.frc.lib.commands.addSimulationPeriodic
import org.team2471.frc.lib.ctre.setCANCoderAngle
import org.team2471.frc.lib.math.deadband
import org.team2471.frc.lib.math.findClosestPointOnLine
import org.team2471.frc.lib.math.normalize
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.math.toPose2d
import org.team2471.frc.lib.units.absoluteValue
import org.team2471.frc.lib.units.asDegrees
import org.team2471.frc.lib.units.asDegreesPerSecond
import org.team2471.frc.lib.units.asFeetPerSecond
import org.team2471.frc.lib.units.asInches
import org.team2471.frc.lib.units.asMeters
import org.team2471.frc.lib.units.asMetersPerSecond
import org.team2471.frc.lib.units.asRadiansPerSecond
import org.team2471.frc.lib.units.asRotation2d
import org.team2471.frc.lib.units.centimeters
import org.team2471.frc.lib.units.degrees
import org.team2471.frc.lib.units.degreesPerSecond
import org.team2471.frc.lib.units.inches
import org.team2471.frc.lib.units.meters
import org.team2471.frc.lib.units.metersPerSecond
import org.team2471.frc.lib.units.radiansPerSecond
import org.team2471.frc.lib.logging.LoopLogger
import org.team2471.frc.lib.commands.named
import org.team2471.frc.lib.commands.periodic
import org.team2471.frc.lib.commands.command
import org.team2471.frc.lib.commands.commandUnnamed
import org.team2471.frc.lib.commands.setDefaultCommandSafe
import org.team2471.frc.lib.ctre.ApplyModuleStates
import org.team2471.frc.lib.ctre.loggedTalonFX.LoggedTalonFX
import org.team2471.frc.lib.ctre.refreshAll
import org.team2471.frc.lib.ctre.setCANCoderAngle
import org.team2471.frc.lib.environment.isReal
import org.team2471.frc.lib.environment.isRedAlliance
import org.team2471.frc.lib.environment.isReplay
import org.team2471.frc.lib.environment.isSim
import org.team2471.frc.lib.logging.SimpleLogger
import org.team2471.frc.lib.math.translation
import org.team2471.frc.lib.units.Gs
import org.team2471.frc.lib.units.amps
import org.team2471.frc.lib.units.asMetersPerSecondCubed
import org.team2471.frc.lib.units.asMetersPerSecondPerSecond
import org.team2471.frc.lib.units.seconds
import org.team2471.frc.lib.units.wrap
import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.ctre.brakeMode
import org.team2471.frc.lib.ctre.coastMode
import org.team2471.frc.lib.energy.BatteryLogger
import org.team2471.frc.lib.units.amps
import org.team2471.frc.lib.units.asMetersPerSecondCubed
import org.team2471.frc.lib.units.asMetersPerSecondPerSecond
import org.team2471.frc.lib.units.pounds
import org.team2471.frc.lib.vision.QuixVisionSim
import org.wpilib.command3.Command
import org.wpilib.command3.Mechanism
import org.wpilib.driverstation.Alert
import org.wpilib.driverstation.DriverStationErrors
import org.wpilib.driverstation.RobotState
import org.wpilib.math.controller.PIDController
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.geometry.Rotation2d
import org.wpilib.math.geometry.Transform2d
import org.wpilib.math.geometry.Translation2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.math.kinematics.SwerveModulePosition
import org.wpilib.math.kinematics.SwerveModuleVelocity
import org.wpilib.system.Timer
import org.wpilib.units.LinearAccelerationUnit
import org.wpilib.units.measure.Angle
import org.wpilib.units.measure.AngularVelocity
import org.wpilib.units.measure.Distance
import org.wpilib.units.measure.LinearAcceleration
import org.wpilib.units.measure.LinearVelocity
import org.wpilib.units.measure.Time
import org.wpilib.units.measure.Velocity
import org.wpilib.units.measure.Voltage
import org.wpilib.util.Preferences
import kotlin.math.abs
import kotlin.math.min

/**
 * Mechanism to interface with the CTRE [SwerveDrivetrain]
 *
 * Implements abstract members to be overridden on a per-robot basis.
 */
abstract class SwerveDriveSubsystem(
    driveConstants: SwerveDrivetrainConstants,
    vararg val moduleConstants: SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration>
): SwerveDrivetrain<LoggedTalonFX, LoggedTalonFX, CANcoder>(
    { deviceId: Int, canbus: CANBus -> LoggedTalonFX(deviceId, canbus) },
    { deviceId: Int, canbus: CANBus -> LoggedTalonFX(deviceId, canbus) },
    { deviceId: Int, canbus: CANBus -> CANcoder(deviceId, canbus) },
    driveConstants,
    *moduleConstants
), Mechanism {

    /** Percentage of max speed to drive using the joysticks. */
    abstract fun getJoystickPercentageSpeed(): ChassisVelocities

    /** Autopilot limits velocity, acceleration, and jerk when driving to a point. It can also respect an approach angle.
     * Better alternative to [driveToPoint], use [driveToAutopilotPoint] instead. Use [createAPObject] to construct and configure an instance. */
    abstract val autoPilot: Autopilot

    /** Path following x error pid controller. Used in [driveAlongChoreoPath]. Error in meters -> added x velocity m/s. */
    abstract val pathXController: PIDController //= PIDController(7.0, 0.0, 0.0)
    /** Path following y error pid controller. Used in [driveAlongChoreoPath]. Error in meters -> added y velocity m/s. */
    abstract val pathYController: PIDController //= PIDController(7.0, 0.0, 0.0)
    /** Path following heading error pid controller. Used in [driveAlongChoreoPath]. Error in radians -> added rotational velocity rad/s. */
    abstract val pathThetaController: PIDController //= PIDController(7.0, 0.0, 0.0)

    /** [driveToPoint] pid controller in auto. Error in meters -> applied speed m/s. */
    abstract val autoDriveToPointController: PIDController //= PIDController(3.0, 0.0, 0.1)
    /** [driveToPoint] pid controller in teleop. Error in meters -> applied speed m/s. */
    abstract val teleopDriveToPointController: PIDController //= PIDController(3.0, 0.0, 0.1)

    /** [driveAtAngle] pid controller, used in anything that the robot automatically moves heading excluding path following. Error in radians -> applied rotational speed rad/s. */
    abstract val driveAtAnglePIDController: PhoenixPIDController //= PhoenixPIDController(7.7, 0.0, 0.072)

    /**
     * choreoPathsStartOnRed:
     * Initial value determines which side all choreo paths are made for.
     * False = all choreo paths are made on the blue side.
     * True = all choreo paths are made on the red side.
     */
    @get:AutoLogOutput(key = "Drive/Path/ChoreoPathsStartOnRed")
    abstract val choreoPathsStartOnRed: Boolean

    /**
     * Wheel odometry position of the robot. Use this for resetting position
     *
     * Abstract to allow for other pose sources (like cameras) to override the setter and to reset when this is set.
     */
    @get:AutoLogOutput(key = "Drive/Pose")
    abstract var pose: Pose2d

    /**
     * Odometry heading. Use this for resetting heading.
     *
     * Abstract to allow for other pose sources (like cameras) to override the setter and to reset when this is set.
     */
    abstract var heading: Rotation2d

    /**
     * Point on the robot where it rotates from.
     */
    abstract var centerOfRotation: Translation2d

    /** Use MapleSim to simulate the swerve or CTRE? */
    var useMapleSim: Boolean = false
    set(value) {
        field = value
        mapleSimDrivetrain = if (isReal || !useMapleSim) null else MapleSimCTRESwerveDrivetrain(150.0.pounds, 34.25.inches, 34.25.inches, pose, pigeon2.simState, *moduleConstants)
    }

    /** Stores information about the current state of the drivetrain */
    var savedState: SwerveDriveState = stateCopy
        private set

    /** Robot-centric velocity of drivetrain xy and heading. */
    val robotRelativeChassisVelocity: ChassisVelocities
        get() = savedState.Velocity

    /** Field-centric velocity of drivetrain xy and heading. */
    @get:AutoLogOutput(key = "Drive/State/Speeds")
    val chassisVelocities: ChassisVelocities
        get() = robotRelativeChassisVelocity.toFieldRelative(pose.rotation)

    /** Field-centric drivetrain xy velocity. */
    @get:AutoLogOutput(key = "Drive/State/Velocity")
    val velocity: Translation2d get() = chassisVelocities.translation

    /** Field-centric drivetrain xy acceleration. */
    @get:AutoLogOutput(key = "Drive/State/Acceleration")
    var acceleration = Translation2d(0.0, 0.0)
        private set

    /** Field-centric drivetrain xy jerk. */
    @get:AutoLogOutput(key = "Drive/State/Jerk")
    var jerk = Translation2d(0.0, 0.0)
        private set

    private var prevTime = -0.02

    /** Stores the velocity and angle of each swerve module. */
    @get:AutoLogOutput(key = "Drive/State/Modules/ModuleStates")
    val moduleStates: Array<SwerveModuleVelocity>
        get() = savedState.ModuleVelocities

    /** Stores the velocity and angle setpoints of each swerve module. */
    @get:AutoLogOutput(key = "Drive/State/Modules/ModuleTargets")
    val moduleTargets: Array<SwerveModuleVelocity>
        get() = savedState.ModuleTargets

    /** Stores the distance and angle of each swerve module. */
    @get:AutoLogOutput(key = "Drive/State/Modules/ModulePositions")
    val modulePositions: Array<SwerveModulePosition>
        get() = savedState.ModulePositions

    /** The raw heading of the robot, unaffected by vision updates and odometry resets. Not Wrapped */
    @get:AutoLogOutput(key = "Drive/State/RawHeading")
    val rawHeading: Rotation2d
        get() = savedState.RawHeading

        @get:AutoLogOutput(key = "Drive/State/Timestamp")
    val stateTimestamp: Double
        get() = savedState.Timestamp

    /** Loop frequency of the odometry thread. */
    @get:AutoLogOutput(key = "Drive/State/OdometryPeriod")
    val odometryPeriod: Double
        get() = savedState.OdometryPeriod

    /** Successful Data Acquisitions */
    @get:AutoLogOutput(key = "Drive/State/Daqs/SuccessfulDaqs")
    val successfulDaqs: Int
        get() = savedState.SuccessfulDaqs

    /** Failed Data Acquisitions */
    @get:AutoLogOutput(key = "Drive/State/Daqs/FailedDaqs")
    val failedDaqs: Int
        get() = savedState.FailedDaqs

    private val gyro: Pigeon2
        get() = pigeon2

    @get:AutoLogOutput(key = "Drive/Gyro/isConnected")
    val gyroConnected: Boolean
        get() = gyro.isConnected

    @get:AutoLogOutput(key = "Drive/Gyro/Yaw")
    val rawGyroYaw: Angle
        get() = BaseStatusSignal.getLatencyCompensatedValueAsDouble(gyro.yaw, gyro.angularVelocityZWorld).degrees.wrap()

    @get:AutoLogOutput(key = "Drive/Gyro/Pitch")
    val rawGyroPitch: Angle
        get() = BaseStatusSignal.getLatencyCompensatedValueAsDouble(gyro.pitch, gyro.angularVelocityXWorld).degrees.wrap()

    @get:AutoLogOutput(key = "Drive/Gyro/Roll")
    val rawGyroRoll: Angle
        get() = BaseStatusSignal.getLatencyCompensatedValueAsDouble(gyro.roll, gyro.angularVelocityYWorld).degrees.wrap()

    @get:AutoLogOutput(key = "Drive/Gyro/YawRate")
    val gyroYawRate: AngularVelocity
        get() = gyro.angularVelocityZWorld.valueAsDouble.degreesPerSecond
    @get:AutoLogOutput(key = "Drive/Gyro/PitchRate")
    val gyroPitchRate: AngularVelocity
        get() = gyro.angularVelocityXWorld.valueAsDouble.degreesPerSecond
    @get:AutoLogOutput(key = "Drive/Gyro/RollRate")
    val gyroRollRate: AngularVelocity
        get() = gyro.angularVelocityYWorld.valueAsDouble.degreesPerSecond

    @get:AutoLogOutput(key = "Drive/Gyro/AccelerationX")
    val gyroAccelerationX: LinearAcceleration
        get() = (gyro.accelerationX.valueAsDouble - gyro.gravityVectorX.valueAsDouble).Gs
    @get:AutoLogOutput(key = "Drive/Gyro/AccelerationY")
    val gyroAccelerationY: LinearAcceleration
        get() = (gyro.accelerationY.valueAsDouble - gyro.gravityVectorY.valueAsDouble).Gs

    @get:AutoLogOutput(key = "Drive/Gyro/yawLatency")
    val gyroLatency: Time
        get() = gyro.yaw.timestamp.latency.seconds

    @get:AutoLogOutput(key = "Drive/Gyro/supplyVoltage")
    val gyroVoltage: Voltage
        get() = gyro.supplyVoltage.value

    @get:AutoLogOutput(key = "Drive/Gyro/fault_BootIntoMotion")
    val gyroBootIntoMotionFault: Boolean
        get() = gyro.fault_BootIntoMotion.value

    @get:AutoLogOutput(key = "Drive/Gyro/fault_Undervoltage")
    val gyroUnderVoltageFault: Boolean
        get() = gyro.fault_Undervoltage.value


    /** Returns an array of module translations on the robot. */
    val moduleTranslations = moduleConstants.map { Translation2d(it.LocationX.meters, it.LocationY.meters) }.toTypedArray()

    /** Radius of swerve drivetrain */
    val driveBaseRadius = moduleTranslations.maxOf { it.norm.meters }

    /** The maximum translational speed of drivetrain. */
    val maxSpeed: LinearVelocity = moduleConstants.first().SpeedAt12Volts.metersPerSecond

    /** The maximum rotational speed of drivetrain. */
    val maxAngularSpeed: AngularVelocity = (maxSpeed.asMetersPerSecond / driveBaseRadius.asMeters).radiansPerSecond

    /**
     * Determines if the choreo paths should be flipped.
     * Dependent on [choreoPathsStartOnRed].
     *
     * true = paths need to be flipped
     * false = paths do not need to be flipped
     * */
    @get:AutoLogOutput(key = "Drive/Path/FlipChoreoPaths")
    val flipChoreoPaths
        get() = choreoPathsStartOnRed != isRedAlliance

    // ALERTS
    private val gyroDisconnectedAlert = Alert("Gyro Disconnected", Alert.Level.HIGH)
    private val driveDisconnectAlerts = Array(moduleConstants.size) { Alert("Module $it Drive Motor Disconnected", Alert.Level.HIGH) }
    private val steerDisconnectAlerts = Array(moduleConstants.size) { Alert("Module $it Steer Motor Disconnected", Alert.Level.HIGH) }
    private val encoderDisconnectAlerts = Array(moduleConstants.size) { Alert("Module $it Encoder Disconnected", Alert.Level.HIGH) }
    private var moduleErrorIndex = 0

    // OTHER

    // SWERVE REQUESTS

    /** Swerve request for driving using voltage out. XY𝞱-"power" (open loop) */
    private val fieldCentricVoltsDriveRequest = ApplyFieldVelocity()
        .withDriveRequestType(DriveRequestType.OpenLoopVoltage)
    /** Swerve request for driving using velocity PID. XY𝞱-velocity (closed loop)  */
    private val fieldCentricVelocityDriveRequest = ApplyFieldVelocity()
        .withDriveRequestType(DriveRequestType.Velocity)
    /** Swerve request for driving with velocity and PID-ing heading. XY-velocity 𝞱-position */
    private val driveAtAngleRequest = FieldCentricFacingAngle()
        .withDriveRequestType(DriveRequestType.Velocity)


    // Module STATUS SIGNALS
    private val steerCurrentStatusSignals = modules.map { it.steerMotor.supplyCurrent }.toTypedArray() // Current
    private val driveCurrentStatusSignals = modules.map { it.driveMotor.supplyCurrent }.toTypedArray()

    private val driveAccelerationStatusSignals = modules.map { it.driveMotor.acceleration }.toTypedArray()

    /** Refresh these status signals every periodic loop */
    private val statusSignalsToRefreshPeriodically = StatusSignalCollection(*steerCurrentStatusSignals, *driveCurrentStatusSignals)

    // INITIALIZATION

    private var ranPostInit = false

    init {
        println("SwerveDriveSubsystem Initialization")
        println("maxSpeed: ${maxSpeed.asFeetPerSecond.round(2)} f/s and maxAngularSpeed: ${maxAngularSpeed.asDegreesPerSecond.round(2)} deg/s")

        // Register the telemetry loop function to be called during the odometry thread.
        // This function runs every time new swerve odometry data gets received. Every 4ms-1ms (depending on CAN bus speed)
        registerTelemetry(::telemetryLoop)

        // Sets the default command. If null, skip it
        if (hasOverride("defaultCommand")) {
            setDefaultCommandSafe(defaultCommand())
        }

        /**
         * This is responsible for providing disconnect warnings, and more good things.
         * Designed to be run every robot loop cycle
         */
        addPeriodic {
            LoopLogger.record("SwerveDriveSubsystem periodic")
            statusSignalsToRefreshPeriodically.refreshAll() // Refresh Motor current data

            // Disabled actions
            if (RobotState.isDisabled()) {
                // Set module setpoints to their current position.
                if (!RobotState.isAutonomous()) {
                    setControl(ApplyModuleStates())
                }
                if (isReal) {
                    modules.forEach {
                        if (it.steerMotor.isConnected && it.encoder.isConnected) {
                            // Set steer motor to encoder position if it is not already there.
                            val encoderPosition = it.encoder.position.value
                            if ((it.steerMotor.position.value - encoderPosition).wrap().absoluteValue() > 0.5.degrees ) {
                                println("steer motor position: ${it.steerMotor.position.value}")
                                println("encoder position: ${it.encoder.position}")
                                it.steerMotor.setPosition(encoderPosition)
                            }
                        }
                    }
                }
            }


            if (!ranPostInit) {
                postInit()
            }


            // Power logging
            BatteryLogger.recordCurrent("Steer", steerCurrentStatusSignals.sumOf { it.valueAsDouble }.amps)
            BatteryLogger.recordCurrent("Drive", driveCurrentStatusSignals.sumOf { it.valueAsDouble }.amps)
            LoopLogger.record("SwerveDriveSubsystem periodic")
        }

        // Only runs in simulation. Update vision and swerve drive sim
        addSimulationPeriodic {
            LoopLogger.record("Drive Sim piodic")
            updateSimState(0.01, 12.0)
            if (mapleSimDrivetrain != null) {
                QuixVisionSim.updatePose(mapleSimDrivetrain!!.actualPoseInSimulationWorld)
                Logger.recordOutput("Drive/MapleSim/ActualPose", mapleSimDrivetrain!!.actualPoseInSimulationWorld)
            } else {
                QuixVisionSim.updatePose(pose)
            }
            LoopLogger.record("Drive Sim piodic")
        }

    }

    // Post init actions. Only runs once.
    // These actions aren't inside the standard init because they will crash the code by accessing abstract vars and applying them to method vars
    private fun postInit() {
        pathThetaController.enableContinuousInput(-Math.PI, Math.PI)
        driveAtAngleRequest.withHeadingPID(driveAtAnglePIDController.p, driveAtAnglePIDController.i, driveAtAnglePIDController.d)
        ranPostInit = true
    }

    // LOOPS

    open fun defaultCommand(): Command = idle()

    /**
     * Loop that is called every 10 ms (or less) during the odometry thread.
     * Updates [savedState] for up-to-date odometry information .
     */
    private fun telemetryLoop(state: SwerveDriveState) {
        val currTime = Timer.getMonotonicTimestamp()
        updateSavedState(state) // Refresh so we get current data

        // Calculate acceleration and jerk
        val prevAcceleration = acceleration
        val deltaTime = currTime - prevTime

        // Refresh drive motor acceleration data
        driveAccelerationStatusSignals.refreshAll()
        // To have accurate acceleration, we grab directly from the drive motor.
        acceleration = kinematics.toChassisVelocities(*moduleStates.mapIndexed { i, m -> m.apply {
            velocity = driveAccelerationStatusSignals[i].valueAsDouble * moduleConstants[i].DriveMotorGearRatio * moduleConstants[i].WheelRadius
        } }.toTypedArray()).translation

        jerk = ((acceleration - prevAcceleration) / deltaTime)

        val isGyroConnected = gyroConnected
        gyroDisconnectedAlert.set(!isGyroConnected)

        // Check if a part of any modules have been disconnected. Save on cycle time by only checking one module every loop.
        val module = modules[moduleErrorIndex]
        driveDisconnectAlerts[moduleErrorIndex].set(!module.driveMotor.isConnected)
        steerDisconnectAlerts[moduleErrorIndex].set(!module.steerMotor.isConnected)
        encoderDisconnectAlerts[moduleErrorIndex].set(!module.encoder.isConnected)
        moduleErrorIndex = (moduleErrorIndex + 1) % modules.size

        // Calculate heading from swerve odometry when gyro is disconnected. evil. this isn't reliable enough. Do lots of testing, disconnecting and recconecting gyro
//        if (!isGyroConnected && isReal) {
//            val deltaYaw = kinematics.toChassisSpeeds(*moduleStates).omegaRadiansPerSecond * deltaTime
//            resetRotation(heading + deltaYaw.radians.asRotation2d)
//        }

        prevTime = currTime
        //This errors only in replay
        if (!isReplay) SimpleLogger.recordOutput("Drive/State/TelemetryLoop", Timer.getMonotonicTimestamp() - currTime)
    }

    // STATE METHODS

    /**
     *  Refreshes the savedState to the current state of the swerve.
     *
     *  Sometimes takes a long time when data acquisitions fail, this is why it's not a getter.
     */
    fun updateSavedState(state: SwerveDriveState = stateCopy) {
        savedState = state
    }

    override fun resetTranslation(translation: Translation2d?) {
        super.resetTranslation(translation)
        mapleSimDrivetrain?.setSimulationWorldPose(Pose2d(translation, heading))
        updateSavedState() // Refresh state so we see an instant response.
    }

    override fun resetRotation(rotation: Rotation2d?) {
        super.resetRotation(rotation)
        mapleSimDrivetrain?.setSimulationWorldPose(Pose2d(pose.translation, rotation))
        updateSavedState() // Refresh state so we see an instant response.
    }

    override fun resetPose(pose2d: Pose2d) {
        // Intentionally not calling super.resetPose(). This allows for the custom setters on heading and pose to trigger.
        resetTranslation(pose2d.translation)
        heading = pose2d.rotation
    }

    /**
     * Sets the [heading] to be 180 or 0 degrees depending on the current alliance.
     */
    fun zeroGyro() {
        val wantedAngle = (if (isRedAlliance) 180.0.degrees else 0.0.degrees).asRotation2d
        println("zero gyro isRedAlliance  ${isRedAlliance} zeroing to ${wantedAngle.degrees} degrees")
        heading = wantedAngle
        println("heading: ${heading}")
    }

    /**
     * Set the steering module offsets to the current position of the module. This "zeros" the steer motors.
     *
     * Usually each module bevel gear should face to the right (with perspective to robot) before running this.
     */
    fun setAngleOffsets() = command("SetAngleOffsets",this) {
        println("setting angle offsets")
        val offsets = modules.map { it.encoder.setCANCoderAngle(0.0.degrees) }
        offsets.forEachIndexed { i, offset ->
            Preferences.setDouble("Module $i Offset", offset.asDegrees)
        }
    }

    // CONTROL METHODS

    /**
     * Runs the drive at the desired velocity. Field centric
     * @param velocity Speeds in meters/sec
     */
    fun driveVelocity(velocity: ChassisVelocities) {
//        println("drive velocity")
        SimpleLogger.recordOutput("Drive/Wanted ChassisSpeeds", velocity.toRobotRelative(heading))
        setControl(
            fieldCentricVelocityDriveRequest
                .withVelocity(velocity)
                .withCenterOfRotation(centerOfRotation)
        )
    }

    /**
     * Runs the drive at the desired voltage. Field centric
     * @param velocityInVolts Speeds in volts
     */
    fun driveVoltage(velocityInVolts: ChassisVelocities) { // TODO Volts units wrong. Fix
        setControl(
            fieldCentricVoltsDriveRequest
                .withVelocity(ChassisVelocities(
                    velocityInVolts.vx / 12.0 * maxSpeed.asMetersPerSecond ,
                    velocityInVolts.vy / 12.0 * maxSpeed.asMetersPerSecond,
                    velocityInVolts.omega / 12.0 * maxAngularSpeed.asRadiansPerSecond,
                ))
                .withCenterOfRotation(centerOfRotation)
        )
    }

    /**
     * Runs the drive at the desired percentage. Field centric
     * @param velocityInPercentage Speeds in percentage of full power
     */
    fun drivePercentage(velocityInPercentage: ChassisVelocities) {
        driveVoltage(velocityInPercentage * 12.0)
    }

    /**
     * Set the swerve drive modules to point inward in an "X" fashion.
     */
    fun xPose() = setControl(SwerveDriveBrake())

    /**
     * Applies a 0v output to the drivetrain.
     */
    fun stop() = driveVoltage(ChassisVelocities())

    /**
     * Set all the drive and steer motors to brake mode.
     */
    fun brakeMode() {
        modules.forEach {
            it.steerMotor.brakeMode()
            it.driveMotor.brakeMode()
        }
    }

    /**
     * Set all the drive and steer motors to coast mode.
     */
    fun coastMode() {
        modules.forEach {
            it.steerMotor.coastMode()
            it.driveMotor.coastMode()
        }
    }

    /**
     * Returns the wanted chassis speeds from the joystick.
     * @see getJoystickPercentageSpeed
     * @see maxSpeed
     */
    fun getChassisVelocitiesFromJoystick(): ChassisVelocities = getJoystickPercentageSpeed().apply {
        vx *= maxSpeed.asMetersPerSecond
        vy *= maxSpeed.asMetersPerSecond
        omega *= maxAngularSpeed.asRadiansPerSecond
    }

    // All of these driveAtAngle function variations exist to make syntax good when calling the function
    fun driveAtAngle(angle: Rotation2d) = driveAtAngle { angle }
    fun driveAtAngle(angle: () -> Rotation2d) = driveAtAngle(angle) { getChassisVelocitiesFromJoystick().translation }
    fun driveAtAngle(
        angle: () -> Rotation2d,
        translation: () -> Translation2d = { getChassisVelocitiesFromJoystick().translation }
    ) = driveAtAngle(angle(), translation())

    /**
     * Uses the [driveAtAnglePIDController] to drive the robot with a field-centric angle and translation.
     */
    fun driveAtAngle(angle: Rotation2d, translation: Translation2d) {
        SimpleLogger.recordOutput("Drive/DriveAtAngle/Angle", angle)
        SimpleLogger.recordOutput("Drive/DriveAtAngle/Translation", translation)
        setControl(
            driveAtAngleRequest
                .withVelocityX(translation.x)
                .withVelocityY(translation.y)
                .withTargetDirection(angle)
                .withCenterOfRotation(centerOfRotation)
        )
    }

    // COMMANDS

    /**
     * Drives the robot using the joystick. [getChassisVelocitiesFromJoystick]
     */
    fun joystickVelocityDrive(): Command = command("joystickVelocityDrive", this) {
        this.periodic {
            if (!RobotState.isAutonomous()) driveVelocity(getChassisVelocitiesFromJoystick())
        }
    }

    fun joystickPercentageDrive(): Command = command ("joystickPercentageDrive", this) {
        periodic {
            if (!RobotState.isAutonomous()) drivePercentage(getJoystickPercentageSpeed())
        }
    }

    /**
     * Translates the robot using the joystick, does not turn. [getChassisVelocitiesFromJoystick]
     */
    fun joystickOnlyTranslationDrive(): Command {
        return run {
            driveVelocity(getChassisVelocitiesFromJoystick().apply { omega = 0.0 })
        }.named("JoystickOnlyTranslationDrive")
    }


    fun driveToPoint(
        wantedPose: Pose2d,
        poseSupplier: () -> Pose2d = { pose },
        exitSupplier: (Distance, Angle) -> Boolean = { error, headingError -> error < 0.75.inches && headingError < 1.0.degrees },
        maxVelocity: LinearVelocity = maxSpeed
    ): Command = driveToPoint({ wantedPose }, poseSupplier, exitSupplier, maxVelocity)

    /**
     * Drives the robot to a [wantedPose]. Uses the [autoDriveToPointController] or [teleopDriveToPointController]
     *
     * @param wantedPose The pose to drive to
     * @param poseSupplier A function that returns the pose of the robot. The default value is the swerve odometry.
     * @param exitSupplier A function that returns true if the command should abort. The default value ends when the robot is within 0.75 meters of the target.
     * @param maxVelocity The maximum velocity of the robot. The default value is [maxSpeed] from constants.
     */
    fun driveToPoint(
        wantedPose: () -> Pose2d,
        poseSupplier: () -> Pose2d = { pose },
        exitSupplier: (Distance, Angle) -> Boolean = { error, headingError -> error < 0.75.inches && headingError < 1.0.degrees },
        maxVelocity: LinearVelocity = maxSpeed
    ): Command = commandUnnamed(this) {
        println("running driveToPoint")
        while (true) {
            // Calculate pose error
            val translationToPose = wantedPose().translation.minus(poseSupplier().translation)
            val distanceError = translationToPose.norm.meters
            val headingError = (wantedPose().rotation - poseSupplier().rotation).measure.absoluteValue()

            // Log errors
            SimpleLogger.recordOutput("Drive/DriveToPoint/DistanceErrorM", distanceError.asMeters)
            SimpleLogger.recordOutput("Drive/DriveToPoint/HeadingErrorD", headingError.asDegrees)
            SimpleLogger.recordOutput("Drive/DriveToPoint/Point", wantedPose())

            if (exitSupplier(distanceError, headingError)) {
                println("stopping driveToPoint. Distance error ${distanceError.asInches.round(2)}in. Heading error ${headingError.asDegrees.round(2)}deg.")
                break
            } else {
                val pidController = if (RobotState.isAutonomous()) autoDriveToPointController else teleopDriveToPointController
                val velocityOutput = min(abs(pidController.calculate(distanceError.asMeters, 0.0)), maxVelocity.asMetersPerSecond)
                val wantedVelocity = translationToPose.normalize() * velocityOutput
                driveAtAngle(wantedPose().rotation, wantedVelocity)
            }
            yield()
        }

        stop() // Stop driving
        SimpleLogger.recordOutput("Drive/DriveToPoint/Point", Pose2d())
    }.named("DriveToPoint")

    fun driveToAutopilotPoint(
        wantedPose: Pose2d,
        poseSupplier: () -> Pose2d = { pose },
        entryAngleSupplier: () -> Angle? = { null },
        autopilotSupplier: Autopilot = autoPilot,
        earlyExit: (Pose2d, APTarget) -> Boolean = { robotPose, target -> autopilotSupplier.atTarget(robotPose, target)  }
    ) = driveToAutopilotPoint({ wantedPose }, poseSupplier, entryAngleSupplier, autopilotSupplier, earlyExit)

    /**
     * Drives the robot to a [wantedPose] using [Autopilot]. Uses [autoPilot] to control the robot.
     *
     * Finishes when the [Autopilot.atTarget] method returns true.
     *
     * @param wantedPose The position to drive to
     * @param poseSupplier A function that returns the pose of the robot. The default value is the swerve odometry.
     * @param entryAngleSupplier The angle [Autopilot] will try to enter the wantedPose at. The default value is null.
     */
    fun driveToAutopilotPoint(
        wantedPose: () -> Pose2d,
        poseSupplier: () -> Pose2d = { pose },
        entryAngleSupplier: () -> Angle? = { null },
        autopilotSupplier: Autopilot = autoPilot,
        exitSupplier: (Pose2d, APTarget) -> Boolean = { robotPose, target -> autopilotSupplier.atTarget(robotPose, target) }
    ): Command = commandUnnamed(this) {
        println("running driveToAutopilotPoint")
        while (true) {
            val pose = poseSupplier()
            val targetPose = wantedPose()
            val entryAngle = entryAngleSupplier()
            val target = if (entryAngle != null) {
                APTarget(targetPose).withEntryAngle(entryAngle.asRotation2d)
            } else {
                APTarget(targetPose)
            }

            // Exit or Continue
            if (exitSupplier(pose, target)) {
                println("Stopping driveToAutopilotPoint error meters/rad: ${pose - targetPose}")
                break
            } else {
                // Calculate Output
                val output = autopilotSupplier.calculate(pose, robotRelativeChassisVelocity, target)
                val velocity = Translation2d(output.vx.asMetersPerSecond, output.vy.asMetersPerSecond)

                driveAtAngle(output.targetAngle(), velocity)

                SimpleLogger.recordOutput("Drive/AutoPilot/Velocity", velocity.norm)
                SimpleLogger.recordOutput("Drive/AutoPilot/Target", targetPose)
            }

            yield()
        }

        stop()
        SimpleLogger.recordOutput("Drive/AutoPilot/Target", Pose2d())
    }.named("DriveToAutopilotPoint")


    /**
     * Drives the robot to the closest point along a line but also lets the driver control the robot along it. Uses the [autoDriveToPointController] or [teleopDriveToPointController]
     *
     * @param pointOne The first line endpoint.
     * @param pointTwo The second line endpoint.
     * @param heading The wanted heading of the robot to align to. The default value doesn't restrict the heading and allows joystick rotation control.
     * @param poseSupplier A function that returns the pose of the robot. The default value is the swerve odometry.
     * @param maxVelocity The maximum velocity of the robot. The default value is [maxSpeed] from constants.
     *
     * @see driveToPoint
     */
    fun joystickDriveAlongLine(
        pointOne: Translation2d,
        pointTwo: Translation2d,
        heading: Rotation2d? = null,
        poseSupplier: () -> Pose2d = { pose },
        lineTolerance: Distance = 0.5.inches,
        maxVelocity: LinearVelocity = maxSpeed
    ): Command = commandUnnamed(this) {
        val lineAngle = (pointTwo - pointOne).angle

        while (true) {
            val currentPose = poseSupplier()
            val linePoint = findClosestPointOnLine(pointOne, pointTwo, currentPose.translation)
            val translationToPose = linePoint.minus(currentPose.translation)
            val distanceToPose = translationToPose.norm.deadband(lineTolerance.asMeters)
            val driveToPointPower = min(abs(teleopDriveToPointController.calculate(distanceToPose, 0.0)), maxVelocity.asMetersPerSecond)

            val driveToPointVelocity = translationToPose.normalize() * driveToPointPower
            val teleopChassisSpeeds = getChassisVelocitiesFromJoystick().apply {
                // Limit joystick speeds to be along the line
                val modifiedTranslation = translation.rotateBy(-lineAngle)
                val lineCentricTranslation = Translation2d(modifiedTranslation.x, 0.0).rotateBy(lineAngle)
                vx = lineCentricTranslation.x
                vy = lineCentricTranslation.y
            }

            SimpleLogger.recordOutput("Drive/AlongLine/line", *arrayOf(pointOne, pointTwo))
            SimpleLogger.recordOutput("Drive/AlongLine/closestPoint", linePoint.toPose2d())
            SimpleLogger.recordOutput("Drive/AlongLine/translation2Pose", translationToPose.toPose2d())


            if (heading == null) {
                val driveToPointChassisSpeeds = ChassisVelocities(driveToPointVelocity.x, driveToPointVelocity.y, 0.0)
                val wantedChassisSpeeds = driveToPointChassisSpeeds + teleopChassisSpeeds
                driveVelocity(wantedChassisSpeeds)
            } else {
                val wantedVelocity = teleopChassisSpeeds.translation + driveToPointVelocity
                driveAtAngle(heading, wantedVelocity)
            }

            yield()
        }
    }.named("JoystickDriveAlongLine") {
        stop()
        SimpleLogger.recordOutput("Drive/AlongLine/line", *arrayOf<Translation2d>())
        SimpleLogger.recordOutput("Drive/AlongLine/closestPoint", Pose2d())
    }


    /**
     * Drives the robot to the closest point along a line specified by [pointOne] and [pointTwo]
     *
     * @param pointOne The first line endpoint.
     * @param pointTwo The second line endpoint.
     * @param heading The wanted heading of the robot to align to.
     * @param poseSupplier A function that returns the pose of the robot. The default value is the swerve odometry.
     * @param maxVelocity The maximum velocity of the robot. The default value is [maxSpeed] from constants.
     *
     * @see driveToPoint
     */
    fun driveToLine(
        pointOne: Translation2d,
        pointTwo: Translation2d,
        heading: Rotation2d,
        poseSupplier: () -> Pose2d = { pose },
        exitSupplier: ((Distance, Angle) -> Boolean)? = null,
        maxVelocity: LinearVelocity = maxSpeed
    ) = commandUnnamed(this) {
        println("running driveToLine")
        val closestPoseOnLine = findClosestPointOnLine(pointOne, pointTwo, poseSupplier().translation).toPose2d(heading)
        SimpleLogger.recordOutput("Drive/ToPointOnLine/Points", *arrayOf(pointOne, pointTwo))
        SimpleLogger.recordOutput("Drive/ToPointOnLine/ClosestPose", closestPoseOnLine)

        if (exitSupplier == null) {
            await(driveToPoint(closestPoseOnLine, poseSupplier, maxVelocity = maxVelocity))
        } else {
            await(driveToPoint(closestPoseOnLine, poseSupplier, exitSupplier, maxVelocity))
        }

        SimpleLogger.recordOutput("Drive/ToPointOnLine/Points", *arrayOf<Translation2d>())
        SimpleLogger.recordOutput("Drive/ToPointOnLine/ClosestPose", *arrayOf<Pose2d>())
    }.named("DriveToLine")

    /**
     * Drives the robot along a path from Choreo. Uses the [pathXController], [pathYController], and [pathThetaController] to control the robot.
     *
     * @param path The path to drive along
     * @param poseSupplier A function that returns the pose of the robot. The default value is the swerve odometry.
     * @param resetOdometry Whether to reset the odometry to the start of the path. The default value is false.
     * @param exitSupplier A function that returns true if the command should abort. The default value ends when the path duration finishes.
     */
    fun driveAlongChoreoPath(
        path: Trajectory<SwerveSample>,
        poseSupplier: () -> Pose2d = ::pose,
        resetOdometry: Boolean = false,
        exitSupplier: (Double, Transform2d) -> Boolean = { percentage, error -> percentage >= 1.0 }
    ): Command = command("DriveAlongChoreoPath", this) {
//        println("Running DriveAlongChoreoPath") //TODO: UNCOMMENT IN 2027 CHOREO
//
//        val totalTime = path.totalTime
//        val applyFieldSpeedsRequest = ApplyFieldSpeeds().withDriveRequestType(SwerveModule.DriveRequestType.Velocity)
//        val timer = Timer()
//        timer.start()
//
//        if (resetOdometry) {
//            pose = path.getInitialPose(flipChoreoPaths).get()
//            val pose = pose
//            println("Resetting odometry. (${pose.translation.x}, ${pose.translation.y}, ${pose.rotation.degrees})")
//        }
//
//        Logger.recordOutput("Drive/Path/Name", path.name())
//        Logger.recordOutput("Drive/Path/TotalTime", totalTime)
//
//        while (true) {
//            val t = min(timer.get() + 0.02, totalTime) //added 0.02 to start moving 1 frame faster
//            LoopLogger.record("DriveAlongPath time")
//            val percentComplete = t / totalTime
//            val currentPose = poseSupplier()
//            LoopLogger.record("DriveAlongPath poseSupplier")
//            val sample = path.sampleAt(t, flipChoreoPaths).get()
//            LoopLogger.record("DriveAlongPath sampleAt")
//            val wantedPose = sample.pose
//            val error = wantedPose - currentPose
//            LoopLogger.record("DriveAlongPath pathInfo")
//
//            Logger.recordOutput("Drive/Path/Done %", percentComplete)
//
//            // Exit Path?
//            if (exitSupplier(percentComplete, error)) {
//                println("Finished driveAlongChoreoPath at ${(t / totalTime * 100.0).round(2)}% done")
//                break
//            } else {
//                val wantedSpeeds = sample.chassisSpeeds
//                val moduleForcesX = sample.moduleForcesX()
//                val moduleForcesY = sample.moduleForcesY()
//
//                // Add heading and xy error
//                wantedSpeeds.apply {
//                    vx += pathXController.calculate(currentPose.x, wantedPose.x)
//                    vy += pathYController.calculate(currentPose.y, wantedPose.y)
//                    omega += pathThetaController.calculate(currentPose.rotation.radians, sample.heading)
//                }
//                LoopLogger.record("DriveAlongPath pid")
//                setControl(
//                    applyFieldSpeedsRequest
//                        .withSpeeds(wantedSpeeds)
//                        .withWheelForceFeedforwardsX(moduleForcesX)
//                        .withWheelForceFeedforwardsY(moduleForcesY)
//                        .withCenterOfRotation(centerOfRotation)
//                )
//                LoopLogger.record("DriveAlongPath setControl")
//
//                Logger.recordOutput("Drive/Path/Time", t)
//                Logger.recordOutput("Drive/Path/Pose", wantedPose)
//                Logger.recordOutput("Drive/Path/Speeds", sample.chassisSpeeds)
//                Logger.recordOutput("Drive/Path/AppliedSpeeds", wantedSpeeds)
//                Logger.recordOutput("Drive/Path/Path Acceleration", hypot(sample.ax, sample.ay))
////                Logger.recordOutput("Drive/Path/Module Forces X", moduleForcesX)
////                Logger.recordOutput("Drive/Path/Module Forces Y", moduleForcesY)
////                Logger.recordOutput("Drive/Path/Pose Error", (wantedPose - currentPose).translation.norm.meters)
//                LoopLogger.record("DriveAlongPath logger")
//            }
//
//            yield()
//        }
//
//        val finalSample = path.getFinalSample(flipChoreoPaths).getOrNull()
//        // Are we stopping?
//        if (finalSample != null) {
////            println("final sample ${finalSample.chassisSpeeds.translation.norm.round(2)} m/s")
//            setControl(
//                ApplyFieldSpeeds().apply {
//                    Speeds = finalSample.chassisSpeeds
//                    DriveRequestType = SwerveModule.DriveRequestType.Velocity
//                }
//            )
//        } else {
//            // Tell drivetrain to apply no output
//            stop()
//        }
//
//        // Publish empty data to show that the path is done
//        Logger.recordOutput("Drive/Path/Pose", Pose2d())
    }

    // OTHER

    /**
     * Simple constructor for creating an [Autopilot] object.
     *
     * @param maxVelocity The maximum velocity Autopilot will allow. meters/sec
     * @param maxAcceleration The maximum acceleration Autopilot will allow. meters/sec^2
     * @param maxJerk The maximum jerk Autopilot will allow. meters/sec^3
     * @param xyTolerance The xy translation tolerance for the robot to be at the target position, effects [Autopilot.atTarget]. Meters
     * @param thetaTolerance The theta rotation tolerance for the robot to be at the target position, effects [Autopilot.atTarget]. Radians
     * @param beelineRadius The beeline radius is a distance where, under that range, an entry angle is no longer respected. Default value 8 cm
     *
     * @see Autopilot
     * @see APConstraints
     * @see APProfile
     */
    fun createAPObject(
        maxVelocity: LinearVelocity,
        maxAcceleration: LinearAcceleration,
        maxJerk: Velocity<LinearAccelerationUnit>,
        xyTolerance: Distance,
        thetaTolerance: Angle,
        beelineRadius: Distance = 8.0.centimeters
    ): Autopilot {
        return Autopilot(APProfile(APConstraints(maxVelocity.asMetersPerSecond, maxAcceleration.asMetersPerSecondPerSecond, maxJerk.asMetersPerSecondCubed))
            .withErrorXY(xyTolerance).withErrorTheta(thetaTolerance).withBeelineRadius(beelineRadius)
        )
    }

    // SysID Routines

    /** Used to find drive motor PID and SVA constants. */
//    private val translationSysIdRoutine = SysIdRoutine(
//        SysIdRoutine.Config(
//            null,
//            7.0.volts,
//            2.0.seconds
//        ) { state: SysIdRoutineLog.State ->
//            SignalLogger.writeString("SysIdTranslation_State", state.toString())
//            Logger.recordOutput("SysIdTranslation_State", state.toString())
//          },
//        Mechanism({ output: Voltage ->
//            modules.forEachIndexed { i, m ->
//                Logger.recordOutput("SysID/Translation/motor${i}Position", m.driveMotor.position.valueAsDouble + (Math.random() - 0.5) * 0.0001)
//                Logger.recordOutput("SysID/Translation/motor${i}Velocity", m.driveMotor.velocity.valueAsDouble + (Math.random() - 0.5) * 0.0001)
//                Logger.recordOutput("SysID/Translation/motor${i}Volts Applied", m.driveMotor.motorVoltage.valueAsDouble + (Math.random() - 0.5) * 0.0001)
//            }
//            setControl(SysIdSwerveTranslation().withVolts(output))
//        }, null, this)
//    )
//    /** Used to find [driveAtAnglePIDController] PID values. */
//    private val rotationSysIdRoutine = SysIdRoutine(
//        SysIdRoutine.Config(
//            Units.Volts.of(Math.PI / 6).per(Units.Second),
//            Math.PI.volts,
//            5.0.seconds
//        ) { state: SysIdRoutineLog.State ->
//            SignalLogger.writeString("SysIdRotation_State", state.toString())
//            Logger.recordOutput("SysIdRotation_State", state.toString())
//          },
//        Mechanism({ output: Voltage ->
//            /* output is actually radians per second, but SysId only supports "volts" */
//            setControl(SysIdSwerveRotation().withRotationalRate(output.asVolts))
//            /* also log the requested output for SysId */
//            // Adding randomness because values need to constantly be updating for sysid to pick up new samples
//            SignalLogger.writeDouble("Rotational_Rate", output.asVolts + (Math.random() - 0.5) * 0.0001)
//            Logger.recordOutput("Rotational_Rate", output.asVolts + (Math.random() - 0.5) * 0.0001)
//            Logger.recordOutput("SysID/Rotation/yaw", gyro.yaw.valueAsDouble + (Math.random() - 0.5) * 0.0001)
//            Logger.recordOutput("SysID/Rotation/yawRate", gyro.angularVelocityZWorld.valueAsDouble + (Math.random() - 0.5) * 0.0001)
//        }, null, this)
//    )
//    /** Used to find steer motor PID and SVA constants. */
//    private val steerSysIdRoutine = SysIdRoutine(
//        SysIdRoutine.Config(
//            null,
//            7.0.volts,
//            null
//        ) { state: SysIdRoutineLog.State ->
//            SignalLogger.writeString("SysIdSteer_State", state.toString())
//            Logger.recordOutput("SysIdSteer_State", state.toString())
//          },
//        Mechanism({ volts: Voltage? -> setControl(SysIdSwerveSteerGains().withVolts(volts)) }, null, this)
//    )
//
//    // SysID Commands
//
//    fun sysIDTranslationDynamic(direction: SysIdRoutine.Direction): Command = translationSysIdRoutine.dynamic(direction).beforeWait(1.0)
//    fun sysIDTranslationQuasistatic(direction: SysIdRoutine.Direction): Command = translationSysIdRoutine.quasistatic(direction).beforeWait(1.0)
//    fun sysIDRotationDynamic(direction: SysIdRoutine.Direction): Command = rotationSysIdRoutine.dynamic(direction).beforeWait(1.0)
//    fun sysIDRotationQuasistatic(direction: SysIdRoutine.Direction): Command = rotationSysIdRoutine.quasistatic(direction).beforeWait(1.0)
//    fun sysIDSteerDynamic(direction: SysIdRoutine.Direction): Command = steerSysIdRoutine.dynamic(direction).beforeWait(1.0)
//    fun sysIDSteerQuasistatic(direction: SysIdRoutine.Direction): Command = steerSysIdRoutine.quasistatic(direction).beforeWait(1.0)
//
//    // Full SysID test commands
//    fun sysIDTranslationAll() = sequenceCommand(
//        sysIDTranslationQuasistatic(SysIdRoutine.Direction.kForward),
//        sysIDTranslationQuasistatic(SysIdRoutine.Direction.kReverse),
//        sysIDTranslationDynamic(SysIdRoutine.Direction.kForward),
//        sysIDTranslationDynamic(SysIdRoutine.Direction.kReverse)
//    )
//    fun sysIDRotationAll() = sequenceCommand(
//        sysIDRotationQuasistatic(SysIdRoutine.Direction.kForward),
//        sysIDRotationQuasistatic(SysIdRoutine.Direction.kReverse),
//        sysIDRotationDynamic(SysIdRoutine.Direction.kForward),
//        sysIDRotationDynamic(SysIdRoutine.Direction.kReverse),
//    )
//    fun sysIDSteerAll() = sequenceCommand(
//        sysIDSteerQuasistatic(SysIdRoutine.Direction.kForward),
//        sysIDSteerQuasistatic(SysIdRoutine.Direction.kReverse),
//        sysIDSteerDynamic(SysIdRoutine.Direction.kForward),
//        sysIDSteerDynamic(SysIdRoutine.Direction.kReverse),
//    )

    // SIM
    var mapleSimDrivetrain: MapleSimCTRESwerveDrivetrain? = null//if (isReal || !useMapleSim) null else MapleSimCTRESwerveDrivetrain(150.0.pounds, 34.25.inches, 34.25.inches, pose, pigeon2.simState, *moduleConstants)

    /** Must be called periodically during sim for swerve sim to work */
    override fun updateSimState(dtSeconds: Double, supplyVoltage: Double) {
        if (isSim) {
            if (mapleSimDrivetrain != null) {
                mapleSimDrivetrain!!.updateCTRE(dtSeconds, supplyVoltage.volts, *modules)
            } else {
                super.updateSimState(dtSeconds, supplyVoltage)
            }
        } else {
            DriverStationErrors.reportError("DriveIOCTRE.updateSim() called while robot is real", true)
            throw Error("DriveIOCTRE.updateSim() called while robot is real")
        }
    }

    // OTHER

    private fun hasOverride(methodName: String): Boolean {
        val method = javaClass.getMethod(methodName)
        return method.declaringClass != MechanismBase::class.java
    }
}