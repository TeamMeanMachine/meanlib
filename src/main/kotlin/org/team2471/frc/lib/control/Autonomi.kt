package org.team2471.frc.lib.control

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import org.team2471.frc.lib.commands.use
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.asSeconds
import org.team2471.frc.lib.util.demoMode
import org.wpilib.command3.Command
import org.wpilib.command3.Scheduler
import org.wpilib.driverstation.DriverStation
import org.wpilib.driverstation.internal.DriverStationBackend
import org.wpilib.hardware.hal.OpModeOption
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.opmode.OpMode
import org.wpilib.opmode.PeriodicOpMode
import org.wpilib.smartdashboard.SendableChooser
import org.wpilib.system.Filesystem
import org.wpilib.system.RobotController
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/** Manages robot driving paths and auto commands */
abstract class Autonomi {
    /** All Choreo paths */
    val paths: MutableMap<String, Trajectory<SwerveSample>> = findChoreoPaths()

    /** Chooser for selecting autonomous commands */
    abstract val autoChooser: SendableChooser<AutoCommand>
    /** Chooser for selecting test commands */
    abstract val testChooser: SendableChooser<Command>

    abstract val autos: List<AutoCommand>

    /** Set the drive pose to the starting pose of the selected auto. Abstract so it can change per robot */
    abstract val drivePoseSetter: (Pose2d) -> Unit

    /** Runs when the selected auto changes. Used for warming up a path following command or any other pre-auto tasks. */
    abstract val warmupFunction: () -> Unit

    /** The currently selected AutoCommand (Command + starting pose) */
    var selectedAuto: AutoCommand? = null
        private set
    /** The currently selected auto command */
    val autonomousCommand: Command? get() = if (!demoMode) selectedAuto?.command else use("DemoModeAuto") { println("DEMO MODE: Not running auto, no killing kids today.") }
    /** The currently selected test command */
    val testCommand: Command? get() = testChooser.selected

    init {
        // Read all the paths on init
        readAutoPaths()
    }

    /** Refreshes the selectedAuto var if the chooser has changed. Call this during disabled periodic to save on auto init time.  */
    fun updateSelectedAuto(continuouslySetPosition: Boolean = false) {
        val startTime = RobotController.getMeasureMonotonicTime()
        val newAuto = autoChooser.selected
        if (selectedAuto != newAuto) {
            selectedAuto = autoChooser.selected
            println("selected auto changed ${autoChooser.selected}")
            println("Auto is ${autonomousCommand?.name()}")
            setDrivePositionToAutoStartPose()
            readAutoPaths()
            println("finished reading auto in ${(RobotController.getMeasureMonotonicTime() - startTime).asSeconds} seconds")
            warmupFunction()
        }
        autonomousCommand?.name()
        if (continuouslySetPosition) {
            setDrivePositionToAutoStartPose(true)
        }
    }

    /** Set the drive pose to the starting pose of the selected auto. */
    fun setDrivePositionToAutoStartPose(hidePrint: Boolean = false) {
        val startingPose = selectedAuto?.resetPosition?.invoke()
        if (startingPose != null) {
            if (!hidePrint) println("resetting drive pose to auto start pose")
            drivePoseSetter(startingPose) // Set robot pose
        }
    }

    /** Warm up the paths in the JVM by reading them (May speed up path execution time) */
    fun readAutoPaths() {
        val startTime = RobotController.getMeasureMonotonicTime()
        val pathNameAndStartPose = mutableListOf<Pair<String, Pose2d>>()
        val segments = mutableListOf<ChassisVelocities?>()
        paths.forEach {
//            pathNameAndStartPose.add(Pair(
//                it.value.name(),
//                it.value.sampleAt(0.0, true).get().pose
//            ))
//            val pathSegment = it.value.totalTime / 10.0
//            for (i in 0..10) {
//                segments.add(it.value.sampleAt(i * pathSegment, true).getOrNull()?.chassisSpeeds)
//            }
            //TODO: UNCOMMENT WHEN CHOREO UPDATES TO 2027
        }
        println("paths: ${pathNameAndStartPose.map { it.first }}")
        println("reading ${paths.size} paths and ${segments.size} samples. Took ${(RobotController.getMeasureMonotonicTime() - startTime).asSeconds.round(4)} seconds.")
    }

    /** Find all the paths in the choreo directory and return a list of them. */
    fun findChoreoPaths(): MutableMap<String, Trajectory<SwerveSample>> {
        return try {
            val map: MutableMap<String, Trajectory<SwerveSample>> = mutableMapOf()
            Filesystem.getDeployDirectory().toPath().resolve("choreo").listDirectoryEntries("*.traj").forEach {
                try {
                    val name = it.name.removeSuffix(".traj")
//                    val traj = Choreo.loadTrajectory(name).getOrNull()
//                    if (traj != null) {
//                        @Suppress("UNCHECKED_CAST")
//                        map[name] = traj as Trajectory<SwerveSample>
//                    }
                    //TODO: UNCOMMENT WHEN CHOREO UPDATES TO 2027
                } catch (e: Exception) {
                    println("failed to load path at $it")
                    println(e)
                }
            }
            println("loaded ${map.size} paths")
            map
        } catch (e: Exception) {
            println("failed to load any auto paths $e"); mutableMapOf()
        }
    }

    /** Pair of an auto command and its starting pose */
    class AutoCommand(val name: String, val command: Command, val resetPosition: (() -> Pose2d)? = null)

    class AutoOpMode(val name: String, val autoCommand: Command, val resetPosition: (() -> Pose2d)? = null, warmupFunction: () -> Unit, val resetPositionLoop: () -> Unit): PeriodicOpMode() {

        init {
            println("inside $name auto init")
//            Autonomous.warmupDriveAlongPath()
            warmupFunction()
            println("finished warmup")

            addPeriodic({}, 0.1)
        }

        override fun disabledPeriodic() {
            resetPositionLoop()
        }

        override fun start() {
            println("starting $name auto")
            Scheduler.getDefault().schedule(autoCommand)
            println("Scheduled auto")
        }

        override fun end() {
            println("ending $name auto")
            Scheduler.getDefault().cancel(autoCommand)
            println("Cancelled auto")
        }
    }
}

