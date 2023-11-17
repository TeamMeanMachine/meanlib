package org.team2471.frc.lib.framework

import edu.wpi.first.hal.DriverStationJNI
import edu.wpi.first.wpilibj.DSControlWord
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.Timer.delay
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher

const val LANGUAGE_KOTLIN = 6

private val m_word = DSControlWord()

/**
 * The core robot program to run. The methods in this interface can be overridden in order to
 * execute code in the specified mode.
 */
abstract class MeanlibRobot : RobotBase() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun startCompetition() {
        init()
        DriverStationJNI.observeUserProgramStarting()

        var previousRobotMode: RobotMode? = null
        var wasConnected = false

        val mainSubsystem = Subsystem("Robot").apply { enable() }

        while (true) {
            delay(0.01)
            DriverStation.refreshData()

            m_word.refresh()
            val isConnected = m_word.isDSAttached

            // Get current mode
            var mode: RobotMode? = null
            if (m_word.isDisabled) {
                mode = RobotMode.DISABLED
            } else if (m_word.isAutonomous) {
                mode = RobotMode.AUTONOMOUS
            } else if (m_word.isTeleop) {
                mode = RobotMode.TELEOP
            } else if (m_word.isTest) {
                mode = RobotMode.TEST
            } else if (isSimulation()) {
                mode = RobotMode.SIMULATE
            }
            val modeChanged = (mode != previousRobotMode)
            val lostConnection = (!isConnected && wasConnected)
            val gainedConnection = (isConnected && !wasConnected)

//            val hasNewData = DriverStation.waitForData(0.02)

            Events.process()

            if (gainedConnection) {
                comms()
            }
            if (modeChanged || lostConnection) {
                if (mode == RobotMode.DISABLED || lostConnection) {
                    if (previousRobotMode != RobotMode.DISABLED) {
                        mode = RobotMode.DISABLED
                        DriverStationJNI.observeUserProgramDisabled()
                        GlobalScope.launch(MeanlibDispatcher) {
                            use(mainSubsystem, name = "Disabled") { disable() }
                        }
                    }
                } else {
                    val wasDisabled = previousRobotMode == RobotMode.DISABLED || previousRobotMode == null

                    if (mode == RobotMode.AUTONOMOUS) {
                        DriverStationJNI.observeUserProgramAutonomous()
                        GlobalScope.launch(MeanlibDispatcher) {
                            use(mainSubsystem, name = "Autonomous") {
                                if (wasDisabled) enable()
                                autonomous()
                            }
                        }
                    } else if (mode == RobotMode.TELEOP) {
                        DriverStationJNI.observeUserProgramTeleop()
                        GlobalScope.launch(MeanlibDispatcher) {
                            use(mainSubsystem, name = "Teleop") {
                                if (wasDisabled) enable()
                                teleop()
                            }
                        }
                    } else if (mode == RobotMode.TEST) {
                        DriverStationJNI.observeUserProgramTest()
                        GlobalScope.launch(MeanlibDispatcher) {
                            use(mainSubsystem, name = "Test") {
                                if (wasDisabled) enable()
                                test()
                            }
                        }
                    } else if (mode == RobotMode.SIMULATE) {
//                        DriverStationJNI.observeUserProgramTest()
                        GlobalScope.launch(MeanlibDispatcher) {
                            use(mainSubsystem, name = "Simulate") {
                                if (wasDisabled) enable()
                                simulate()
                            }
                        }
                    }
                }
            }
            wasConnected = isConnected
            previousRobotMode = mode
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
     * Called immediately after [enable] when the robot's mode transitions to simulate.
     */
    open suspend fun simulate() { /* NOOP */ }

    /**
     * Called every time communications are established between the robot and the driver station.
     * This method can be used to make use of functions that require communication with the driver
     * station, e.g. [DriverStation.getAlliance] or [DriverStation.getMatchType]. Note that data
     * from the driver station may not be immediately available and may need to be rechecked.
     */
    open fun comms() { /* NOOP */ }

    @Override
    fun simulationPeriodic() {
        println("in simulationPeriodic (spam)")
    }
}

private enum class RobotMode {
    DISABLED,
    AUTONOMOUS,
    TELEOP,
    TEST,
    SIMULATE,
}
