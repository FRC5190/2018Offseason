/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.mathematics.units.Amps
import org.ghrobotics.lib.mathematics.units.Volts
import org.ghrobotics.lib.wrappers.FalconSRX
import org.ghrobotics.robot.Constants

object IntakeSubsystem : Subsystem() {
    private val intakeMaster = FalconSRX(Constants.kIntakeMasterId)
    private val intakeSlave = FalconSRX(Constants.kIntakeSlaveId)

    val solenoid = Solenoid(Constants.kPCMId, Constants.kIntakeSolenoidId)

    init {
        defaultCommand = IntakeHoldCommand()

        intakeMaster.apply {
            voltageCompensationSaturation = Volts(12.0)
            voltageCompensationEnabled = true

            continuousCurrentLimit = Amps(18)
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