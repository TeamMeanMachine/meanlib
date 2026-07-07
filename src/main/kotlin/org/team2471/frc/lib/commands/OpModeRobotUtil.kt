package org.team2471.frc.lib.commands

import org.wpilib.framework.OpModeRobot

/** Add a callback to run at a specific period. If no period is specified, it is equal to the Robot period: [OpModeRobot.getPeriod] */
fun OpModeRobot.addPeriodic(callback: () -> Unit) = addPeriodic(callback, this.period)