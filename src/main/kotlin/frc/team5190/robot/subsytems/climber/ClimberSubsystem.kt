package frc.team5190.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.mathematics.units.Amps
import frc.team5190.lib.mathematics.units.Distance
import frc.team5190.lib.mathematics.units.NativeUnits
import frc.team5190.lib.mathematics.units.Seconds
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object ClimberSubsystem : Subsystem() {

    private val climberMaster = FalconSRX(Constants.kWinchMasterId)
    private val climberSlave = FalconSRX(Constants.kWinchSlaveId)

    val kHighScalePosition = NativeUnits(47600)
    val kBottomPosition = NativeUnits(0)

    val currentPosition: Distance
        get() = climberMaster.sensorPosition

    init {
        climberMaster.apply {
            inverted = false
            encoderPhase = false
            feedbackSensor = FeedbackDevice.QuadEncoder

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            kP = Constants.kPClimber
            motionCruiseVelocity = Constants.kClimberMotionMagicVelocity
            motionAcceleration = Constants.kClimberMotionMagicAcceleration

            configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen, Constants.kCTRETimeout)
            overrideLimitSwitchesEnable = true

            continuousCurrentLimit = Amps(40)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Seconds(0.0)
            currentLimitingEnabled = true
        }
        climberSlave.follow(climberMaster)
        defaultCommand = ClosedLoopClimbCommand()
    }

    fun set(controlMode: ControlMode, output: Double) {
        climberMaster.set(controlMode, output)
    }

    fun resetEncoders() {
        climberMaster.sensorPosition = NativeUnits(0)
    }
}