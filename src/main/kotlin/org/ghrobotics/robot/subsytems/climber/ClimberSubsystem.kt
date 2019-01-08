package org.ghrobotics.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.nativeunits.STU
import org.ghrobotics.lib.mathematics.units.second
import org.ghrobotics.lib.wrappers.ctre.NativeFalconSRX
import org.ghrobotics.robot.Constants

object ClimberSubsystem : FalconSubsystem() {

    private val climberMaster = NativeFalconSRX(Constants.kWinchMasterId)
    private val climberSlave = NativeFalconSRX(Constants.kWinchSlaveId)

    var climberPosition
        get() = climberMaster.sensorPosition
        set(value) {
            climberMaster.set(ControlMode.MotionMagic, value)
        }

    init {
        climberMaster.run {
            inverted = false
            encoderPhase = false
            feedbackSensor = FeedbackDevice.QuadEncoder

            peakForwardOutput = 1.0
            peakReverseOutput = -1.0

            kP = Constants.kPClimber
            motionCruiseVelocity = Constants.kClimberMotionMagicVelocity
            motionAcceleration = Constants.kClimberMotionMagicAcceleration

            configReverseLimitSwitchSource(
                LimitSwitchSource.FeedbackConnector,
                LimitSwitchNormal.NormallyOpen,
                Constants.kCTRETimeout
            )
            overrideLimitSwitchesEnable = true

            continuousCurrentLimit = 40.amp
            peakCurrentLimit = 0.amp
            peakCurrentLimitDuration = 0.second
            currentLimitingEnabled = true
        }
        climberSlave.follow(climberMaster)

        defaultCommand = ClosedLoopClimbCommand()
    }

    fun set(controlMode: ControlMode, output: Double) {
        climberMaster.set(controlMode, output)
    }

    fun resetEncoders() {
        climberMaster.sensorPosition = 0.STU
    }
}