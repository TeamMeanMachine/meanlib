package org.team2471.frc.lib.actuators

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.ControlMode
import com.revrobotics.*
import org.team2471.frc.lib.units.Angle
import org.team2471.frc.lib.units.degrees
import kotlin.math.max

const val TICKS_PER_REVOLUTION = 42.0

class SparkMaxWrapper (deviceNumber : Int) : IMotorController {
    var positionSetpoint: Double = 0.0
    var velocitySetPoint: Double = 0.0
    val maxRPM = 5700.0
    val canID = deviceNumber

    private val _motorController = CANSparkMax(deviceNumber, CANSparkMaxLowLevel.MotorType.kBrushless).apply {
        val enc = getEncoder(EncoderType.kHallSensor, 4096)
        println("encoder value: " + enc.position + "; id: " + deviceNumber + "-------------------------------------------------------------")
        restoreFactoryDefaults()

        //println("encoder value2: " + enc.position + "; id: " + deviceNumber + "-----------------------------------")
    }

    val analogPosition: Double
        get() = _motorController.getAnalog(CANAnalog.AnalogMode.kAbsolute).position

    val analogAngle: Double
        get() = analogPosition * 360.0/3.036 - 15.65


//    var closedLoopError : Double = 0.0

    fun init() {
    }

    val hasErrors: Boolean
        get() = _motorController.faults > 0

    override fun follow(followerID: IMotorController) {
        _motorController.follow(followerID as CANSparkMax)
        // do nothing .. do not allow follower
    }

//    override fun motorOutputPercent(): Double {
//        return 0.0
//    }

    override fun getSelectedSensorPosition(pidIdx: Int): Int {
        return (_motorController.getEncoder().position * TICKS_PER_REVOLUTION).toInt()
    }

    override fun setNeutralMode(neutralMode: NeutralMode?) {
        when (neutralMode) {
            NeutralMode.Brake -> _motorController.idleMode = CANSparkMax.IdleMode.kBrake
            NeutralMode.Coast -> _motorController.idleMode = CANSparkMax.IdleMode.kCoast
            NeutralMode.EEPROMSetting -> {}
        }
    }


    override fun setInverted(invert: Boolean) {
        _motorController.inverted = invert
    }

    override fun getInverted(): Boolean {
        return _motorController.inverted
    }

