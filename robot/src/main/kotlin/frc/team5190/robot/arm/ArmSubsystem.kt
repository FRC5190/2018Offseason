package frc.team5190.robot.arm

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.FeedbackDevice
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.*
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.MotorIDs

object ArmSubsystem : Subsystem() {

    private val masterArmMotor = FalconSRX(MotorIDs.ARM)

    val currentPosition: Distance
        get() = masterArmMotor.sensorPosition


    init {
        masterArmMotor.apply {
            inverted = true

            feedbackSensor = FeedbackDevice.Analog
            encoderPhase = false

            brakeMode = NeutralMode.Brake

            p = 4.0
            peakFwdOutput = 1.0
            peakRevOutput = -1.0
            closedLoopTolerance = NativeUnits(0)

            motionCruiseVelocity = NativeUnitsPer100Ms(100000)
            motionAcceleration = 350

            closedLoopRamp = Milliseconds(300)
            openLoopRamp = Milliseconds(500)

            continousCurrentLimit = Amps(20)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Milliseconds(0)
            currentLimitingEnabled = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) {
        masterArmMotor.set(controlMode, output)
    }

    override fun initDefaultCommand() {
        defaultCommand = ManualArmCommand()
    }
}

enum class ArmPosition(val distance: Distance) {
    BEHIND(NativeUnits(-795 + 380)),
    ALL_UP(NativeUnits(-795 + 250)),
    UP(NativeUnits(-795 + 200)),
    MIDDLE(NativeUnits(-795 + 40)),
    DOWN(NativeUnits(-795 + 0)),
}