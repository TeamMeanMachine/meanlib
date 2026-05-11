package org.team2471.frc.lib.swerve

import choreo.trajectory.SwerveSample
import choreo.trajectory.Trajectory
import choreo.util.ChoreoAllianceFlipUtil.Flipper
import java.util.ArrayList

/**
 * Instead of Rotating or Mirroring across the center of the field,
 * this mirrors the trajectory left-to-right or right-to-left from driver station perspectives.
 */
//fun Trajectory<SwerveSample>.sideToSideFlip(doFlip: Boolean): Trajectory<SwerveSample> { TODO: UNCOMMENT WHEN CHOREO 2027
//    if (doFlip) {
//        val flippedStates: ArrayList<SwerveSample> = ArrayList()
//        for (state in this.samples()) {
//            flippedStates.add(state.sideToSideFlip())
//        }
//        return Trajectory(this.name(), flippedStates, this.splits(), this.events())
//    } else {
//        return this
//    }
//}
//
//fun SwerveSample.sideToSideFlip(): SwerveSample {
//    val rotated = this.rotateAround()
//    val rotatedAndMirrored = rotated.modifiedMirror()
//    return rotatedAndMirrored
//}



//fun SwerveSample.rotateAround(): SwerveSample { TODO: UNCOMMENT WHEN CHOREO 2027
//    return SwerveSample(
//        this.t,
//        Flipper.ROTATE_AROUND.flipX(this.x),
//        Flipper.ROTATE_AROUND.flipY(this.y),
//        Flipper.ROTATE_AROUND.flipHeading(this.heading),
//        -this.vx,
//        -this.vy,
//        this.omega,
//        -this.ax,
//        -this.ay,
//        this.alpha,
//        this.moduleForcesX(),
//        this.moduleForcesY()
//    )
//}

/** Doesn't perfectly match choreo mirror. Module forces do not change arrangement */
//fun SwerveSample.modifiedMirror(): SwerveSample { TODO: UNCOMMENT WHEN CHOREO 2027
//    return SwerveSample(
//        this.t,
//        Flipper.MIRRORED.flipX(this.x),
//        Flipper.MIRRORED.flipY(this.y),
//        Flipper.MIRRORED.flipHeading(this.heading),
//        -this.vx,
//        this.vy,
//        -this.omega,
//        -this.ax,
//        this.ay,
//        -this.alpha,  // FL, FR, BL, BR
//        // Mirrored
//        // -FR, -FL, -BR, -BL
//        doubleArrayOf(
//            this.moduleForcesX()[2],
//            this.moduleForcesX()[3],
//            this.moduleForcesX()[0],
//            this.moduleForcesX()[1]
//        ),  // FL, FR, BL, BR
//        // Mirrored
//        // FR, FL, BR, BL
//        doubleArrayOf(
//            -this.moduleForcesY()[2],
//            -this.moduleForcesY()[3],
//            -this.moduleForcesY()[0],
//            -this.moduleForcesY()[1]
//        )
//    )
//}