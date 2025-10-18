// Copyright (c) 2025 FRC 6328
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.
package org.team2471.frc.lib.swerve

import edu.wpi.first.math.geometry.Rotation2d
import edu.wpi.first.math.geometry.Translation2d
import edu.wpi.first.math.geometry.Twist2d
import edu.wpi.first.math.kinematics.ChassisSpeeds
import edu.wpi.first.math.kinematics.SwerveDriveKinematics
import edu.wpi.first.math.kinematics.SwerveModuleState
import org.team2471.frc.lib.math.epsilonEquals
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.indices
import kotlin.math.*

/**
 * "Inspired" by FRC team 254. See the license file in the root directory of this project.
 *
 *
 * Takes a prior setpoint (ChassisSpeeds), a desired setpoint (from a driver, or from a path
 * follower), and outputs a new setpoint that respects all of the kinematic constraints on module
 * rotation speed and wheel velocity/acceleration. By generating a new setpoint every iteration, the
 * robot will converge to the desired setpoint quickly while avoiding any intermediate state that is
 * kinematically infeasible (and can result in wheel slip or robot heading drift as a result).
 */
class SwerveSetpointGenerator(val kinematics: SwerveDriveKinematics, val moduleLocations: Array<Translation2d>, val moduleLimits: ModuleLimits) {

    data class SwerveSetpoint(val chassisSpeeds: ChassisSpeeds, val moduleStates: Array<SwerveModuleState>)
    data class ModuleLimits(val maxDriveVelocity: Double, val maxDriveAcceleration: Double, val maxSteeringVelocity: Double)


    /**
     * Check if it would be faster to go to the opposite of the goal heading (and reverse drive
     * direction).
     *
     * @param prevToGoal The rotation from the previous state to the goal state (i.e.
     * prev.inverse().rotateBy(goal)).
     * @return True if the shortest path to achieve this rotation involves flipping the drive
     * direction.
     */
    private fun flipHeading(prevToGoal: Rotation2d): Boolean {
        return abs(prevToGoal.radians) > Math.PI / 2.0
    }

    private fun unwrapAngle(ref: Double, angle: Double): Double {
        val diff = angle - ref
        return if (diff > Math.PI) {
            angle - 2.0 * Math.PI
        } else if (diff < -Math.PI) {
            angle + 2.0 * Math.PI
        } else {
            angle
        }
    }

    private fun interface Function2d {
        fun f(x: Double, y: Double): Double
    }

    /**
     * Find the root of the generic 2D parametric function 'func' using the regula falsi technique.
     * This is a pretty naive way to do root finding, but it's usually faster than simple bisection
     * while being robust in ways that e.g. the Newton-Raphson method isn't.
     *
     * @param func The Function2d to take the root of.
     * @param x0 x value of the lower bracket.
     * @param y0 y value of the lower bracket.
     * @param f0 value of 'func' at x_0, y_0 (passed in by caller to save a call to 'func' during
     * recursion)
     * @param x1 x value of the upper bracket.
     * @param y1 y value of the upper bracket.
     * @param f1 value of 'func' at x_1, y_1 (passed in by caller to save a call to 'func' during
     * recursion)
     * @param iterationsLeft Number of iterations of root finding left.
     * @return The parameter value 's' that interpolating between 0 and 1 that corresponds to the
     * (approximate) root.
     */
    private fun findRoot(
        func: Function2d,
        x0: Double,
        y0: Double,
        f0: Double,
        x1: Double,
        y1: Double,
        f1: Double,
        iterationsLeft: Int
    ): Double {
        if (iterationsLeft < 0 || epsilonEquals(f0, f1)) {
            return 1.0
        }
        val sGuess = max(0.0, min(1.0, -f0 / (f1 - f0)))
        val xGuess = (x1 - x0) * sGuess + x0
        val yGuess = (y1 - y0) * sGuess + y0
        val fGuess = func.f(xGuess, yGuess)
        return if (sign(f0) == sign(fGuess)) {
            // 0 and guess on same side of root, so use upper bracket.
            (sGuess + (1.0 - sGuess) * findRoot(func, xGuess, yGuess, fGuess, x1, y1, f1, iterationsLeft - 1))
        } else {
            // Use lower bracket.
            (sGuess * findRoot(func, x0, y0, f0, xGuess, yGuess, fGuess, iterationsLeft - 1))
        }
    }

