package frc.team5190.lib.wrappers

import com.ctre.phoenix.motorcontrol.*
import com.ctre.phoenix.motorcontrol.can.TalonSRX

class FalconSRX(id: Int, private val timeoutMs: Int = 10) : TalonSRX(id) {

    var p = 0.0
        set(value) {
            config_kP(0, value, timeoutMs)
            field = value
        }

    var i = 0.0
        set(value) {
            config_kI(0, value, timeoutMs)
            field = value
        }

    var d = 0.0
        set(value) {
            config_kD(0, value, timeoutMs)
            field = value
        }

    var f = 0.0
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

    var softLimitFwd = 0
        set(value) {
            configForwardSoftLimitThreshold(value, timeoutMs)
            field = value
        }

    var softLimitRev = 0
        set(value) {
            configReverseSoftLimitThreshold(value, timeoutMs)
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

    var closedLoopTolerance = 0
        set(value) {
            configAllowableClosedloopError(0, value, timeoutMs)
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

    var motionCruiseVelocity = 0
        set(value) {
            configMotionCruiseVelocity(value, timeoutMs)
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

    var peakCurrentLimit = 0
        set(value) {
            configPeakCurrentLimit(value, timeoutMs)
            field = value
        }

    var peakCurrentLimitDuration = 0
        set(value) {
            configPeakCurrentDuration(value, timeoutMs)
            field = value
        }

    var continousCurrentLimit = 0
        set(value) {
            configContinuousCurrentLimit(value, timeoutMs)
            field = value
        }

    var currentLimitingEnabled = false
        set(value) {
            enableCurrentLimit(value)
            field = value
        }

    var voltageCompensationSaturation = 12.0
        set(value) {
            configVoltageCompSaturation(value, timeoutMs)
            field = value
        }

    var voltageCompensationEnabled = false
        set(value) {
            enableVoltageCompensation(value)
            field = value
        }

    var sensorPosition = 0
        set(value) {
            setSelectedSensorPosition(value, 0, timeoutMs)
            field = value
        }
        get() = getSelectedSensorPosition(0)

    var sensorVelocity = 0
        private set
        get() = getSelectedSensorVelocity(0)

    fun setLimitSwitch(source: LimitSwitchSource, normal: LimitSwitchNormal) {
        configForwardLimitSwitchSource(source, normal, timeoutMs)
        configReverseLimitSwitchSource(source, normal, timeoutMs)
    }

    fun setStatusFramePeriod(frame: StatusFrameEnhanced, periodMs: Int) {
        setStatusFramePeriod(frame, periodMs, timeoutMs)
    }

}


