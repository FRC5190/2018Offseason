/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package org.ghrobotics.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import com.ctre.phoenix.motorcontrol.NeutralMode
import edu.wpi.first.wpilibj.Solenoid
import org.ghrobotics.lib.commands.FalconSubsystem
import org.ghrobotics.lib.mathematics.units.amp
import org.ghrobotics.lib.mathematics.units.derivedunits.volt
import org.ghrobotics.lib.wrappers.ctre.NativeFalconSRX
import org.ghrobotics.robot.Constants

object IntakeSubsystem : FalconSubsystem() {
    private val intakeMaster = NativeFalconSRX(Constants.kIntakeMasterId)
    private val intakeSlave = NativeFalconSRX(Constants.kIntakeSlaveId)

    val solenoid = Solenoid(Constants.kPCMId, Constants.kIntakeSolenoidId)

    init {
        defaultCommand = IntakeHoldCommand()

        intakeMaster.run {
            voltageCompensationSaturation = 12.volt
            voltageCompensationEnabled = true

            continuousCurrentLimit = 18.amp
            currentLimitingEnabled = true

            brakeMode = NeutralMode.Coast
        }
        intakeSlave.run {
            follow(intakeMaster)
            inverted = true
        }
    }

    fun set(controlMode: ControlMode, output: Double) = intakeMaster.set(controlMode, output)

    override fun zeroOutputs() {
        set(ControlMode.PercentOutput, 0.0)
    }

    enum class Direction { IN, OUT }
}