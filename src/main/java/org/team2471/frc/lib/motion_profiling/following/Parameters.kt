package org.team2471.frc.lib.motion_profiling.following

import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

data class RobotParameters(
        var robotWidth: Double,
        var robotLength: Double
)

sealed class DrivetrainParameters {
    companion object {
        @JvmStatic
        val moshiAdapter: PolymorphicJsonAdapterFactory<DrivetrainParameters>
                get() = PolymorphicJsonAdapterFactory.of(DrivetrainParameters::class.java, "drivetrain")
                        .withSubtype(ArcadeParameters::class.java, "arcade")

    }
}

data class ArcadeParameters(
        var trackWidth: Double,
        var scrubFactor: Double,
        var leftFeedForwardCoefficient: Double,
        var leftFeedForwardOffset: Double,
        var rightFeedForwardCoefficient: Double,
        var rightFeedForwardOffset: Double,
        var driveTurningP: Double = 0.0,
        var headingFeedForward: Double = 0.0,
        var doHeadingCorrection: Boolean = false,
        var headingCorrectionP: Double = 0.0,
        var headingCorrectionI: Double = 0.0,
        var headingCorrectionIDecay: Double = 1.0
) : DrivetrainParameters()

// TODO: swerve parameters