    override fun configNeutralDeadband(percentDeadband: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configVoltageCompSaturation(voltage: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configVoltageMeasurementFilter(filterWindowSamples: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun enableVoltageCompensation(enable: Boolean) {
    }

    override fun getBusVoltage(): Double {
        return 0.0
    }

    override fun getMotorOutputPercent(): Double {
        return 0.0
    }

    override fun getMotorOutputVoltage(): Double {
        return 0.0
    }

    override fun getTemperature(): Double {
        return 0.0
    }

    override fun configSelectedFeedbackCoefficient(coefficient: Double, pidIdx: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configRemoteFeedbackFilter(
        deviceID: Int,
        remoteSensorSource: RemoteSensorSource?,
        remoteOrdinal: Int,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun configSensorTerm(sensorTerm: SensorTerm?, feedbackDevice: FeedbackDevice?, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun getSelectedSensorVelocity(pidIdx: Int): Int {
        return 0
    }

    override fun setSelectedSensorPosition(sensorPos: Int, pidIdx: Int, timeoutMs: Int): ErrorCode {
       _motorController.getEncoder().setPosition(sensorPos.toDouble())
        return ErrorCode.OK
    }

    override fun setControlFramePeriod(frame: ControlFrame?, periodMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun setStatusFramePeriod(frame: StatusFrame?, periodMs: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun getStatusFramePeriod(frame: StatusFrame?, timeoutMs: Int): Int {
        return 0
    }

    override fun configForwardLimitSwitchSource(
        type: RemoteLimitSwitchSource?,
        normalOpenOrClose: LimitSwitchNormal?,
        deviceID: Int,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun configReverseLimitSwitchSource(
        type: RemoteLimitSwitchSource?,
        normalOpenOrClose: LimitSwitchNormal?,
        deviceID: Int,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun overrideLimitSwitchesEnable(enable: Boolean) {
    }

    override fun configForwardSoftLimitEnable(enable: Boolean, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configReverseSoftLimitThreshold(reverseSensorLimit: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configForwardSoftLimitThreshold(forwardSensorLimit: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun overrideSoftLimitsEnable(enable: Boolean) {
    }

    override fun configAuxPIDPolarity(invert: Boolean, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun setIntegralAccumulator(iaccum: Double, pidIdx: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun getClosedLoopError(pidIdx: Int): Int {
        return (positionSetpoint * TICKS_PER_REVOLUTION).toInt() - getSelectedSensorPosition(pidIdx)
    }

    override fun getIntegralAccumulator(pidIdx: Int): Double {
        return 0.0
    }

    override fun getErrorDerivative(pidIdx: Int): Double {
        return 0.0
    }

    override fun getClosedLoopTarget(pidIdx: Int): Double {
        return 0.0
    }

    override fun getActiveTrajectoryPosition(): Int {
        return 0
    }

    override fun getActiveTrajectoryVelocity(): Int {
        return 0
    }

    override fun getActiveTrajectoryHeading(): Double {
        return 0.0
    }

    override fun configMotionProfileTrajectoryPeriod(baseTrajDurationMs: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configMotionSCurveStrength(curveStrength: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun clearMotionProfileHasUnderrun(timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun changeMotionControlFramePeriod(periodMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun clearMotionProfileTrajectories(): ErrorCode {
        return ErrorCode.OK
    }

    override fun clearStickyFaults(timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configGetCustomParam(paramIndex: Int, timeoutMs: Int): Int {
       return 0
    }

    override fun configGetParameter(paramEnum: Int, ordinal: Int, timeoutMs: Int): Double {
        return 0.0
    }

    override fun configGetParameter(paramEnum: ParamEnum?, ordinal: Int, timeoutMs: Int): Double {
       return 0.0
    }

    override fun configReverseSoftLimitEnable(enable: Boolean, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configSetCustomParam(newValue: Int, paramIndex: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configSetParameter(
        param: ParamEnum?,
        value: Double,
        subValue: Int,
        ordinal: Int,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun configSetParameter(param: Int, value: Double, subValue: Int, ordinal: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun getBaseID(): Int {
        return 0
    }

    override fun getControlMode(): ControlMode {
        return ControlMode.PercentOutput
    }

    override fun getDeviceID(): Int {
        return 0
    }

    override fun getFaults(toFill: Faults?): ErrorCode {
        var revRetFaults = Faults()
        revRetFaults.APIError = _motorController.faults > 0
        //toFill = revRetFaults
        return ErrorCode.OK
    }

    override fun getMotionProfileTopLevelBufferCount(): Int {
        return 0
    }

    override fun pushMotionProfileTrajectory(trajPt: TrajectoryPoint?): ErrorCode {
        return ErrorCode.OK
    }

    override fun isMotionProfileTopLevelBufferFull(): Boolean {
        return true
    }

    override fun processMotionProfileBuffer() {
    }

    override fun getLastError(): ErrorCode {
        return ErrorCode.OK
    }

    override fun getStickyFaults(toFill: StickyFaults?): ErrorCode {
        return ErrorCode.OK
    }

    override fun getFirmwareVersion(): Int {
        return 0
    }

    override fun hasResetOccurred(): Boolean {
        return true
    }

    override fun getMotionProfileStatus(statusToFill: MotionProfileStatus?): ErrorCode {
        return ErrorCode.OK
    }

    override fun valueUpdated() {
    }


    override fun set(Mode: ControlMode?, demand: Double) {
        when (Mode) {
            ControlMode.Velocity ->  {
                velocitySetPoint = demand * maxRPM

                // handle out of bounds conditions
                if (velocitySetPoint > maxRPM) {
                    velocitySetPoint = maxRPM
                } else if(velocitySetPoint < (-1 * maxRPM)) {
                    velocitySetPoint = -1 * maxRPM
                }

                // set reference point of
                _motorController.pidController.setReference(velocitySetPoint, ControlType.kVelocity)
            }
            ControlMode.PercentOutput -> _motorController.set(demand)
            ControlMode.Position -> {
                positionSetpoint = demand / TICKS_PER_REVOLUTION
                _motorController.pidController.setReference(positionSetpoint, ControlType.kPosition)
//                println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
            }
            else -> {}
        }
    }

    override fun set(Mode: ControlMode?, demand0: Double, demand1Type: DemandType?, demand1: Double) {
    }

    override fun set (Mode : ControlMode, demand0 : Double, demand1: Double) {
    }

    fun setAngle(setPoint : Angle){
        set(ControlMode.Position, ((setPoint - getSelectedSensorPosition(0).degrees).wrap()).asDegrees)
    }

    override fun selectProfileSlot(slotIdx: Int, pidIdx: Int) {
    }

    override fun neutralOutput() {
        _motorController.set(0.0)
    }

    override fun setSensorPhase(PhaseSensor: Boolean) {
        if(PhaseSensor) {
            _motorController.encoder.positionConversionFactor = 1.0
        } else {
            _motorController.encoder.positionConversionFactor = -1.0
        }
    }

    override fun configClosedloopRamp(secondsFromNeutralToFull: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configOpenloopRamp(secondsFromNeutralToFull: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configNominalOutputReverse(percentOut: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configNominalOutputForward(percentOut: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configPeakOutputReverse(percentOut: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configPeakOutputForward(percentOut: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configMotionAcceleration(sensorUnitsPer100msPerSec: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configMotionCruiseVelocity(sensorUnitsPer100ms: Int, timeoutMs: Int): ErrorCode {
    return ErrorCode.OK
    }

    fun configSelectedFeedbackSensor(feedbackDevice: FeedbackDevice?, pidIdx: Int, timeoutMs: Int) : ErrorCode {
        return ErrorCode.OK
    }
    override fun configSelectedFeedbackSensor(
        feedbackDevice: RemoteFeedbackDevice?,
        pidIdx: Int,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun config_kP(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.p = value
        return ErrorCode.OK
    }

    override fun config_kD(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.d = value
        return ErrorCode.OK
    }

    override fun config_kF(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.ff = value
        return ErrorCode.OK
    }

    override fun config_kI(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.i = value
        return ErrorCode.OK
    }

    override fun config_IntegralZone(slotIdx: Int, izone: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configAllowableClosedloopError(slotIdx: Int, allowableCloseLoopError: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configMaxIntegralAccumulator(slotIdx: Int, iaccum: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configClosedLoopPeakOutput(slotIdx: Int, percentOut: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configClosedLoopPeriod(slotIdx: Int, loopTimeMs: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    val current: Double
        get() = _motorController.outputCurrent


    fun restoreFactoryDefaults() {
        _motorController.restoreFactoryDefaults()
    }


}