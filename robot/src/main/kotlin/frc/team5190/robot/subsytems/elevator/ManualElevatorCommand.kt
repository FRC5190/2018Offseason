/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import edu.wpi.first.wpilibj.command.Command

class ManualElevatorCommand : Command() {
    init {
        requires(ElevatorSubsystem)
    }

    override fun execute() {
        if (ElevatorSubsystem.atBottom && !ElevatorSubsystem.reset) {
            ElevatorSubsystem.resetEncoders()
            ElevatorSubsystem.reset = true
        }
        if (ElevatorSubsystem.reset && !ElevatorSubsystem.atBottom) {
            ElevatorSubsystem.reset = false
        }
    }

    override fun isFinished(): Boolean = false
}