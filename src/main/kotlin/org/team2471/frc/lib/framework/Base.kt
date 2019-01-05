package org.team2471.frc.lib.framework

import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.*
import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibScope
import org.team2471.frc.lib.coroutines.parallel
import org.team2471.frc.lib.framework.internal.InputMapper
import org.team2471.frc.lib.util.Environment
import java.io.File

const val LANGUAGE_KOTLIN = 6

interface RobotProgram {
    suspend fun autonomous() { /* NOOP */ }

    suspend fun teleop() { /* NOOP */ }

    suspend fun test() { /* NOOP */ }

    suspend fun enable() { /* NOOP */}

    suspend fun disable() { /* NOOP */ }
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

    // Report our robot's language as Java
    HAL.report(FRCNetComm.tResourceType.kResourceType_Language, LANGUAGE_KOTLIN)

    if (Environment.isReal) {
        File("/tmp/frc_versions/FRC_Lib_Version.ini").writeText("Java ${WPILibVersion.Version}")
    }

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

        val wasDisabled = previousRobotMode == RobotMode.DISABLED

        if (previousRobotMode != RobotMode.AUTONOMOUS && ds.isAutonomous) {
            HAL.observeUserProgramAutonomous()
            previousRobotMode = RobotMode.AUTONOMOUS

            MeanlibScope.launch {
                use(mainSubsystem) {
                    if (wasDisabled) {
                        parallel({ robotProgram.enable() }, { robotProgram.autonomous() })
                    } else {
                        robotProgram.autonomous()
                    }
                }
            }
        } else if (previousRobotMode != RobotMode.TELEOP && ds.isOperatorControl) {
            HAL.observeUserProgramTeleop()
            previousRobotMode = RobotMode.TELEOP

            MeanlibScope.launch {
                use(mainSubsystem) {
                    if (wasDisabled) {
                        parallel({ robotProgram.enable() }, { robotProgram.teleop() })
                    } else {
                        robotProgram.teleop()
                    }
                }
            }
        } else if (previousRobotMode != RobotMode.TEST && ds.isTest) {
            HAL.observeUserProgramTest()
            previousRobotMode = RobotMode.TEST

            MeanlibScope.launch {
                use(mainSubsystem) {
                    if (wasDisabled) {
                        parallel({ robotProgram.enable() }, { robotProgram.test() })
                    } else {
                        robotProgram.autonomous()
                    }
                }
            }
        }
    }
}
