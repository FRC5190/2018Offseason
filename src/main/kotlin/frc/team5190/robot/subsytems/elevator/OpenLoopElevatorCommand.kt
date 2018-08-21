/*
 * FRC Team 5190
 * Green Hope Falcons
 */

package frc.team5190.robot.subsytems.elevator

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.utils.DoubleSource


class OpenLoopElevatorCommand(private val percentOutput: DoubleSource) : Command() {
    init {
        +ElevatorSubsystem
    }

    override suspend fun execute() {
        if (ElevatorSubsystem.atBottom && !ElevatorSubsystem.reset) {
            ElevatorSubsystem.resetEncoders()
            ElevatorSubsystem.reset = true
        }
        if (ElevatorSubsystem.reset && !ElevatorSubsystem.atBottom) {
            ElevatorSubsystem.reset = false
        }
        ElevatorSubsystem.set(ControlMode.PercentOutput, percentOutput.value)
    }
}