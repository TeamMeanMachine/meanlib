package org.team2471.frc.lib.framework

import edu.wpi.first.hal.DriverStationJNI
import edu.wpi.first.wpilibj.DSControlWord
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.Timer.delay
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.team2471.frc.lib.coroutines.MeanlibDispatcher
import org.team2471.frc.lib.framework.internal.akitLoggers.MeanLoggedRobot

private val m_word = DSControlWord()

/**
 * The core robot program to run. The methods in this interface can be overridden in order to
 * execute code in the specified mode.
 */
abstract class LoggedMeanlibRobot : MeanLoggedRobot() {
    val mainSubsystem = Subsystem("Robot").apply { enable() }

    @OptIn(DelicateCoroutinesApi::class)
    override fun robotInit() {
        init()

        var wasConnected = false


        GlobalScope.launch {
            while (true) {
                delay(0.01)
                DriverStation.refreshData()

                m_word.refresh()
                val isConnected = m_word.isDSAttached

                Events.process()

                if (isConnected && !wasConnected) { //gained connection
                    comms()
                }

                DriverStationJNI.observeUserProgramTest()

                wasConnected = isConnected
            }
        }
    }


    override fun autonomousInit() {
        DriverStationJNI.observeUserProgramAutonomous()
        GlobalScope.launch(MeanlibDispatcher) {
            use(mainSubsystem, name = "Autonomous") {
                enable()
                autonomous()
            }
        }
    }

    override fun teleopInit() {
//        DriverStationJNI.observeUserProgramTeleop()
        GlobalScope.launch(MeanlibDispatcher) {
            use(mainSubsystem, name = "Teleop") {
                enable()
                teleop()
            }
        }
    }

    override fun disabledInit() {
//        DriverStationJNI.observeUserProgramDisabled()
        GlobalScope.launch(MeanlibDispatcher) {
            use(mainSubsystem, name = "Disabled") { disable() }
        }
    }

    override fun testInit() {
//        DriverStationJNI.observeUserProgramTest()
        GlobalScope.launch(MeanlibDispatcher) {
            use(mainSubsystem, name = "Test") {
                enable()
                test()
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
        // Warn the user that autonomous isn't impemented
        println("The autonomous mode was called without being overridden, consider overriding it to enable this mode.")
    }

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
}
