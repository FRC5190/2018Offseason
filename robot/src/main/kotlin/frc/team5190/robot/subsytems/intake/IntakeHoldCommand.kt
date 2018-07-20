/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.intake

import com.ctre.phoenix.motorcontrol.ControlMode
import edu.wpi.first.wpilibj.command.Command

class IntakeHoldCommand : Command() {
    init {
        requires(IntakeSubsystem)
    }

    override fun initialize() {
        IntakeSubsystem.solenoid.set(true)
        IntakeSubsystem.set(ControlMode.PercentOutput, 0.0)
    }

    override fun isFinished(): Boolean = false
}