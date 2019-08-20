package org.team2471.frc.lib.actuators

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.*
import com.revrobotics.CANSparkMax
import com.revrobotics.CANSparkMaxLowLevel

class SparkMaxWrapper (deviceNumber : Int) : IMotorController {
    private val internalController  = CANSparkMax(deviceNumber, CANSparkMaxLowLevel.MotorType.kBrushless)
    fun init() {

    }

    override fun follow(followerID: IMotorController) {
        internalController.follow(followerID as CANSparkMax)
        // do nothing .. do not allow follower
    }

//    override fun motorOutputPercent(): Double {
//        return 0.0
//    }

    override fun getSelectedSensorPosition(pidIdx: Int): Int {
        TODO()

    }

    override fun setNeutralMode(neutralMode: NeutralMode?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setInverted(invert: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInverted(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configNeutralDeadband(percentDeadband: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configVoltageCompSaturation(voltage: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configVoltageMeasurementFilter(filterWindowSamples: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enableVoltageCompensation(enable: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBusVoltage(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMotorOutputPercent(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMotorOutputVoltage(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTemperature(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSelectedFeedbackCoefficient(coefficient: Double, pidIdx: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configRemoteFeedbackFilter(
        deviceID: Int,
        remoteSensorSource: RemoteSensorSource?,
        remoteOrdinal: Int,
        timeoutMs: Int
    ): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSensorTerm(sensorTerm: SensorTerm?, feedbackDevice: FeedbackDevice?, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSelectedSensorVelocity(pidIdx: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setSelectedSensorPosition(sensorPos: Int, pidIdx: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setControlFramePeriod(frame: ControlFrame?, periodMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setStatusFramePeriod(frame: StatusFrame?, periodMs: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStatusFramePeriod(frame: StatusFrame?, timeoutMs: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configForwardLimitSwitchSource(
        type: RemoteLimitSwitchSource?,
        normalOpenOrClose: LimitSwitchNormal?,
        deviceID: Int,
        timeoutMs: Int
    ): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configReverseLimitSwitchSource(
        type: RemoteLimitSwitchSource?,
        normalOpenOrClose: LimitSwitchNormal?,
        deviceID: Int,
        timeoutMs: Int
    ): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun overrideLimitSwitchesEnable(enable: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configForwardSoftLimitEnable(enable: Boolean, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configReverseSoftLimitThreshold(reverseSensorLimit: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configForwardSoftLimitThreshold(forwardSensorLimit: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun overrideSoftLimitsEnable(enable: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configAuxPIDPolarity(invert: Boolean, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setIntegralAccumulator(iaccum: Double, pidIdx: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getClosedLoopError(pidIdx: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getIntegralAccumulator(pidIdx: Int): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getErrorDerivative(pidIdx: Int): Double {
        TODO()
    }

    override fun getClosedLoopTarget(pidIdx: Int): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveTrajectoryPosition(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveTrajectoryVelocity(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getActiveTrajectoryHeading(): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configMotionProfileTrajectoryPeriod(baseTrajDurationMs: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configMotionSCurveStrength(curveStrength: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearMotionProfileHasUnderrun(timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun changeMotionControlFramePeriod(periodMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearMotionProfileTrajectories(): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearStickyFaults(timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configGetCustomParam(paramIndex: Int, timeoutMs: Int): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configGetParameter(paramEnum: Int, ordinal: Int, timeoutMs: Int): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configGetParameter(paramEnum: ParamEnum?, ordinal: Int, timeoutMs: Int): Double {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configReverseSoftLimitEnable(enable: Boolean, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSetCustomParam(newValue: Int, paramIndex: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSetParameter(
        param: ParamEnum?,
        value: Double,
        subValue: Int,
        ordinal: Int,
        timeoutMs: Int
    ): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSetParameter(param: Int, value: Double, subValue: Int, ordinal: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBaseID(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getControlMode(): ControlMode {
        TODO()

    }

    override fun getDeviceID(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFaults(toFill: Faults?): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMotionProfileTopLevelBufferCount(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pushMotionProfileTrajectory(trajPt: TrajectoryPoint?): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isMotionProfileTopLevelBufferFull(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun processMotionProfileBuffer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLastError(): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getStickyFaults(toFill: StickyFaults?): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFirmwareVersion(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasResetOccurred(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getMotionProfileStatus(statusToFill: MotionProfileStatus?): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun valueUpdated() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun set(Mode: ControlMode?, demand: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun set(Mode: ControlMode?, demand0: Double, demand1Type: DemandType?, demand1: Double) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun set (Mode : ControlMode, demand0 : Double, demand1: Double) {

    }
    override fun selectProfileSlot(slotIdx: Int, pidIdx: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun neutralOutput() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setSensorPhase(PhaseSensor: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configClosedloopRamp(secondsFromNeutralToFull: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configOpenloopRamp(secondsFromNeutralToFull: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configNominalOutputReverse(percentOut: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configNominalOutputForward(percentOut: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configPeakOutputReverse(percentOut: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configPeakOutputForward(percentOut: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configMotionAcceleration(sensorUnitsPer100msPerSec: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configMotionCruiseVelocity(sensorUnitsPer100ms: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configSelectedFeedbackSensor(
        feedbackDevice: RemoteFeedbackDevice?,
        pidIdx: Int,
        timeoutMs: Int
    ): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config_kP(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config_kD(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config_kF(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config_kI(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun config_IntegralZone(slotIdx: Int, izone: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configAllowableClosedloopError(slotIdx: Int, allowableCloseLoopError: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configMaxIntegralAccumulator(slotIdx: Int, iaccum: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configClosedLoopPeakOutput(slotIdx: Int, percentOut: Double, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun configClosedLoopPeriod(slotIdx: Int, loopTimeMs: Int, timeoutMs: Int): ErrorCode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}