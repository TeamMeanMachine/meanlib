package org.team2471.frc.lib.control

import choreo.Choreo
import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.wpilibj.Filesystem
import edu.wpi.first.wpilibj.RobotController
import org.littletonrobotics.junction.networktables.LoggedDashboardChooser
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.asSeconds
import org.team2471.frc.lib.util.demoMode
import org.wpilib.commands3.Command
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.jvm.optionals.getOrNull

/** Manages robot driving paths and auto commands */
abstract class Autonomi {
    /** All Choreo paths */
    val paths: MutableMap<String, Trajectory<SwerveSample>> = findChoreoPaths()

    /** Chooser for selecting autonomous commands */
    abstract val autoChooser: LoggedDashboardChooser<AutoCommand?>
    /** Chooser for selecting test commands */
    abstract val testChooser: LoggedDashboardChooser<Command?>

    /** Set the drive pose to the starting pose of the selected auto. Abstract so it can change per robot */
    abstract val drivePoseSetter: (Pose2d) -> Unit

    /** Runs when the selected auto changes. Used for warming up a path following command or any other pre-auto tasks. */
    abstract val warmupFunction: () -> Unit

    /** The currently selected AutoCommand (Command + starting pose) */
    var selectedAuto: AutoCommand? = null
        private set
    /** The currently selected auto command */
    val autonomousCommand: Command? get() = if (!demoMode) selectedAuto?.command else ({ println("DEMO MODE: Not running auto, no killing kids today.") }).toCommand()
    /** The currently selected test command */
    val testCommand: Command? get() = testChooser.get()

    init {
        // Read all the paths on init
        readAutoPaths()
    }

    /** Refreshes the selectedAuto var if the chooser has changed. Call this during disabled periodic to save on auto init time.  */
    fun updateSelectedAuto(continuouslySetPosition: Boolean = false) {
        val startTime = RobotController.getMeasureFPGATime()
        val newAuto = autoChooser.get()
        if (selectedAuto != newAuto) {
            selectedAuto = autoChooser.get()
            println("selected auto changed ${autoChooser.sendableChooser.selected}")
            println("Auto is ${autonomousCommand?.name()}")
            setDrivePositionToAutoStartPose()
            readAutoPaths()
            println("finished reading auto in ${(RobotController.getMeasureFPGATime() - startTime).asSeconds} seconds")
            warmupFunction()
        }
        autonomousCommand?.name()
        if (continuouslySetPosition) {
            setDrivePositionToAutoStartPose(true)
        }
    }

    /** Set the drive pose to the starting pose of the selected auto. */
    fun setDrivePositionToAutoStartPose(hidePrint: Boolean = false) {
        val startingPose = selectedAuto?.startingPoseSupplier?.invoke()
        if (startingPose != null) {
            if (!hidePrint) println("resetting drive pose to auto start pose")
            drivePoseSetter(startingPose) // Set robot pose
        }
    }

    /** Warm up the paths in the JVM by reading them (May speed up path execution time) */
    fun readAutoPaths() {
        val startTime = RobotController.getMeasureFPGATime()
        val pathNameAndStartPose = mutableListOf<Pair<String, Pose2d>>()
        val segments = mutableListOf<ChassisSpeeds?>()
        paths.forEach {
            pathNameAndStartPose.add(Pair(
                it.value.name(),
                it.value.sampleAt(0.0, true).get().pose
            ))
            val pathSegment = it.value.totalTime / 10.0
            for (i in 0..10) {
                segments.add(it.value.sampleAt(i * pathSegment, true).getOrNull()?.chassisSpeeds)
            }
        }
        println("paths: ${pathNameAndStartPose.map { it.first }}")
        println("reading ${paths.size} paths and ${segments.size} samples. Took ${(RobotController.getMeasureFPGATime() - startTime).asSeconds.round(4)} seconds.")
    }

    /** Find all the paths in the choreo directory and return a list of them. */
    fun findChoreoPaths(): MutableMap<String, Trajectory<SwerveSample>> {
        return try {
            val map: MutableMap<String, Trajectory<SwerveSample>> = mutableMapOf()
            Filesystem.getDeployDirectory().toPath().resolve("choreo").listDirectoryEntries("*.traj").forEach {
                try {
                    val name = it.name.removeSuffix(".traj")
                    val traj = Choreo.loadTrajectory(name).getOrNull()
                    if (traj != null) {
                        @Suppress("UNCHECKED_CAST")
                        map[name] = traj as Trajectory<SwerveSample>
                    }
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
    class AutoCommand(val command: Command, val startingPoseSupplier: (() -> Pose2d)? = null)
}