package org.team2471.frc.lib.framework

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.coroutines.periodic
import java.io.File
import java.sql.Driver

const val LANGUAGE_KOTLIN = 6

/**
 * The core robot program to run. The methods in this interface can be overridden in order to
 * execute code in the specified mode.
 */
abstract class MeanlibRobot : RobotBase() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun startCompetition() {
        init()

        HAL.observeUserProgramStarting()

        var previousRobotMode: RobotMode? = null

        val mainSubsystem = Subsystem("Robot").apply { enable() }

        while (true) {
            val hasNewData = DriverStation.waitForData(0.02)

            Events.process()
            if (!DriverStation.isDSAttached()) previousRobotMode = RobotMode.DISCONNECTED

            if (!hasNewData) continue

            if (previousRobotMode == null || previousRobotMode == RobotMode.DISCONNECTED) {
                comms()

                if (previousRobotMode != null) previousRobotMode = RobotMode.DISABLED
            }

            if (DriverStation.isDisabled()) {
                if (previousRobotMode != RobotMode.DISABLED) {
                    HAL.observeUserProgramDisabled()
                    previousRobotMode = RobotMode.DISABLED

                    GlobalScope.launch(MeanlibDispatcher) {
                        use(mainSubsystem, name = "Disabled") { disable() }
                    }
                }
                continue
            }

            val wasDisabled = previousRobotMode == RobotMode.DISABLED || previousRobotMode == null

            if (previousRobotMode != RobotMode.AUTONOMOUS && DriverStation.isAutonomous()) {
                HAL.observeUserProgramAutonomous()
                previousRobotMode = RobotMode.AUTONOMOUS
                GlobalScope.launch(MeanlibDispatcher) {
                    use(mainSubsystem, name = "Autonomous") {
                        if (wasDisabled) enable()
                        autonomous()
                    }
                }
            } else if (previousRobotMode != RobotMode.TELEOP && DriverStation.isTeleop()) {
                HAL.observeUserProgramTeleop()
                previousRobotMode = RobotMode.TELEOP
                GlobalScope.launch(MeanlibDispatcher) {
                    use(mainSubsystem, name = "Teleop") {
                        if (wasDisabled) enable()
                        teleop()
                    }
                }
            } else if (previousRobotMode != RobotMode.TEST && DriverStation.isTest()) {
                HAL.observeUserProgramTest()
                previousRobotMode = RobotMode.TEST

                GlobalScope.launch(MeanlibDispatcher) {
                    use(mainSubsystem, name = "Test") {
                        if (wasDisabled) enable()
                        test()
                    }
                }
            }
        }
    }

    override fun endCompetition() { /* NOOP */ }

    /**
     * Robot-wide initialization code should go here.
     *
     * Users should override this method for default Robot-wide initialization which will be called
     * when the robot is first powered on. It will be called exactly one time.
     *
     * Warning: the Driver Station "Robot Code" light and FMS "Robot Ready" indicators will be off
     * until RobotInit() exits. Code in RobotInit() that waits for enable will cause the robot to
     * never indicate that the code is ready, causing the robot to be bypassed in a match.
     */
    open fun init() { /* NOOP */ }

    /**
     * Called immediately when the robot becomes enabled. This method must exit before [autonomous],
     * [teleop] or [test] will be called.
     */
    open suspend fun enable() { /* NOOP */}

    /**
     * Called immediately when the robot becomes disabled.
     */
    open suspend fun disable() { /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to autonomous.
     */
    open suspend fun autonomous() {
    println("started autonomous")
    /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to teleoperated.
     */
    open suspend fun teleop() { /* NOOP */ }

    /**
     * Called immediately after [enable] when the robot's mode transitions to test.
     */
    open suspend fun test() { /* NOOP */ }

    /**
     * Called every time communications are established between the robot and the driver station.
     * This method can be used to make use of functions that require communication with the driver
     * station, e.g. [DriverStation.getAlliance] or [DriverStation.getMatchType]. Note that data
     * from the driver station may not be immediately available and may need to be rechecked.
     */
    open fun comms() { /* NOOP */ }
}

private enum class RobotMode {
    DISCONNECTED,
    DISABLED,
    AUTONOMOUS,
    TELEOP,
    TEST,
}