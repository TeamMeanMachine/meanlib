package org.team2471.frc.lib.actuators

import com.ctre.phoenix.ErrorCode
import com.ctre.phoenix.ParamEnum
import com.ctre.phoenix.motion.MotionProfileStatus
import com.ctre.phoenix.motion.TrajectoryPoint
import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.BaseMotorController
import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.can.BaseTalon
import com.ctre.phoenix.sensors.CANCoder
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
        _motorController.follow((followerID as SparkMaxWrapper)._motorController, inverted != followerID.inverted)
    }

    override fun getSelectedSensorPosition(pidIdx: Int): Double {
        return (_motorController.getEncoder().position * TICKS_PER_REVOLUTION)
    }

    override fun setNeutralMode(neutralMode: NeutralMode?) {
        when (neutralMode) {
            NeutralMode.Brake -> _motorController.idleMode = CANSparkMax.IdleMode.kBrake
            NeutralMode.Coast -> _motorController.idleMode = CANSparkMax.IdleMode.kCoast
            NeutralMode.EEPROMSetting -> {}
            else -> {}
        }
    }


    fun burnFlash(){
        println("Burned Flash for ${_motorController.deviceId}")
        _motorController.burnFlash()
    }
    override fun setInverted(invert: Boolean) {
        _motorController.inverted = invert
    }

    override fun setInverted(invertType: InvertType?) {
        if (invertType == InvertType.FollowMaster || invertType == InvertType.OpposeMaster) {
            println("ERROR!!!   SparkMax motor ${_motorController.deviceId} setInverted attempted to follow. This only works for CTRE motors.")
        } else {
            _motorController.inverted = (invertType == InvertType.InvertMotorOutput)
        }
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
        println("Error: SparkMaxWrapper.getMotorOutputPercent nonfunctional")
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

    override fun configRemoteFeedbackFilter(canCoderRef: CANCoder?, remoteOrdinal: Int, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configRemoteFeedbackFilter(talonRef: BaseTalon?, remoteOrdinal: Int, timeoutMs: Int): ErrorCode {
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

    override fun getSelectedSensorVelocity(pidIdx: Int): Double {
        return (_motorController.encoder.velocity * TICKS_PER_REVOLUTION / 10.0)
    }

    override fun setSelectedSensorPosition(sensorPos: Double, pidIdx: Int, timeoutMs: Int): ErrorCode {
        _motorController.encoder.position = sensorPos
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

    override fun configForwardSoftLimitThreshold(forwardSensorLimit: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configReverseSoftLimitThreshold(reverseSensorLimit: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configForwardSoftLimitEnable(enable: Boolean, timeoutMs: Int): ErrorCode {
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

    override fun getClosedLoopError(pidIdx: Int): Double {
        return (positionSetpoint * TICKS_PER_REVOLUTION)- getSelectedSensorPosition(pidIdx)
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

    override fun getActiveTrajectoryPosition(): Double {
        return 0.0
    }

    override fun getActiveTrajectoryVelocity(): Double {
        return 0.0
    }

    override fun configMotionCruiseVelocity(sensorUnitsPer100ms: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configMotionAcceleration(sensorUnitsPer100msPerSec: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
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
                velocitySetPoint = demand / TICKS_PER_REVOLUTION * 10.0

                //println("Velocity = $velocitySetPoint")

                // handle out of bounds conditions
                if (velocitySetPoint > maxRPM) {
                    velocitySetPoint = maxRPM
                } else if(velocitySetPoint < (-1 * maxRPM)) {
                    velocitySetPoint = -1 * maxRPM
                }

                // set reference point of
                _motorController.pidController.setReference(velocitySetPoint, CANSparkMax.ControlType.kVelocity)
            }
            ControlMode.PercentOutput -> _motorController.set(demand)
            ControlMode.Position -> {
                positionSetpoint = demand / TICKS_PER_REVOLUTION
                _motorController.pidController.setReference(velocitySetPoint, CANSparkMax.ControlType.kVelocity)
//                println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
            }
            else -> {}
        }
    }

    override fun set(Mode: ControlMode?, demand0: Double, demand1Type: DemandType?, demand1: Double) {
        when (Mode) {
            ControlMode.Velocity ->  {
                velocitySetPoint = demand0 / TICKS_PER_REVOLUTION * 10.0

                // handle out of bounds conditions
                if (velocitySetPoint > maxRPM) {
                    velocitySetPoint = maxRPM
                } else if(velocitySetPoint < (-1 * maxRPM)) {
                    velocitySetPoint = -1 * maxRPM
                }

                // set reference point of
                _motorController.pidController.setReference(velocitySetPoint, CANSparkMax.ControlType.kVelocity, 0, demand1)
            }
            ControlMode.PercentOutput -> _motorController.set(demand0)
            ControlMode.Position -> {
                positionSetpoint = demand0 / TICKS_PER_REVOLUTION
                _motorController.pidController.setReference(positionSetpoint, CANSparkMax.ControlType.kPosition, 0, demand1)
//                println("positionSetpoint = $positionSetpoint position=${_motorController.getEncoder().position}")
            }
            else -> {}
        }
    }


    fun setAngle(setPoint : Angle){
        set(ControlMode.Position, ((setPoint - getSelectedSensorPosition(0).degrees).wrap()).asDegrees)
    }

    override fun selectProfileSlot(slotIdx: Int, pidIdx: Int) {
        println("Error: SparkMaxWrapper.selectProfileSlot nonfunctional")
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
        println("kD=$value")
        return ErrorCode.OK
    }

    fun getDValue() : Double {
        return _motorController.pidController.d;
    }

    override fun config_kF(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.ff = value
        return ErrorCode.OK
    }

    override fun config_IntegralZone(slotIdx: Int, izone: Double, timeoutMs: Int): ErrorCode {
        return ErrorCode.OK
    }

    override fun configAllowableClosedloopError(
        slotIdx: Int,
        allowableCloseLoopError: Double,
        timeoutMs: Int
    ): ErrorCode {
        return ErrorCode.OK
    }

    override fun config_kI(slotIdx: Int, value: Double, timeoutMs: Int): ErrorCode {
        _motorController.pidController.i = value
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