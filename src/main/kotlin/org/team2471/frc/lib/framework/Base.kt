package org.team2471.frc.lib.framework

import edu.wpi.first.hal.FRCNetComm
import edu.wpi.first.hal.HAL
import edu.wpi.first.networktables.NetworkTableInstance
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.util.WPILibVersion
import kotlinx.coroutines.GlobalScope
import org.team2471.frc.lib.coroutines.meanlibLaunch
import org.team2471.frc.lib.coroutines.periodic
import java.io.File
import kotlin.reflect.KFunction0

const val LANGUAGE_KOTLIN = 6

/**
 * The core robot program to run. The methods in this interface can be overridden in order to
 * execute code in the specified mode.
 */
abstract class MeanlibRobot : RobotBase() {
    override fun startCompetition() {
        init()

        HAL.observeUserProgramStarting()

        val ds = DriverStation.getInstance()

        var previousRobotMode: RobotMode? = null

        val mainSubsystem = Subsystem("Robot").apply { enable() }

        while (true) {
            Events.process()

            val hasNewData = ds.waitForData(0.02)

            if (!hasNewData) continue

            if (!ds.isDSAttached) previousRobotMode = RobotMode.DISCONNECTED

            if (previousRobotMode == null || previousRobotMode == RobotMode.DISCONNECTED) {
                comms()

                if (previousRobotMode != null) previousRobotMode = RobotMode.DISABLED
            }

            val current = RobotMode.current
            if (previousRobotMode != current) {
                current.HalObserveUserProgram()
                val wasDisabled = previousRobotMode == RobotMode.DISABLED
                GlobalScope.meanlibLaunch {
                    use(mainSubsystem, name = current.name) {
                        if (wasDisabled) enable()
                        current.action.invoke(this@MeanlibRobot)
                    }
                }
                previousRobotMode = RobotMode.current
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
    open suspend fun autonomous() { /* NOOP */ }

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

private enum class RobotMode(val action: suspend MeanlibRobot.() -> Unit, val HalObserveUserProgram: () -> Unit) {
    DISCONNECTED({ comms() }, {}),
    DISABLED(MeanlibRobot::disable, HAL::observeUserProgramDisabled),
    AUTONOMOUS(MeanlibRobot::autonomous, HAL::observeUserProgramAutonomous),
    TELEOP(MeanlibRobot::teleop, HAL::observeUserProgramTeleop),
    TEST(MeanlibRobot::test, HAL::observeUserProgramTest);

    companion object {
        val current: RobotMode
            get() {
                val ds = DriverStation.getInstance()
                return when {
                    ds.isDisabled -> DISABLED
                    ds.isAutonomous -> AUTONOMOUS
                    ds.isOperatorControl -> TELEOP
                    ds.isTest -> TEST
                    !ds.isDSAttached -> DISCONNECTED
                    else -> DISCONNECTED
                }
            }
    }
}
