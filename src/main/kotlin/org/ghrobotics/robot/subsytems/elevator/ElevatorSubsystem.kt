/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.elevator

/* ktlint-disable no-wildcard-imports */
import com.ctre.phoenix.motorcontrol.*
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.inch
import org.ghrobotics.lib.mathematics.units.meter
import org.ghrobotics.lib.mathematics.units.nativeunits.STU
import org.ghrobotics.lib.wrappers.ctre.FalconSRX
import org.ghrobotics.robot.Constants

object ElevatorSubsystem : FalconSubsystem() {

    private val elevatorMaster = FalconSRX(Constants.kElevatorMasterId, Constants.elevatorNativeUnitSettings)
    private val elevatorSlave = FalconSRX(Constants.kElevatorSlaveId, Constants.elevatorNativeUnitSettings)

    val kSwitchPosition = 27.inch
    val kFirstStagePosition = 32.inch
    val kScalePosition = 17000.STU.toModel(Constants.elevatorNativeUnitSettings)
    val kHighScalePosition = 60.inch
    val kIntakePosition = 500.STU.toModel(Constants.elevatorNativeUnitSettings)

    val atBottom get() = elevatorMaster.sensorCollection.isRevLimitSwitchClosed

    var elevatorPosition
        get() = elevatorMaster.sensorPosition
        set(value) {
            elevatorMaster.set(ControlMode.MotionMagic, value)
        }

    var reset = false

    init {
        elevatorMaster.run {
            inverted = false
            encoderPhase = false
            feedbackSensor = FeedbackDevice.QuadEncoder

            configForwardLimitSwitchSource(
                LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                Constants.kCTRETimeout
            )
            configReverseLimitSwitchSource(
                LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen,
                Constants.kCTRETimeout
            )
            overrideLimitSwitchesEnable = true

            peakForwardOutput = 1.0
            peakReverseOutput = -1.0

            softLimitForward = Constants.kElevatorSoftLimitFwd
            softLimitForwardEnabled = true

            kP = Constants.kPElevator
            kF = Constants.kVElevator
            allowedClosedLoopError = Constants.kElevatorClosedLpTolerance

            continuousCurrentLimit = 30.amp
            currentLimitingEnabled = true

            motionCruiseVelocity = Constants.kElevatorMotionMagicVelocity
            motionAcceleration = Constants.kElevatorMotionMagicAcceleration

            brakeMode = NeutralMode.Brake
        }
        elevatorSlave.run {
            follow(elevatorMaster)
            inverted = true
        }
        defaultCommand = ClosedLoopElevatorCommand()
    }

    fun set(controlMode: ControlMode, output: Double) = elevatorMaster.set(controlMode, output)

    fun resetEncoders() {
        elevatorMaster.sensorPosition = 0.meter
    }

    override fun zeroOutputs() {
        set(ControlMode.PercentOutput, 0.0)
    }
}