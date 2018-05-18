package frc.team5190.robot.climb

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.MotorIDs

object ClimbSubsystem : Subsystem() {

    private val masterClimbMotor = FalconSRX(MotorIDs.WINCH_MASTER)
    private val slaveClimbMotor = FalconSRX(MotorIDs.WINCH_SLAVE)

    val currentPosition: Distance
        get() = masterClimbMotor.sensorPosition

    init {
        slaveClimbMotor.apply {
            follow(masterClimbMotor)
            inverted = false
        }
        masterClimbMotor.apply {
            inverted = false
            encoderPhase = false

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            p = 2.0
            motionCruiseVelocity = NativeUnitsPer100Ms(10000000)
            motionAcceleration = 12000

            setLimitSwitch(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen)
            overrideLimitSwitchesEnable = true

            continousCurrentLimit = Amps(40)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Milliseconds(0)
            currentLimitingEnabled = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) {
        masterClimbMotor.set(controlMode, output)
    }

    fun resetEncoders() {
        masterClimbMotor.sensorPosition = NativeUnits(0)
    }

    override fun initDefaultCommand() {
        defaultCommand = IdleClimbCommand()
    }


}