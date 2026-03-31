package org.team2471.frc.lib.util

import org.littletonrobotics.junction.Logger
import org.team2471.frc.lib.units.amps

class PowerTracker {

    private val motors: ArrayList<MotorTracker> = arrayListOf()

    var totalCharge = 0.0
    var totalPower = 0.0

    // Don't call if sim
    fun addMotors(name: String, currentSupplier: () -> Double, numMotors: Int = 1, voltageSupplier: () -> Double? = {null}) {
        motors.add(MotorTracker(name, currentSupplier, voltageSupplier, numMotors))
    }

    fun update(/*currentVoltage: Double,*/ delta: Double = 0.02) {
//        Logger.recordOutput("PowerTracker/Rio Voltage", currentVoltage, "volts")
        totalCharge = 0.0
//        totalPower = 0.0
        motors.forEach { it ->
            it.update(/*currentVoltage*/delta)
            totalCharge += it.totalCharge
//            totalPower += it.totalPower
        }
        Logger.recordOutput("PowerTracker/Robot Total Charge", totalCharge, "amp seconds")
//        Logger.recordOutput("PowerTracker/Robot Energy Usage", totalPower, "watt hours")
    }
}

private class MotorTracker(val name: String, val currentSupplier: () -> Double, val voltageSupplier: () -> Double? = {null}, val numMotors: Int = 1) {
    var totalCharge = 0.0
//    var totalPower = 0.0

    fun update(/*currentVoltage: Double,*/ delta: Double = 0.02) {
        val current = numMotors * currentSupplier.invoke()
//        val power = (voltageSupplier.invoke() ?: currentVoltage) * numMotors * currentSupplier.invoke()

//        Logger.recordOutput("PowerTracker/${name} Supply Voltage", voltageSupplier.invoke() ?: 0.0, "volts")

        Logger.recordOutput("PowerTracker/${name} Current Draw", current, "amps")
//        Logger.recordOutput("PowerTracker/${name} Power Draw", power, "watts")

        totalCharge += current * delta
//        totalPower += power * delta

        Logger.recordOutput("PowerTracker/${name} Total Charge", totalCharge, "amp seconds")
//        Logger.recordOutput("PowerTracker/${name} Energy Usage", totalPower, "watt hours")
    }
}