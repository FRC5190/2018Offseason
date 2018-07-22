/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command

class IntakeHoldCommand : Command() {
    init {
        requires(IntakeSubsystem)
    }

    override suspend fun initialize() {
        IntakeSubsystem.solenoid.set(true)
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }

    override suspend fun isFinished(): Boolean = false
}