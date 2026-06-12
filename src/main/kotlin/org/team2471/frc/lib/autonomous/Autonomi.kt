package org.team2471.frc.lib.autonomous

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.asSeconds
import org.wpilib.command3.Command
import org.wpilib.math.geometry.Pose2d
import org.wpilib.math.kinematics.ChassisVelocities
import org.wpilib.system.Filesystem
import org.wpilib.system.RobotController
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name

/** Manages robot driving paths and auto commands */
abstract class Autonomi {
    /** All Choreo paths */
    val paths: MutableMap<String, Trajectory<SwerveSample>> = findChoreoPaths()

    /** List of all the auto routines */
    abstract val autos: List<AutoRoutine>
    /** List of all the test routines */
    abstract val tests: List<TestRoutine>

    /** Set the drive pose to the starting pose of the selected auto. Abstract so it can change per robot */
    abstract val drivePoseSetter: (Pose2d) -> Unit

    /** Runs when the selected auto changes. Used for warming up a path following command or any other pre-auto tasks. */
    abstract val warmupFunction: () -> Unit

    init {
        println("Autonomi init")
        // Read all the paths on init
        readAutoPaths()
    }

    /** Convert an AutoRoutine to an AutoOpMode. Uses the [drivePoseSetter] and [warmupFunction] from Autonomi to set up the auto OpMode. */
    fun AutoRoutine.toAutoOpMode(): AutoOpMode {
        return AutoOpMode(
            this.name,
            this.command,
            { readAutoPaths(); warmupFunction() },
            { this.startingPositionSupplier?.invoke()?.let { drivePoseSetter(it) }; this.disabledPeriodicFunction?.invoke() })
    }

    /** Convert a TestRoutine to a TestOpMode. */
    fun TestRoutine.toTestOpMode(): TestOpMode {
        return TestOpMode(this.name, this.command) { this.initFunction.invoke() }
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
}

/**
 * A class to store an auto command and data to be built into an auto OpMode.
 *
 * @param name The name of the auto
 * @param command The command to run when the auto OpMode runs
 * @param startingPositionSupplier The position to periodically set the robot to when the auto is selected.
 * @param disabledPeriodicFunction A function that runs periodically when the auto is disabled
 */
class AutoRoutine(val name: String, val command: Command, val startingPositionSupplier: (() -> Pose2d)? = null, val disabledPeriodicFunction: (() -> Unit)? = null)

/**
 * A class to store a test command to be built into a test OpMode.
 *
 * @param name The name of the test
 * @param command The command to run when the test OpMode runs
 * @param initFunction A function that runs when the test is selected. Useful for binding buttons or overriding Mechanism default commands that only persist while the test is selected.
 */
class TestRoutine(val name: String, val command: Command, val initFunction: () -> Unit = {})