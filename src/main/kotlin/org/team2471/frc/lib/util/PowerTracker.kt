package org.team2471.frc.lib.util

//import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.MeanLogger
import org.team2471.frc.lib.units.amps

object PowerTracker {

    private val motors: ArrayList<MotorTracker> = arrayListOf()

    var totalCharge = 0.0
    var totalPower = 0.0

    // Don't call if sim
    fun addMotors(name: String, currentSupplier: () -> Double, numMotors: Int = 1, voltageSupplier: () -> Double? = {null}) {
        motors.add(MotorTracker(name, currentSupplier, voltageSupplier, numMotors))
    }

    fun update(delta: Double = 0.02) {
        var newTotalCharge = 0.0
        motors.forEach { it ->
            it.update(delta)
            newTotalCharge += it.totalCharge
//            totalPower += it.totalPower
        }
        totalCharge = newTotalCharge
    }

    fun logData() {
        motors.forEach {
            MeanLogger.recordOutput("PowerTracker/${it.name} Current Draw", it.current)
            MeanLogger.recordOutput("PowerTracker/${it.name} Total Charge", it.totalCharge)
        }
        MeanLogger.recordOutput("PowerTracker/Robot Total Charge", totalCharge)
    }
}

private class MotorTracker(val name: String, val currentSupplier: () -> Double, val voltageSupplier: () -> Double? = {null}, val numMotors: Int = 1) {
    var totalCharge = 0.0
    var current = 0.0
//    var totalPower = 0.0

    fun update(delta: Double = 0.02) {
        current = numMotors * currentSupplier.invoke()
//        val power = (voltageSupplier.invoke() ?: currentVoltage) * numMotors * currentSupplier.invoke()


        totalCharge += current * delta
//        totalPower += power * delta
    }
}