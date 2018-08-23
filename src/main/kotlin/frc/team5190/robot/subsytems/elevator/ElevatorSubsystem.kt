/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.*
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.math.units.*
import frc.team5190.lib.utils.launchFrequency
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object ElevatorSubsystem : Subsystem() {

    private val elevatorMaster = FalconSRX(Constants.kElevatorMasterId)
    private val elevatorSlave = FalconSRX(Constants.kElevatorSlaveId)

    val settings = preferences { radius = 1.25 / 2.0 }

    val kSwitchPosition = Inches(27.0, settings)
    val kFirstStagePosition = Inches(32.0, settings)
    val kScalePosition = NativeUnits(17000, settings)
    val kHighScalePosition = Inches(60.0, settings)
    val kIntakePosition = NativeUnits(500, settings)


    val atBottom
        get() = elevatorMaster.sensorCollection.isRevLimitSwitchClosed

    val currentPosition: Distance
        get() = elevatorMaster.sensorPosition


    var reset = false

    init {
        elevatorMaster.apply {
            inverted = false
            encoderPhase = false
            feedbackSensor = FeedbackDevice.QuadEncoder

            configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                    Constants.kCTRETimeout)
            configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                    Constants.kCTRETimeout)
            overrideLimitSwitchesEnable = true

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            softLimitFwd = Constants.kElevatorSoftLimitFwd
            softLimitFwdEnabled = true

            kP = Constants.kPElevator
            kF = Constants.kVElevator
            closedLoopTolerance = Constants.kElevatorClosedLpTolerance

            continuousCurrentLimit = Amps(30)
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kElevatorMotionMagicVelocity
            motionAcceleration = Constants.kElevatorMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }
        elevatorSlave.apply {
            follow(elevatorMaster)
            inverted = true
        }
        defaultCommand = ClosedLoopElevatorCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = elevatorMaster.set(controlMode, output)

    fun resetEncoders() {
        elevatorMaster.sensorPosition = NativeUnits(0)
    }
}