    private fun findSteeringMaxS(
        x0: Double,
        y0: Double,
        f0: Double,
        x1: Double,
        y1: Double,
        f1: Double,
        maxDeviation: Double,
        maxIterations: Int
    ): Double {
        var f1 = f1
        f1 = unwrapAngle(f0, f1)
        val diff = f1 - f0
        if (abs(diff) <= maxDeviation) {
            // Can go all the way to s=1.
            return 1.0
        }
        val offset = f0 + sign(diff) * maxDeviation
        val func =
            Function2d { x: Double, y: Double -> unwrapAngle(f0, atan2(y, x)) - offset }
        return findRoot(func, x0, y0, f0 - offset, x1, y1, f1 - offset, maxIterations)
    }

    private fun findDriveMaxS(
        x0: Double,
        y0: Double,
        f0: Double,
        x1: Double,
        y1: Double,
        f1: Double,
        maxVelStep: Double,
        maxIterations: Int
    ): Double {
        val diff = f1 - f0
        if (abs(diff) <= maxVelStep) {
            // Can go all the way to s=1.
            return 1.0
        }
        val offset = f0 + sign(diff) * maxVelStep
        val func =
            Function2d { x: Double, y: Double -> hypot(x, y) - offset }
        return findRoot(func, x0, y0, f0 - offset, x1, y1, f1 - offset, maxIterations)
    }

