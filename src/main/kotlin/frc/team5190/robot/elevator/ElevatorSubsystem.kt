package frc.team5190.robot.elevator

import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.LimitSwitchNormal
import com.ctre.phoenix.motorcontrol.LimitSwitchSource
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.Amps
import frc.team5190.lib.units.Inches
import frc.team5190.lib.units.Milliseconds
import frc.team5190.lib.units.preferences
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object ElevatorSubsystem : Subsystem() {

    private val prefs = preferences { radius = 1.25 / 2 }

    private val masterElevatorMotor = FalconSRX(Constants.MotorIDs.ELEVATOR_MASTER)
    private val slaveElevatorMotor = FalconSRX(Constants.MotorIDs.ELEVATOR_SLAVE)

    init {
        slaveElevatorMotor.apply {
            follow(masterElevatorMotor)
            inverted = true
        }

        masterElevatorMotor.apply {
            feedbackSensor = FeedbackDevice.QuadEncoder
            encoderPhase = false
            p = 0.5
            closedLoopTolerance = Inches(0.25, prefs)

            openLoopRamp = Milliseconds(300)
            closedLoopRamp = Milliseconds(500)

            continousCurrentLimit = Amps(30)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Milliseconds(0)
            currentLimitingEnabled = true

            softLimitFwd = Inches(89.455, prefs)
            softLimitFwdEnabled = true

            setLimitSwitch(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen)
            overrideLimitSwitchesEnable = true
        }
    }

    override fun initDefaultCommand() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}