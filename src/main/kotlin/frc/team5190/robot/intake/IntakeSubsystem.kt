package frc.team5190.robot.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.units.Amps
import frc.team5190.lib.units.Milliseconds
import frc.team5190.lib.units.Volts
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.ChannelIDs
import frc.team5190.robot.MotorIDs
import frc.team5190.robot.SolenoidIDs

object IntakeSubsystem : Subsystem() {

    private val masterIntakeMotor = FalconSRX(MotorIDs.INTAKE_MASTER)
    private val slaveIntakeMotor = FalconSRX(MotorIDs.INTAKE_SLAVE)

    private val leftCubeSensor = AnalogInput(ChannelIDs.LEFT_CUBE_SENSOR)
    private val rightCubeSensor = AnalogInput(ChannelIDs.RIGHT_CUBE_SENSOR)

    val intakeSolenoid = Solenoid(SolenoidIDs.PCM, SolenoidIDs.INTAKE)

    val isCubeIn
        get() = leftCubeSensor.voltage > 0.9 && rightCubeSensor.voltage > 0.9

    init {
        slaveIntakeMotor.apply {
            follow(masterIntakeMotor)
            inverted = true
        }

        masterIntakeMotor.apply {
            inverted = false
            voltageCompensationSaturation = Volts(12.0)
            voltageCompensationEnabled = true

            continousCurrentLimit = Amps(18)
            peakCurrentLimit = Amps(0)
            peakCurrentLimitDuration = Milliseconds(0)
            currentLimitingEnabled = true
        }
    }

    fun set(controlMode: ControlMode, motorOutput: Double) {
        masterIntakeMotor.set(controlMode, motorOutput)
    }

    override fun initDefaultCommand() {
        defaultCommand = IntakeHoldCommand()
    }

}

enum class IntakeDirection {
    IN, OUT
}