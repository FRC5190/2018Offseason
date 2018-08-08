/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.AnalogInput
import edu.wpi.first.wpilibj.Solenoid
import frc.team5190.lib.commands.Subsystem
import frc.team5190.lib.math.units.Amps
import frc.team5190.lib.math.units.Volts
import frc.team5190.lib.wrappers.FalconSRX
import frc.team5190.robot.Constants

object IntakeSubsystem : Subsystem() {
    private val intakeMaster = FalconSRX(Constants.kIntakeMasterId)
    private val intakeSlave = FalconSRX(Constants.kIntakeSlaveId)

    val solenoid = Solenoid(Constants.kPCMId, Constants.kIntakeSolenoidId)

    init {
        defaultCommand = IntakeHoldCommand()

        intakeMaster.apply {
            voltageCompensationSaturation = Volts(12.0)
            voltageCompensationEnabled = true

            continousCurrentLimit = Amps(18)
            currentLimitingEnabled = true

            brakeMode = NeutralMode.Coast
        }
        intakeSlave.apply {
            follow(intakeMaster)
            inverted = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) = intakeMaster.set(controlMode, output)

    enum class Direction { IN, OUT }
}