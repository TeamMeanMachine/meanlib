package org.team2471.frc.lib.autonomous

import org.wpilib.command3.Command

class TestRoutine(val name: String, val command: Command, val initFunction: () -> Unit = {})