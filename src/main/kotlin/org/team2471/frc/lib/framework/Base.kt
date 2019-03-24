package org.team2471.frc.lib.framework

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import java.io.File

const val LANGUAGE_KOTLIN = 6

/**
 * The core robot program to run. The methods in this interface can be overridden in order to
 * execute code in the specified mode.
 */
interface RobotProgram {
    /**
     * Called immediately when the robot becomes enabled. This method must exit before [autonomous],
     * [teleop] or [test] will be called.
     */
    suspend fun enable() { /* NOOP */}

    /**
     * Called immediately when the robot becomes disabled.
     */
    suspend fun disable() { /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to autonomous.
     */
    suspend fun autonomous() { /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to teleoperated.
     */
    suspend fun teleop() { /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to test.
     */
    suspend fun test() { /* NOOP */ }

    /**
     * Called every time communications are established between the robot and the driver station.
     * This method can be used to make use of functions that require communication with the driver
     * station, e.g. [DriverStation.getAlliance] or [DriverStation.getMatchType]. Note that data
     * from the driver station may not be immediately available and may need to be rechecked.
     */
    fun comms() { /* NOOP */ }
}

private enum class RobotMode {
    DISCONNECTED,
    DISABLED,
    AUTONOMOUS,
    TELEOP,
    TEST,
}

/**
 * Initializes the HAL and core WPILib features, including the NetworkTables server and versioning.
 */
fun initializeWpilib() {
    // set up network tables
    val ntInstance = NetworkTableInstance.getDefault()
    ntInstance.setNetworkIdentity("Robot")
    ntInstance.startServer("/home/lvuser/networktables.ini")

    // initialize hardware configuration
    check(HAL.initialize(500, 0)) { "Failed to initialize. Terminating." }

    // Report our robot's language as Java
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, LANGUAGE_KOTLIN)

    if (RobotBase.isReal()) {
        File("/tmp/frc_versions/FRC_Lib_Version.ini").writeText("Java ${WPILibVersion.Version}")
    }

    println("wpilib initialized successfully.")
}


/**
 * Runs a given [robotProgram].
 */
fun runRobotProgram(robotProgram: RobotProgram): Nothing {
    println("********** Robot program starting! **********")

    HAL.observeUserProgramStarting()
    val ds = DriverStation.getInstance()

    var previousRobotMode: RobotMode? = null

    val mainSubsystem = Subsystem("Robot").apply { enable() }

    while (true) {
        val hasNewData = ds.waitForData(0.02)

        Events.process()
        if (!ds.isDSAttached) previousRobotMode = RobotMode.DISCONNECTED

        if (!hasNewData) continue

        if (previousRobotMode == null || previousRobotMode == RobotMode.DISCONNECTED) {
            robotProgram.comms()

            if (previousRobotMode != null) previousRobotMode = RobotMode.DISABLED
        }

        if (ds.isDisabled) {
            if (previousRobotMode != RobotMode.DISABLED) {
                HAL.observeUserProgramDisabled()
                previousRobotMode = RobotMode.DISABLED

                GlobalScope.launch(MeanlibDispatcher) {
                    use(mainSubsystem, name = "Disabled") { robotProgram.disable() }
                }
            }
            continue
        }

        val wasDisabled = previousRobotMode == RobotMode.DISABLED || previousRobotMode == null

        if (previousRobotMode != RobotMode.AUTONOMOUS && ds.isAutonomous) {
            HAL.observeUserProgramAutonomous()
            previousRobotMode = RobotMode.AUTONOMOUS

            GlobalScope.launch(MeanlibDispatcher) {
                use(mainSubsystem, name = "Autonomous") {
                    if (wasDisabled) robotProgram.enable()
                    robotProgram.autonomous()
                }
            }
        } else if (previousRobotMode != RobotMode.TELEOP && ds.isOperatorControl) {
            HAL.observeUserProgramTeleop()
            previousRobotMode = RobotMode.TELEOP

            GlobalScope.launch(MeanlibDispatcher) {
                use(mainSubsystem, name = "Teleop") {
                    if (wasDisabled) robotProgram.enable()
                    robotProgram.teleop()
                }
            }
        } else if (previousRobotMode != RobotMode.TEST && ds.isTest) {
            HAL.observeUserProgramTest()
            previousRobotMode = RobotMode.TEST

            GlobalScope.launch(MeanlibDispatcher) {
                use(mainSubsystem, name = "Test") {
                    if (wasDisabled) robotProgram.enable()
                    robotProgram.test()
                }
            }
        }
    }
}