    // protected double findDriveMaxS(
    //     double x_0, double y_0, double x_1, double y_1, double max_vel_step) {
    //   // Our drive velocity between s=0 and s=1 is quadratic in s:
    //   // v^2 = ((x_1 - x_0) * s + x_0)^2 + ((y_1 - y_0) * s + y_0)^2
    //   //     = a * s^2 + b * s + c
    //   // Where:
    //   //   a = (x_1 - x_0)^2 + (y_1 - y_0)^2
    //   //   b = 2 * x_0 * (x_1 - x_0) + 2 * y_0 * (y_1 - y_0)
    //   //   c = x_0^2 + y_0^2
    //   // We want to find where this quadratic results in a velocity that is > max_vel_step from our
    //   // velocity at s=0:
    //   // sqrt(x_0^2 + y_0^2) +/- max_vel_step = ...quadratic...
    //   final double dx = x_1 - x_0;
    //   final double dy = y_1 - y_0;
    //   final double a = dx * dx + dy * dy;
    //   final double b = 2.0 * x_0 * dx + 2.0 * y_0 * dy;
    //   final double c = x_0 * x_0 + y_0 * y_0;
    //   final double v_limit_upper_2 = Math.pow(Math.hypot(x_0, y_0) + max_vel_step, 2.0);
    //   final double v_limit_lower_2 = Math.pow(Math.hypot(x_0, y_0) - max_vel_step, 2.0);
    //   return 0.0;
    // }
    /**
     * Generate a new setpoint.
     *
//     * @param limits The kinematic limits to respect for this setpoint.
     * @param prevSetpoint The previous setpoint motion. Normally, you'd pass in the previous
     * iteration setpoint instead of the actual measured/estimated kinematic state.
     * @param desiredState The desired state of motion, such as from the driver sticks or a path
     * following algorithm.
     * @param dt The loop time.
     * @return A Setpoint object that satisfies all of the KinematicLimits while converging to
     * desiredState quickly.
     */
    fun generateSetpoint(prevSetpoint: SwerveSetpoint, desiredState: ChassisSpeeds, dt: Double): SwerveSetpoint {
        var desiredState: ChassisSpeeds = desiredState
        val modules: Array<Translation2d> = moduleLocations

        val desiredModuleState: Array<SwerveModuleState> = kinematics.toSwerveModuleStates(desiredState)
        // Make sure desiredState respects velocity limits.
        if (moduleLimits.maxDriveVelocity > 0.0) {
            SwerveDriveKinematics.desaturateWheelSpeeds(desiredModuleState, moduleLimits.maxDriveVelocity)
            desiredState = kinematics.toChassisSpeeds(*desiredModuleState)
        }

        // Special case: desiredState is a complete stop. In this case, module angle is arbitrary, so
        // just use the previous angle.
        var needToSteer = true
        if (desiredState.toTwist2d(0.02).epsilonEquals(Twist2d())) {
            needToSteer = false
            for (i in modules.indices) {
                desiredModuleState[i].angle = prevSetpoint.moduleStates[i].angle
                desiredModuleState[i].speedMetersPerSecond = 0.0
            }
        }

        // For each module, compute local Vx and Vy vectors.
        val prevVx = DoubleArray(modules.size)
        val prevVy = DoubleArray(modules.size)
        val prevHeading: Array<Rotation2d?> = arrayOfNulls(modules.size)
        val desiredVx = DoubleArray(modules.size)
        val desiredVy = DoubleArray(modules.size)
        val desiredHeading: Array<Rotation2d?> = arrayOfNulls(modules.size)
        var allModulesShouldFlip = true
        for (i in modules.indices) {
            prevVx[i] = (prevSetpoint.moduleStates[i].angle.cos * prevSetpoint.moduleStates[i].speedMetersPerSecond)
            prevVy[i] = (prevSetpoint.moduleStates[i].angle.sin * prevSetpoint.moduleStates[i].speedMetersPerSecond)
            prevHeading[i] = prevSetpoint.moduleStates[i].angle
            if (prevSetpoint.moduleStates[i].speedMetersPerSecond < 0.0) {
                prevHeading[i] = prevHeading[i]!!.rotateBy(Rotation2d.fromRadians(Math.PI))
            }
            desiredVx[i] = desiredModuleState[i].angle.cos * desiredModuleState[i].speedMetersPerSecond
            desiredVy[i] = desiredModuleState[i].angle.sin * desiredModuleState[i].speedMetersPerSecond
            desiredHeading[i] = desiredModuleState[i].angle
            if (desiredModuleState[i].speedMetersPerSecond < 0.0) {
                desiredHeading[i] = desiredHeading[i]!!.rotateBy(Rotation2d.fromRadians(Math.PI))
            }
            if (allModulesShouldFlip) {
                val requiredRotationRad: Double = abs(prevHeading[i]!!.unaryMinus().rotateBy(desiredHeading[i]).radians)
                if (requiredRotationRad < Math.PI / 2.0) {
                    allModulesShouldFlip = false
                }
            }
        }
        if (allModulesShouldFlip
            && !prevSetpoint.chassisSpeeds.toTwist2d(0.02).epsilonEquals(Twist2d()) && !desiredState.toTwist2d(0.02)
                .epsilonEquals(Twist2d())
        ) {
            // It will (likely) be faster to stop the robot, rotate the modules in place to the complement
            // of the desired
            // angle, and accelerate again.
            return generateSetpoint(prevSetpoint, ChassisSpeeds(), dt)
        }

        // Compute the deltas between start and goal. We can then interpolate from the start state to
        // the goal state; then
        // find the amount we can move from start towards goal in this cycle such that no kinematic
        // limit is exceeded.
        val dx: Double = desiredState.vxMetersPerSecond - prevSetpoint.chassisSpeeds.vxMetersPerSecond
        val dy: Double = desiredState.vyMetersPerSecond - prevSetpoint.chassisSpeeds.vyMetersPerSecond
        val dtheta: Double =
            desiredState.omegaRadiansPerSecond - prevSetpoint.chassisSpeeds.omegaRadiansPerSecond

        // 's' interpolates between start and goal. At 0, we are at prevState and at 1, we are at
        // desiredState.
        var minS = 1.0

        // In cases where an individual module is stopped, we want to remember the right steering angle
        // to command (since
        // inverse kinematics doesn't care about angle, we can be opportunistically lazy).
        val overrideSteering: MutableList<Rotation2d?> = ArrayList(modules.size)
        // Enforce steering velocity limits. We do this by taking the derivative of steering angle at
        // the current angle,
        // and then backing out the maximum interpolant between start and goal states. We remember the
        // minimum across all modules, since
        // that is the active constraint.
        val maxThetaStep = dt * moduleLimits.maxSteeringVelocity
        for (i in modules.indices) {
            if (!needToSteer) {
                overrideSteering.add(prevSetpoint.moduleStates[i].angle)
                continue
            }
            overrideSteering.add(null)
            if (epsilonEquals(prevSetpoint.moduleStates[i].speedMetersPerSecond, 0.0)) {
                // If module is stopped, we know that we will need to move straight to the final steering
                // angle, so limit based
                // purely on rotation in place.
                if (epsilonEquals(desiredModuleState[i].speedMetersPerSecond, 0.0)) {
                    // Goal angle doesn't matter. Just leave module at its current angle.
                    overrideSteering[i] = prevSetpoint.moduleStates[i].angle
                    continue
                }

                var necessaryRotation: Rotation2d = prevSetpoint.moduleStates[i].angle.unaryMinus().rotateBy(desiredModuleState[i].angle)
                if (flipHeading(necessaryRotation)) {
                    necessaryRotation = necessaryRotation.rotateBy(Rotation2d.fromRadians(Math.PI))
                }
                // getRadians() bounds to +/- Pi.
                val numStepsNeeded: Double = abs(necessaryRotation.radians) / maxThetaStep

                if (numStepsNeeded <= 1.0) {
                    // Steer directly to goal angle.
                    overrideSteering[i] = desiredModuleState[i].angle
                    // Don't limit the global min_s;
                    continue
                } else {
                    // Adjust steering by max_theta_step.
                    overrideSteering[i] = prevSetpoint.moduleStates[i].angle
                        .rotateBy(Rotation2d.fromRadians(sign(necessaryRotation.radians) * maxThetaStep))
                    minS = 0.0
                    continue
                }
            }
            if (minS == 0.0) {
                // s can't get any lower. Save some CPU.
                continue
            }

            val kMaxIterations = 8
            val s =
                findSteeringMaxS(
                    prevVx[i],
                    prevVy[i],
                    prevHeading[i]!!.radians,
                    desiredVx[i],
                    desiredVy[i],
                    desiredHeading[i]!!.radians,
                    maxThetaStep,
                    kMaxIterations
                )
            minS = min(minS, s)
        }

        // Enforce drive wheel acceleration limits.
        val maxVelStep = dt * moduleLimits.maxDriveAcceleration
        for (i in modules.indices) {
            if (minS == 0.0) {
                // No need to carry on.
                break
            }
            val vxMinS = if (minS == 1.0) desiredVx[i] else (desiredVx[i] - prevVx[i]) * minS + prevVx[i]
            val vyMinS = if (minS == 1.0) desiredVy[i] else (desiredVy[i] - prevVy[i]) * minS + prevVy[i]
            // Find the max s for this drive wheel. Search on the interval between 0 and min_s, because we
            // already know we can't go faster
            // than that.
            val kMaxIterations = 10
            val s = minS * findDriveMaxS(
                prevVx[i],
                prevVy[i],
                hypot(prevVx[i], prevVy[i]),
                vxMinS,
                vyMinS,
                hypot(vxMinS, vyMinS),
                maxVelStep,
                kMaxIterations
            )

            minS = min(minS, s)
        }

        val retSpeeds = ChassisSpeeds(
            prevSetpoint.chassisSpeeds.vxMetersPerSecond + minS * dx,
            prevSetpoint.chassisSpeeds.vyMetersPerSecond + minS * dy,
            prevSetpoint.chassisSpeeds.omegaRadiansPerSecond + minS * dtheta
        )
        val retStates: Array<SwerveModuleState> = kinematics.toSwerveModuleStates(retSpeeds)
        for (i in modules.indices) {
            val maybeOverride = overrideSteering[i]
            if (maybeOverride != null) {
                val override: Rotation2d = maybeOverride
                if (flipHeading(retStates[i].angle.unaryMinus().rotateBy(override))) {
                    retStates[i].speedMetersPerSecond *= -1.0
                }
                retStates[i].angle = override
            }
            val deltaRotation: Rotation2d = prevSetpoint.moduleStates[i].angle.unaryMinus().rotateBy(retStates[i].angle)
            if (flipHeading(deltaRotation)) {
                retStates[i].angle = retStates[i].angle.rotateBy(Rotation2d.fromRadians(Math.PI))
                retStates[i].speedMetersPerSecond *= -1.0
            }
        }
        return SwerveSetpoint(retSpeeds, retStates)
    }
}
