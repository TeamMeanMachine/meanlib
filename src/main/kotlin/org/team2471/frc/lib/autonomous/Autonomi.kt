package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command
import org.wpilib.math.geometry.Pose2d

/**
 * A class that holds an autonomous command, its name, and a starting position.
 */
class Autonomi(val name: String, val command: Command, val startingPositionSupplier: (() -> Pose2d)? = null)