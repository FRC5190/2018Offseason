/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.*
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.math.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object ElevatorSubsystem : Subsystem() {

    private val elevatorMaster = FalconSRX(Constants.kElevatorMasterId)
    private val elevatorSlave  = FalconSRX(Constants.kElevatorSlaveId)

    val atBottom
        get() = elevatorMaster.sensorCollection.isRevLimitSwitchClosed

    val currentPosition: Distance
        get() = elevatorMaster.sensorPosition

    val settings = preferences { radius = 1.25 / 2.0 }

    var reset = false

    init {
        elevatorMaster.apply {
            inverted       = false
            encoderPhase   = false
            feedbackSensor = FeedbackDevice.QuadEncoder
            peakFwdOutput = 0.3
            peakRevOutput = -0.3

            configForwardLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                    Constants.kCTRETimeout)
            configReverseLimitSwitchSource(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                    Constants.kCTRETimeout)
            overrideLimitSwitchesEnable = true

            softLimitFwd        = Constants.kElevatorSoftLimitFwd
            softLimitFwdEnabled = true

            kP                  = Constants.kPElevator
            closedLoopTolerance = Constants.kElevatorClosedLpTolerance

            continousCurrentLimit  = Amps(30)
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kElevatorMotionMagicVelocity
            motionAcceleration   = Constants.kElevatorMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }
        elevatorSlave.apply {
            follow(elevatorSlave)
            inverted = true
        }
        defaultCommand = ClosedLoopElevatorCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = elevatorMaster.set(controlMode, output)

    fun resetEncoders() {
        elevatorMaster.sensorPosition = NativeUnits(0)
    }

    enum class Position(val distance: Distance) {
        SWITCH   (Inches(27.0, settings)),
        FSTAGE   (Inches(32.0, settings)),
        SCALE    (NativeUnits(17000, settings)),
        HIGHSCALE(Inches(60.0, settings)),
        INTAKE   (NativeUnits(500))
    }
}