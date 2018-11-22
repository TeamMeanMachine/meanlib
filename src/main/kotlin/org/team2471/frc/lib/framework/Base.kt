package org.team2471.frc.lib.framework

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.*
import edu.wpi.first.wpilibj.hal.FRCNetComm
import edu.wpi.first.wpilibj.hal.HAL
import edu.wpi.first.wpilibj.internal.HardwareHLUsageReporting
import edu.wpi.first.wpilibj.internal.HardwareTimer
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.framework.internal.InputMapper
import java.io.File

interface RobotProgram {
    suspend fun autonomous()

    suspend fun teleop()

    suspend fun disable()

    suspend fun test()
}

private enum class RobotMode {
    DISABLED,
    AUTONOMOUS,
    TELEOP,
    TEST,
}

fun initializeWpilib() {
    // set up network tables
    val ntInstance = NetworkTableInstance.getDefault()
    ntInstance.setNetworkIdentity("Robot")
    ntInstance.startServer("/home/lvuser/networktables.ini")

    // initialize hardware configuration
    check(HAL.initialize(500, 0)) { "Failed to initialize. Terminating." }

    // Set some implementations so that the static methods work properly
    Timer.SetImplementation(HardwareTimer())
    HLUsageReporting.SetImplementation(HardwareHLUsageReporting())
    setDefaultRobotStateImplementation()

    // Report our robot's language as Java
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, FRCNetComm.tInstances.kLanguage_Java)

    // wpilib's RobotBase does this for some reason
    File("/tmp/frc_versions/FRC_Lib_Version.ini").writeText("Java ${WPILibVersion.Version}")

    println("wpilib initialized successfully.")
}

fun runRobotProgram(robotProgram: RobotProgram): Nothing {
    println("********** Robot program starting! **********")

    HAL.observeUserProgramStarting()
    val ds = DriverStation.getInstance()

    var previousRobotMode: RobotMode? = null

    val mainSubsystem = Subsystem("Robot")

    while (true) {
        ds.waitForData()

        if (ds.isDisabled) {
            if (previousRobotMode != RobotMode.DISABLED) {
                HAL.observeUserProgramDisabled()
                previousRobotMode = RobotMode.DISABLED

                MeanlibScope.launch {
                    use(mainSubsystem) { robotProgram.disable() }
                }
            }
            continue
        }

        // process joystick inputs
        InputMapper.process()

        if (previousRobotMode != RobotMode.AUTONOMOUS && ds.isAutonomous) {
            HAL.observeUserProgramAutonomous()
            previousRobotMode = RobotMode.AUTONOMOUS

            MeanlibScope.launch {
                use(mainSubsystem) { robotProgram.autonomous() }
            }
        } else if (previousRobotMode != RobotMode.TELEOP && ds.isOperatorControl) {
            HAL.observeUserProgramTeleop()
            previousRobotMode = RobotMode.TELEOP

            MeanlibScope.launch {
                use(mainSubsystem) { robotProgram.teleop() }
            }
        } else if (previousRobotMode != RobotMode.TEST && ds.isTest) {
            HAL.observeUserProgramTest()
            previousRobotMode = RobotMode.TEST

            MeanlibScope.launch {
                use(mainSubsystem) { robotProgram.test() }
            }
        }
    }
}
