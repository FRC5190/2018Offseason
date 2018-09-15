package frc.team5190.robot.subsytems.climber

import com.ctre.phoenix.motorcontrol.ControlMode
import frc.team5190.lib.commands.Command
import frc.team5190.lib.utils.DoubleSource

class OpenLoopClimbCommand(private val percentOutput: DoubleSource) : Command(ClimberSubsystem) {
    override suspend fun execute() {
        ClimberSubsystem.set(ControlMode.PercentOutput, percentOutput.value)
    }
}