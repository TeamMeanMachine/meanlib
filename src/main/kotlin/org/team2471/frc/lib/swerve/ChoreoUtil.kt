package org.team2471.frc.lib.swerve

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory

/**
 * Returns this trajectory, mirrored left-to-right from the driver's perspective.
 *
 * @param doMirror Whether to mirror the trajectory or not.
 *
 * @see mirrorY
 * @see mirrorX
 * @see rotateAround
 */
fun Trajectory<SwerveSample>.mirrorY(doMirror: Boolean): Trajectory<SwerveSample> {
    return if (doMirror) {
        this.mirrorY()
    } else {
        this
    }
}

/**
 * Returns this trajectory, mirrored to the other alliance.
 *
 * @param doMirror Whether to mirror the trajectory or not.
 *
 * @see mirrorX
 * @see mirrorY
 * @see rotateAround
 */
fun Trajectory<SwerveSample>.mirrorX(doMirror: Boolean): Trajectory<SwerveSample> {
    return if (doMirror) {
        this.mirrorX()
    } else {
        this
    }
}

/**
 * Returns this trajectory, rotated 180 degrees around the field center.
 *
 * @param doRotate Whether to rotate the trajectory or not.
 *
 * @see rotateAround
 * @see mirrorY
 * @see mirrorX
 */
fun Trajectory<SwerveSample>.rotateAround(doRotate: Boolean): Trajectory<SwerveSample> {
    return if (doRotate) {
        this.rotateAround()
    } else {
        this
    }
}