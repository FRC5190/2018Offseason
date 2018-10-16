/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.commands.Subsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import org.ghrobotics.lib.wrappers.GenericFalonSRX
import org.ghrobotics.robot.Constants

object IntakeSubsystem : Subsystem() {
    private val intakeMaster = GenericFalonSRX(Constants.kIntakeMasterId)
    private val intakeSlave = GenericFalonSRX(Constants.kIntakeSlaveId)

    val solenoid = Solenoid(Constants.kPCMId, Constants.kIntakeSolenoidId)

    init {
        defaultCommand = IntakeHoldCommand()

        intakeMaster.apply {
            voltageCompensationSaturation = 12.volt
            voltageCompensationEnabled = true

            continuousCurrentLimit = 18.amp
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