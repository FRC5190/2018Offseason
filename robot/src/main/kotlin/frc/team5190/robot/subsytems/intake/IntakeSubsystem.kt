/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.Solenoid
import edu.wpi.first.wpilibj.command.Subsystem
import frc.team5190.lib.math.units.Amps
import frc.team5190.lib.math.units.Volts
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object IntakeSubsystem : Subsystem() {
    private val intakeMaster = FalconSRX(Constants.kIntakeMasterId)
    private val intakeSlave = FalconSRX(Constants.kIntakeSlaveId)

    private val leftCubeSensor = AnalogInput(Constants.kLeftCubeSensorId)
    private val rightCubeSensor = AnalogInput(Constants.kRightCubeSensorId)

    val solenoid = Solenoid(Constants.kPCMId, Constants.kIntakeSolenoidId)

    val cubeIn
        get() = leftCubeSensor.voltage > Volts(0.9).volts && rightCubeSensor.voltage > Volts(0.9).volts

    val amperage: Double
        get() = intakeMaster.outputCurrent

    init {
        intakeMaster.apply {
            voltageCompensationSaturation = Volts(12.0)
            voltageCompensationEnabled = true

            continousCurrentLimit = Amps(18)
            currentLimitingEnabled = true
        }
        intakeSlave.apply {
            follow(intakeMaster)
            inverted = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) {
        intakeMaster.set(controlMode, output)
    }

    override fun initDefaultCommand() {
        defaultCommand = IntakeHoldCommand()
    }

    enum class Direction { IN, OUT }
}