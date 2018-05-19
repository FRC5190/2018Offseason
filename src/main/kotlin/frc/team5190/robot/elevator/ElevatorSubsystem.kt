package frc.team5190.robot.elevator

import com.ctre.phoenix.motorcontrol.*
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.MotorIDs
import frc.team5190.robot.elevator.ElevatorSubsystem.prefs

object ElevatorSubsystem : Subsystem() {

    private val masterElevatorMotor = FalconSRX(MotorIDs.ELEVATOR_MASTER)
    private val slaveElevatorMotor = FalconSRX(MotorIDs.ELEVATOR_SLAVE)

    val prefs = preferences { radius = 1.25 / 2 }

    val currentPosition: Distance
        get() = masterElevatorMotor.sensorPosition

    init {
        slaveElevatorMotor.apply {
            follow(masterElevatorMotor)
            inverted = true
        }

        masterElevatorMotor.apply {
            feedbackSensor = FeedbackDevice.QuadEncoder
            encoderPhase = false
            p = 0.3
            closedLoopTolerance = Inches(0.25, prefs)

            brakeMode = NeutralMode.Brake

            peakFwdOutput = 1.0
            peakRevOutput = -1.0

            openLoopRamp = Milliseconds(300)
            closedLoopRamp = Milliseconds(500)

            continousCurrentLimit = Amps(30)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Milliseconds(0)
            currentLimitingEnabled = true

            softLimitFwd = Inches(89.455, prefs)
            softLimitFwdEnabled = true

            voltageCompensationSaturation = Volts(12.0)
            voltageCompensationEnabled = true

            setLimitSwitch(LimitSwitchSource.FeedbackConnector, LimitSwitchNormal.NormallyOpen)
            overrideLimitSwitchesEnable = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) {
        masterElevatorMotor.set(controlMode, output)
    }

    override fun initDefaultCommand() {
        defaultCommand = ManualElevatorCommand()
    }
}

enum class ElevatorPosition(val distance: Distance) {
    INTAKE(NativeUnits(500, prefs)),
    SWITCH(Inches(27.0, ElevatorSubsystem.prefs)),
    FIRST_STAGE(Inches(32.0, ElevatorSubsystem.prefs)),
    SCALE(NativeUnits(17000, prefs)),
    SCALE_HIGH(Inches(60.0, prefs))
}