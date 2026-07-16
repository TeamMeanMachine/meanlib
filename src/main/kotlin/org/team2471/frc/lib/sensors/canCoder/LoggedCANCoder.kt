package org.team2471.frc.lib.sensors.canCoder

import org.team2471.frc.lib.framework.internal.akitLoggers.SimpleLogger
import org.team2471.frc.lib.units.*
import org.team2471.frc.lib.util.RobotMode
import org.team2471.frc.lib.util.robotMode

class LoggedCANCoder(val id: Int, name: String, simRotationSupplier: () -> Angle = {0.0.rotations}, canBus: String = "") {
    private val inputs = LoggedCANCoderIO.CANCoderIOInputs(name)
    private val io: LoggedCANCoderIO = when (robotMode) {
        RobotMode.REAL -> LoggedCANCoderReal(id, canBus)
        RobotMode.REPLAY, RobotMode.SIM -> LoggedCANCoderSim(simRotationSupplier)
    }
    private var doUpdate = true

    val position: Angle
        get() = inputs.position.rotations
    val velocity: AngularVelocity get() = inputs.velocity.rotations.perSecond
    val absolutePosition: Angle
        get() = inputs.absolutePosition.rotations

    init {
        io.updateInputs(inputs)
//        SimpleLogger.processInputs("Sensors", inputs)
        MasterCANCoder.addCANCoder(this)
    }

    fun periodicLoop() {
        if (doUpdate) {
            io.updateInputs(inputs)
//            SimpleLogger.processInputs("Sensors", inputs)
        }
    }

    fun setInverted(invert: Boolean) {
        io.setInverted(invert)
    }

    fun setSimAngleSupplier(angleSupplier: () -> Angle) {
        io.simAngleSupplier = (angleSupplier)
    }

    fun stopUpdates() {
        doUpdate = false
    }
    fun startUpdates() {
        doUpdate = true
    }

    /**
     * [offset] units in rotations
     */
    fun setMagnetSensorOffset(offset: Angle) {
        io.setMagnetOffset(offset.asRotations)
    }
}