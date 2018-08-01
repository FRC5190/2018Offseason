/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.lib.wrappers

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import com.ctre.phoenix.motorcontrol.can.TalonSRX
import frc.team5190.lib.math.units.*

class FalconSRX(id: Int, private val timeoutMs: Int = 10) : TalonSRX(id) {

    var kP = 0.0
        set(value) {
            config_kP(0, value, timeoutMs)
            field = value
        }

    var kI = 0.0
        set(value) {
            config_kI(0, value, timeoutMs)
            field = value
        }

    var kD = 0.0
        set(value) {
            config_kD(0, value, timeoutMs)
            field = value
        }

    var kF = 0.0
        set(value) {
            config_kF(0, value, timeoutMs)
            field = value
        }

    var encoderPhase = false
        set(value) {
            setSensorPhase(value)
            field = value
        }

    var overrideLimitSwitchesEnable = false
        set(value) {
            overrideLimitSwitchesEnable(value)
            field = value
        }

    var softLimitFwd: Distance = NativeUnits(0)
        set(value) {
            configForwardSoftLimitThreshold(value.STU, timeoutMs)
            field = value
        }

    var softLimitRev: Distance = NativeUnits(0)
        set(value) {
            configReverseSoftLimitThreshold(value.STU, timeoutMs)
            field = value
        }

    var softLimitFwdEnabled = false
        set(value) {
            configForwardSoftLimitEnable(value, timeoutMs)
            field = value
        }

    var softLimitRevEnabled = false
        set(value) {
            configReverseSoftLimitEnable(value, timeoutMs)
            field = value
        }

    var brakeMode = NeutralMode.Coast
        set(value) {
            setNeutralMode(value)
            field = value
        }

    var closedLoopTolerance: Distance = NativeUnits(0)
        set(value) {
            configAllowableClosedloopError(0, value.STU, timeoutMs)
            field = value
        }

    var nominalFwdOutput = 0.0
        set(value) {
            configNominalOutputForward(value, timeoutMs)
            field = value
        }

    var nominalRevOutput = 0.0
        set(value) {
            configNominalOutputReverse(value, timeoutMs)
            field = value
        }

    var peakFwdOutput = 1.0
        set(value) {
            configPeakOutputForward(value, timeoutMs)
            field = value
        }

    var peakRevOutput = -1.0
        set(value) {
            configPeakOutputReverse(value, timeoutMs)
            field = value
        }

    var openLoopRamp: Time = Seconds(0.0)
        set(value) {
            configOpenloopRamp(value.SEC, timeoutMs)
            field = value
        }

    var closedLoopRamp: Time = Seconds(0.0)
        set(value) {
            configClosedloopRamp(value.SEC, timeoutMs)
            field = value
        }

    var motionCruiseVelocity: Speed = NativeUnitsPer100Ms(0)
        set(value) {
            configMotionCruiseVelocity(value.STU, timeoutMs)
            field = value
        }

    var motionAcceleration = 0
        set(value) {
            configMotionAcceleration(value, timeoutMs)
            field = value
        }

    var feedbackSensor = FeedbackDevice.None
        set(value) {
            configSelectedFeedbackSensor(value, 0, timeoutMs)
            field = value
        }

    var peakCurrentLimit: Current = Amps(0)
        set(value) {
            configPeakCurrentLimit(value.amps, timeoutMs)
            field = value
        }

    var peakCurrentLimitDuration: Time = Milliseconds(0)
        set(value) {
            configPeakCurrentDuration(value.MS, timeoutMs)
            field = value
        }

    var continousCurrentLimit: Current = Amps(0)
        set(value) {
            configContinuousCurrentLimit(value.amps, timeoutMs)
            field = value
        }

    var currentLimitingEnabled = false
        set(value) {
            enableCurrentLimit(value)
            field = value
        }

    var voltageCompensationSaturation: Voltage = Volts(12.0)
        set(value) {
            configVoltageCompSaturation(value.volts, timeoutMs)
            field = value
        }

    var voltageCompensationEnabled = false
        set(value) {
            enableVoltageCompensation(value)
            field = value
        }

    var sensorPosition: Distance = NativeUnits(0)
        set(value) {
            setSelectedSensorPosition(value.STU, 0, timeoutMs)
            field = value
        }
        get() = NativeUnits(getSelectedSensorPosition(0))

    var sensorVelocity: Speed = NativeUnitsPer100Ms(0)
        private set
        get() = NativeUnitsPer100Ms(getSelectedSensorVelocity(0))

    init {
//        kP = 0.0; kI = 0.0; kD = 0.0; kF = 0.0
//        encoderPhase = false; overrideLimitSwitchesEnable = false
//        softLimitFwd = NativeUnits(0); softLimitFwdEnabled = false
//        softLimitRev = NativeUnits(0); softLimitRevEnabled = false
//        openLoopRamp = Seconds(0.0); closedLoopRamp = Seconds(0.0)
//        motionCruiseVelocity = NativeUnitsPer100Ms(0); motionAcceleration = 0
//        feedbackSensor = FeedbackDevice.None
//        peakCurrentLimit = Amps(0); continousCurrentLimit = Amps(0)
//        peakCurrentLimitDuration = Seconds(0.0); currentLimitingEnabled = false
//        voltageCompensationSaturation = Volts(12.0); voltageCompensationEnabled = false
    }

}


