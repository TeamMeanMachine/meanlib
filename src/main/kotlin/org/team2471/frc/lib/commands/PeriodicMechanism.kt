package org.team2471.frc.lib.commands

import org.wpilib.command3.Mechanism

interface PeriodicMechanism: Mechanism {
    fun periodic()
}