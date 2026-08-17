package org.team2471.frc.lib.autonomous

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import org.team2471.frc.lib.autonomous.auto.AutoOpMode
import org.team2471.frc.lib.autonomous.auto.AutoOpModeSupplier
import org.team2471.frc.lib.autonomous.auto.AutoRoutine
import org.team2471.frc.lib.autonomous.test.TestOpMode
import org.team2471.frc.lib.autonomous.test.TestOpModeSupplier
import org.team2471.frc.lib.autonomous.test.TestRoutine
import org.team2471.frc.lib.math.round
import org.team2471.frc.lib.units.asSeconds
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

    // PATHS

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

    /** Converts an [AutoRoutine] to a [AutoOpModeSupplier]. Uses the [drivePoseSetter] and [warmupFunction] from Autonomi to set up the [org.team2471.frc.lib.autonomous.auto.AutoOpMode]. */
    fun createOpModeSupplier(autoRoutine: AutoRoutine): AutoOpModeSupplier =
        AutoOpModeSupplier(
            autoRoutine.name
        ) {
            AutoOpMode(
                autoRoutine.name,
                autoRoutine.command,
                { this.readAutoPaths(); this.warmupFunction() },
                { autoRoutine.startingPositionSupplier?.invoke()?.let { drivePoseSetter(it) }; autoRoutine.disabledPeriodicFunction?.invoke() }
        )
    }

    /** Convert a [TestRoutine] to a function that returns a [org.team2471.frc.lib.autonomous.test.TestOpMode]. */
    fun createOpModeSupplier(testRoutine: TestRoutine): TestOpModeSupplier =
        TestOpModeSupplier(
            testRoutine.name
        ) {
            TestOpMode(
                testRoutine.name,
                testRoutine.command,
                testRoutine.initFunction
            )
        }

    /** Maps a list of [AutoRoutine]s to a list of [AutoOpModeSupplier]. For clean syntax*/
    fun List<AutoRoutine>.toAutoOpModeSuppliers(): List<AutoOpModeSupplier> = map { createOpModeSupplier(it) }
    /** Maps a list of [TestRoutine]s to a list of functions that return a [TestOpMode]. for clean syntax*/
    fun List<TestRoutine>.toTestOpModeSuppliers(): List<TestOpModeSupplier> = map { createOpModeSupplier(it) }
}